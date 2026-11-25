package com.linh.snackgo;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import androidx.test.platform.app.InstrumentationRegistry;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.constants.PrefConstants;
import com.linh.snackgo.controller.*;
import com.linh.snackgo.data.*;
import com.linh.snackgo.model.*;
import com.linh.snackgo.utils.*;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import static org.junit.Assert.*;

/** kiểm thử nghiệp vụ với dữ liệu riêng. */
public class BackendSmokeTest {
    private static final String PREFIX = "backend-smoke-";
    private static Context context;
    private SQLiteDatabase db;
    private SessionManager session;
    private CartDao cart;
    private ProductDao products;
    private OrderController orders;
    private long userId, first, second;

    @BeforeClass
    public static void isolateStorage() {
        Context target = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextWrapper(target) {
            @Override public Context getApplicationContext() { return this; }
            @Override public File getDatabasePath(String name) { return target.getDatabasePath(PREFIX + name); }
            @Override public SharedPreferences getSharedPreferences(String name, int mode) {
                return target.getSharedPreferences(PREFIX + name, mode);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
                return target.openOrCreateDatabase(PREFIX + name, mode, factory);
            }
            @Override public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                    SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler handler) {
                return target.openOrCreateDatabase(PREFIX + name, mode, factory, handler);
            }
        };
    }

    @Before
    public void setUp() {
        db = DatabaseHelper.getInstance(context).getWritableDatabase();
        clearFixtures();
        session = new SessionManager(context);
        cart = new CartDao(context);
        products = new ProductDao(context);
        orders = new OrderController(context);
        userId = createUser("user", AppConstants.ROLE_USER);
        session.createLoginSession(userId, AppConstants.ROLE_USER);
        first = products.insert(product(25000));
        second = products.insert(product(20000));
    }

    @After
    public void cleanUp() { if (db != null) clearFixtures(); }

    private void clearFixtures() {
        assertTrue(db.getPath().endsWith(PREFIX + DbConstants.DB_NAME));
        for (String table : Arrays.asList(DbConstants.TABLE_ORDER_ITEMS, DbConstants.TABLE_ORDERS,
                DbConstants.TABLE_CART_ITEMS, DbConstants.TABLE_PRODUCT_IMAGES,
                DbConstants.TABLE_PRODUCTS, DbConstants.TABLE_USERS)) db.delete(table, null, null);
        context.getSharedPreferences(PrefConstants.PREF_NAME, Context.MODE_PRIVATE).edit().clear().commit();
    }

    private long createUser(String name, String role) {
        String salt = PasswordUtils.generateSalt();
        return new UserDao(context).insert(new User(name, name + "@example.com", "0912345678",
                PasswordUtils.hashPassword("Secret123", salt), salt, "123 Đường ABC", "", role));
    }

    private Product product(long price) {
        return new Product("Món", "Mô tả", AppConstants.CATEGORY_SNACK, price, 0, "", "snack_lays_cheddar", true);
    }

    private OrderController.OrderResult checkout(List<Long> selected) {
        return orders.placeOrder(userId, "Người nhận", "0987654321", "456 Đường XYZ",
                AppConstants.PAYMENT_CASH, true, selected);
    }

    @Test
    public void cartRejectsInvalidQuantityAndCapsWithoutOverflow() {
        assertFalse(cart.addOrIncrease(userId, first, 0));
        assertFalse(cart.addOrIncrease(userId, first, -1));
        assertTrue(cart.addOrIncrease(userId, first, 2));
        assertFalse(cart.setQuantity(userId, first, 0));
        assertEquals(2, cart.getTotalCount(userId));
        assertTrue(cart.addOrIncrease(userId, first, Integer.MAX_VALUE));
        assertEquals(AppConstants.MAX_QUANTITY_PER_ITEM, cart.getTotalCount(userId));
        assertFalse(cart.addOrIncrease(userId, first, 1));
    }

    @Test
    public void concurrentCartAddsKeepEveryIncrement() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Callable<Boolean>> calls = new ArrayList<>();
            for (int i = 0; i < 20; i++) calls.add(() -> cart.addOrIncrease(userId, first, 1));
            for (Future<Boolean> result : executor.invokeAll(calls, 15, TimeUnit.SECONDS)) assertTrue(result.get());
            assertEquals(20, cart.getTotalCount(userId));
        } finally { executor.shutdownNow(); }
    }

    @Test
    public void concurrentCheckoutsCreateOnlyOneOrder() throws Exception {
        cart.addOrIncrease(userId, first, 1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Boolean> placeOrder = () -> checkout(null).isSuccess();
            int successful = 0;
            for (Future<Boolean> result : executor.invokeAll(Arrays.asList(placeOrder, placeOrder), 15, TimeUnit.SECONDS)) {
                if (result.get()) successful++;
            }
            assertEquals(1, successful);
            assertEquals(1, new OrderDao(context).getOrdersByUser(userId).size());
        } finally { executor.shutdownNow(); }
    }

    @Test
    public void checkoutOnlyConsumesSelectionAndCannotReuseOrChargeStaleCart() {
        cart.addOrIncrease(userId, first, 2);
        cart.addOrIncrease(userId, second, 1);
        long id = checkout(Collections.singletonList(first)).getOrderId();
        Order saved = orders.getOrderDetail(id);
        assertNotNull(saved);
        assertEquals(60000, saved.getTotalAmount());
        assertEquals(1, cart.getTotalCount(userId));
        assertEquals("user", session.getUserName());
        assertEquals("0912345678", session.getUserPhone());
        OrderDao dao = new OrderDao(context);
        assertFalse(dao.createOrder(saved, saved.getOrderItems()));
        cart.addOrIncrease(userId, first, 2);
        Product changed = products.getById(first);
        changed.setPrice(30000);
        assertTrue(products.update(changed));
        assertFalse(dao.createOrder(saved, saved.getOrderItems()));
        assertEquals(1, dao.getOrdersByUser(userId).size());
        assertEquals(3, cart.getTotalCount(userId));
        assertEquals(25000, dao.getById(id).getOrderItems().get(0).getUnitPrice());
    }

    @Test
    public void invalidSelectionAndPaymentLeaveCartUntouched() {
        cart.addOrIncrease(userId, first, 1);
        assertFalse(checkout(Collections.emptyList()).isSuccess());
        assertFalse(checkout(Arrays.asList(first, second)).isSuccess());
        assertFalse(orders.placeOrder(userId, "Tên", "0912345678", "123 ABC", "invalid", false, null).isSuccess());
        assertEquals(1, cart.getTotalCount(userId));
    }

    @Test
    public void anotherAccountCannotReadCancelOrModifyUserData() {
        cart.addOrIncrease(userId, first, 1);
        long id = checkout(null).getOrderId();
        session.createLoginSession(createUser("other", AppConstants.ROLE_USER), AppConstants.ROLE_USER);
        assertNull(orders.getOrderDetail(id));
        assertFalse(orders.cancelOrder(id, userId));
        assertFalse(orders.reorder(id, userId));
        assertFalse(new CartController(context).addToCart(userId, first));
        assertFalse(new UserController(context).updateProfile(userId, "Khác", "0977777777", "ABC", null).isSuccess());
        assertFalse(new AdminProductController(context).deleteProduct(first));
    }

    @Test
    public void cancellationOnlySucceedsOnceForTheOwner() {
        cart.addOrIncrease(userId, first, 1);
        long id = checkout(null).getOrderId();
        assertTrue(orders.cancelOrder(id, userId));
        assertFalse(orders.cancelOrder(id, userId));
        assertEquals(AppConstants.ORDER_STATUS_CANCELLED, orders.getOrderDetail(id).getStatus());
    }

    @Test
    public void galleryOrderAndPhotoSurviveSyncAndSoftDelete() throws Exception {
        long admin = createUser("admin", AppConstants.ROLE_ADMIN);
        session.createLoginSession(admin, AppConstants.ROLE_ADMIN);
        ProductImageSyncer syncer = new ProductImageSyncer(context);
        syncer.syncAssetImages();
        Product edited = products.getById(first);
        File photo = File.createTempFile("backend-photo", ".jpg", context.getCacheDir());
        try {
            List<String> paths = new ArrayList<>(edited.getImages());
            Collections.reverse(paths);
            paths.add(0, photo.getAbsolutePath());
            edited.setImages(paths);
            AdminProductController controller = new AdminProductController(context);
            assertTrue(controller.updateProduct(edited));
            syncer.syncAssetImages();
            assertEquals(paths, products.getById(first).getImages());
            cart.addOrIncrease(userId, first, 1);
            assertTrue(controller.deleteProduct(first));
            assertTrue(photo.exists());
            assertFalse(cart.addOrIncrease(userId, first, 1));
            assertEquals(0, cart.getTotalCount(userId));
            assertTrue(controller.restoreProduct(first));
            assertEquals(paths, products.getById(first).getImages());
        } finally { photo.delete(); }
    }

    @Test
    public void failedGallerySaveRollsBackProductChanges() {
        Product edited = products.getById(first);
        edited.setPrice(99999);
        edited.setImages(Arrays.asList("/data/test/image.jpg", null));
        assertFalse(products.update(edited));
        assertEquals(25000, products.getById(first).getPrice());
        assertEquals(-1, products.insert(edited));
        assertEquals(2, products.getAllActive().size());
    }

    @Test
    public void loginPrefillsProfileAndLogoutPreservesFavorites() {
        FavoriteManager favorites = new FavoriteManager(context);
        favorites.setFavorite(userId, first, true);
        session.setOnboardingDone(true);
        session.logout();
        assertTrue(new AuthController(context).login("user@example.com", "Secret123").isSuccess());
        assertEquals("0912345678", session.getUserPhone());
        assertEquals("123 Đường ABC", session.getUserAddress());
        assertTrue(favorites.isFavorite(userId, first));
        assertTrue(session.isOnboardingDone());
        assertFalse(context.getSharedPreferences(PrefConstants.PREF_NAME, 0).contains(PrefConstants.KEY_USER_NAME));
    }

    @Test
    public void adminValidationAcceptsSeedDescriptionAndRejectsInvalidFields() {
        session.createLoginSession(userId, AppConstants.ROLE_ADMIN);
        assertFalse(session.isAdmin());
        session.createLoginSession(createUser("admin", AppConstants.ROLE_ADMIN), AppConstants.ROLE_ADMIN);
        AdminProductController controller = new AdminProductController(context);
        assertTrue(controller.validate("Custas", "56000", "62000", AppConstants.CATEGORY_CANDY,
                DatabaseHelper.SEED_CUSTAS_TIRAMISU_DESCRIPTION, "").isValid());
        assertFalse(controller.validate("Món", "0", "", AppConstants.CATEGORY_SNACK, "Mô tả", "").isValid());
        assertFalse(controller.validate("Món", "10000", "", "invalid", "Mô tả", "").isValid());
        assertFalse(controller.validate("Món", "10000", "", AppConstants.CATEGORY_SNACK, "", "").isValid());
    }
}
