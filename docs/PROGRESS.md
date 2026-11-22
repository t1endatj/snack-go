# SnackGo Implementation Progress

## Task 1: Foundation
- **Status**: DONE
- **Completed Items**:
  - Configured Gradle dependencies (`viewBinding`, Material Components 1.12.0, RecyclerView 1.3.2, CardView 1.0.0, ConstraintLayout 2.1.4, ViewPager2 1.1.0, Glide 4.16.0).
  - Configured `Theme.Material3.Light.NoActionBar` and color palette (`primary_ink` #1C1B1F, `accent_orange` #FF8A00, `amber` #FFB300, `red` #E53935, `background` #FAF8F5, `surface` #FFFFFF, `secondary_text` #6B6B6B, `divider` #EEEBE6, and pastel card backgrounds).
  - Applied Poppins font app-wide and created text styles (`TextAppearance.SnackGo.Headline`, `Title`, `Body`, `Price`).
  - Created Material 3 shape appearances (24dp card corner radius, fully rounded buttons and chips).
  - Created `AppConstants` for app-wide defaults, roles, categories, order statuses, and payment methods.
  - Created `DbConstants` with database name `snackgo.db` (v1) and exact table/column names for `users`, `products`, `cart_items`, `orders`, and `order_items`.
  - Created `PrefConstants` for SharedPreferences keys.
  - Created `IntentKeys` for Intent extras.
  - Created `SessionManager` wrapping SharedPreferences.
  - Created `PriceFormatter` formatting integers/doubles to VND format (e.g., `22000` -> `"22.000đ"`).
  - Set up package structure (`model/`, `view/activity/`, `view/fragment/`, `view/adapter/`, `controller/`, `data/`, `utils/`, `constants/`).

---

## Task 2: Models + DatabaseHelper + DAOs + Seed Data
- **Status**: DONE
- **Completed Items**:
  - Fixed price fields spec deviation: converted all price/money fields to `long` (VND integers) across `Product`, `CartItem`, `Order`, `OrderItem`, `AppConstants`, `DatabaseHelper`, DAOs, and `PriceFormatter`.
  - Constants completion (`AppConstants` with promo codes, seeded admin credentials, categories, order statuses; `DbConstants` with full schema).
  - Data models (`User`, `Product`, `CartItem`, `OrderItem`, `Order`).
  - `PasswordUtils` using SHA-256 with 16-byte random salt for hashing & verification.
  - `DatabaseHelper` (singleton SQLiteOpenHelper, foreign keys enabled, 5 tables created per schema, automatic seeding of 1 admin user and 13 products across 3 categories on first launch).
  - DAOs:
    - `UserDao` (insert, getByEmail, getByPhone, getById, update, updatePassword, phoneExists, phoneExistsForOtherUser, emailExists).
    - `ProductDao` (insert, update, softDelete, updateActiveStatus, getById, getAllActive, getActiveByCategory, searchActive).
    - `ProductImageDao` (insert, deleteByProduct, deleteAssetImagesByProduct, getByProduct, getAllImagesGroupedByProduct, getCoverImagesForAllProducts).
    - `CartDao` (addOrIncrease, setQuantity, remove, clear, getItems joined with products, getTotalCount, removeProductFromAllCarts).
    - `OrderDao` (createOrder in a single transaction, getOrdersByUser, getOrderSummariesByUser, getItems, getById, updateStatus, generateOrderCode).
  - Created `ic_placeholder_product.xml` vector drawable and `ImageUtils` helper for safe resource loading.

---

## Task 3: Onboarding, Login, Register, AuthController
- **Status**: DONE
- **Completed Items**:
  - `SplashActivity` configured as launcher activity in `AndroidManifest.xml` (dispatches based on `KEY_ONBOARDING_DONE` and `KEY_IS_LOGGED_IN`). Runs `ProductImageSyncer` in background thread.
  - `OnboardingActivity` with 3 ViewPager2 slides (`OnboardingAdapter`), top orange-to-amber gradient, 40% screen height illustration area with soft white circular glow (`bg_illustration_glow`), white rounded bottom sheet (32dp top corners), logo chip "SnackGo", headline (26sp bold), subtext (15sp grey), indicator dots (active orange pill `#FF8A00`, unselected `#D9D9D9`), full-width ink pill button (54dp min height, 16sp semibold) with trailing arrow icon, "Bỏ qua" button with chevron icon, saving `KEY_ONBOARDING_DONE` and routing to `LoginActivity`.
  - `LoginActivity` on cream background (`#FAF8F5`), conditional back button, logo and tagline, white rounded card (28dp radius), filled/outlined input fields with leading icons, password show/hide toggle, primary ink pill button with arrow, inline errors on `TextInputLayout`, Snackbar for invalid credentials, and navigation to `RegisterActivity`.
  - `RegisterActivity` with back button & logo top bar, headline "Tạo tài khoản mới", inputs for Họ và tên, Email, Số điện thoại (`+84` prefix text), Mật khẩu (toggle), Nhập lại mật khẩu (toggle), MaterialCheckBox for terms, ink pill button "Tạo tài khoản", inline field errors, focus-loss validation listeners, and auto-session creation navigating to `MainActivity` (clearing back stack).
  - `AuthController` managing authentication business rules (`login` by email or phone, `register` with `ROLE_USER`, field error IDs, SHA-256 password hashing, session saving via `SessionManager`).
  - `UiUtils` providing `setupHideKeyboardOnTouch` for hiding soft keyboard when tapping outside input fields.

---

## Task 4: Home (chips, grid, search, ProductAdapter, mini-cart) & UI Polish
- **Status**: DONE
- **Completed Items**:
  - `MainActivity` shell hosting `FragmentContainerView` and `BottomNavigationView` with 4 tabs (Trang chủ, Giỏ hàng, Đơn hàng, Cá nhân). Tab state preserved across rotation (`KEY_SELECTED_TAB`).
  - BadgeDrawable on Cart tab showing total cart count (hidden when 0) and helper methods `updateCartBadge()` & `navigateToTab(menuId)`.
  - Controllers & UI Polish (search bar CardView with 2dp elevation, category chips with 0dp stroke, bottom nav active orange pill indicator `#FFE9D2`, neutral product placeholder, Poppins font integration, Title Case name normalization).

---

## Task 5: Product detail + Cart
- **Status**: DONE
- **Completed Items**:
  - Created `CartSummary` model and extended `CartController` with `addToCart`, `updateQuantity`, `removeItem`, `restoreItem`, `validatePromo` (`SNACK10`), and `computeSummary`.
  - Created `ProductDetailActivity` with soft warm gradient background, top bar with back button & cart icon button with badge, large product image in a 28dp rounded white card frame, white bottom sheet (32dp top corners), tag pill, name, category, red price (`#E53935`), strikethrough original price, favorite heart toggle, quantity selector (`-` / `+`), live total price, and "Thêm vào giỏ hàng" button with Snackbar action.
  - Implemented `CartFragment` with item list, quantity stepper, separate trash removal button with undo Snackbar, promo code section (`SNACK10` = 10% off subtotal), price summary, checkout button, and empty state view.

---

## Task 6: Checkout + Place Order + Order Success
- **Status**: DONE
- **Completed Items**:
  - `OrderController`: Implemented `placeOrder`, `getOrders`, `getOrderDetail`, `cancelOrder`, and `reorder`.
  - `CheckoutActivity`: Delivery address validation (name, phone `+84`, address min 5 chars), payment method radio cards (COD Cash, Bank, E-Wallet), order summary, and order confirmation.
  - `OrderSuccessActivity`: Animated check circle vector icon, order code rounded chip, delivery estimation, "Xem đơn hàng", and "Về trang chủ" buttons.

---

## Task 7: Profile + Orders History + Logout
- **Status**: DONE
- **Completed Items**:
  - `UserController`: `getCurrentUser()`, `updateProfile(...)`, `changePassword(...)`, `logout()`.
  - `OrdersFragment`: Filter chips, `OrderAdapter` with status chips, empty state, click navigation.
  - `OrderDetailActivity`: Status timeline, snapshot items list, receiver info, summary, cancel & reorder buttons.
  - `ProfileFragment`: Circular avatar with 28dp camera badge, photo picker, info card, menu rows card, logout dialog.
  - `EditProfileActivity` & `ChangePasswordActivity`.

---

## Task 8: Multi-Image Support per Product & Admin Product Management
- **Status**: DONE
- **Completed Items**:
  - Created 13 asset folders under `app/src/main/assets/products/<folder_name>/`.
  - Created `docs/IMAGES_HOWTO.md` in Vietnamese explaining image placement, natural filename sorting, cover selection, and sync rules.
  - Created `product_images` table (`id`, `product_id`, `path`, `sort_order`) and `ProductImageDao`. Updated `DbConstants.DB_VERSION = 15` with incremental `CREATE TABLE IF NOT EXISTS` migration in `onUpgrade()`.
  - Implemented `ProductImageSyncer` scanning `assets/products/<folder>` in background thread at app start, sorting images naturally, and syncing `product_images` idempotently while preserving admin file rows.
  - Updated `Product` model with `List<String> images` and `getCoverImage()`. Updated `ProductDao` to eager-load image lists in bulk.
  - Updated `ImageUtils` to handle `"asset:products/..."` (Glide `file:///android_asset/`), absolute paths (`/storage/...`), resource names, and fallback placeholders.
  - Upgraded `ProductDetailActivity` with ViewPager2 swipeable gallery (`ProductDetailGalleryAdapter`), page indicator dots, horizontal thumbnail strip (`ProductDetailThumbnailAdapter`), and full-screen image viewer (`DialogFullScreenImageBinding`).
  - Upgraded `AdminProductFormActivity` with multi-image picking (`MAX_PRODUCT_IMAGES = 6`), horizontal strip of image thumbnails (`AdminFormImageAdapter`), cover star selection, and individual image removal.
  - Removed redundant root `pic/` folder and cleaned up all temporary images related to `pic/`. Retained only actual user-uploaded product images in `app/src/main/assets/products/` (`drink_probi_dua`, `candy_custas_tiramisu`, `snack_que_dorkbua`, `candy_banh_su_kem_pho_mai`, `snack_pate_chaud`, `snack_toonies_ga_ran`, `drink_sting_dau`, `candy_hershey_kisses`, `snack_lays_cheddar`, `snack_mi_enaak_gemez`, `candy_oreo_red_velvet`, `drink_kirin_imuse`, `snack_khoai_tay_pho_mai`).
  - Configured detailed Vietnamese product descriptions in `DatabaseHelper` (seed and migrations: v16 for Probi, v17 for Custas Tiramisu, v18 for Dorkbua Lotus, v19 for Bánh su kem phô mai GO!, v20 for Bánh Pâté Chaud, v21 for Snack Toonies vị gà rán, v22 for Nước tăng lực Sting hương dâu, v23 for Socola sữa hạnh nhân Hershey's Kisses, v24 for Snack khoai tây Lay's Phô Mai Cheddar, v25 for Snack mì Gemez Enaak Extra, v26 for Bánh Oreo Socola Pie Red Velvet, v27 for Nước uống Kirin iMUSE sữa chua & chanh, v28 for Khoai tây phô mai O'Star) and polished `activity_product_detail.xml` description heading.
  - Added actual product images for all 13 seeded products: single-image for `candy_custas_tiramisu`, `snack_que_dorkbua`, `snack_pate_chaud`, `snack_toonies_ga_ran`, `drink_sting_dau`, `candy_hershey_kisses`, `candy_oreo_red_velvet`, `drink_kirin_imuse`, and multi-images for `candy_banh_su_kem_pho_mai` (2), `drink_probi_dua` (2), `snack_lays_cheddar` (2), `snack_mi_enaak_gemez` (2), and `snack_khoai_tay_pho_mai` (3). All 13 products now have 100% complete real image galleries and authentic product descriptions!

---

## Task: Parts A, B, C (White Background Blending, Richer Product Data, Product Detail Redesign)
- **Status**: DONE
- **Completed Items**:
  - **Part A - Product Photos With White Backgrounds**:
    - Created custom view `BlendImageView` in `view/widget/` with `fitCenter`, configurable padding ratio (default 10%), corner radius clipping, `PorterDuff.Mode.MULTIPLY` blending over pastel card background, `ColorMatrix` brightness scaling (1.06x), and soft shadow ellipse (8% alpha, 60% width).
    - Auto-detection: samples 8 boundary points (4 corners + 4 edge middles) in a 5x5 patch; if all R, G, B >= 235, automatically blends; cached with `LruCache`.
    - Integrated `BlendImageView` across Home (`item_product.xml`), Cart (`item_cart.xml`), Checkout summary (`item_checkout_summary_row.xml`), Mini-cart (`fragment_home.xml`), Admin product list (`item_admin_product.xml`), and Admin form thumbnails (`item_admin_form_image.xml`). Rotating pastel backgrounds: peach `#FFE9E4`, yellow `#FFF8CC`, blue `#E8F3FF`, pink `#FFE8F1`. Full-screen image dialog retains pure white background without blending.
  - **Part B - Richer Product Data**:
    - Verified `docs/PRODUCTS.md` (missing, so new columns left empty for now as specified without inventing data).
    - Added 10 columns to `products` table: `origin`, `made_in`, `size_info`, `ingredients`, `features`, `usage_guide`, `storage_guide`, `shelf_life` (TEXT), `rating` (REAL default 0), `sold_count` (INTEGER default 0).
    - Bumped `DbConstants.DB_VERSION = 29` with incremental safe `ALTER TABLE ... ADD COLUMN` migration in `DatabaseHelper.onUpgrade()`.
    - Incremented `sold_count` by ordered quantity within the same SQLite transaction in `OrderDao.createOrder()`.
    - Implemented `FavoriteManager` over `SharedPreferences` with key `fav_user_<userId>`, synchronized between Home and Product Detail.
    - Fixed Admin edit bug: preserved original `image` folder key when editing products, only saving admin-picked files in `product_images`. Added optional fields to `AdminProductFormActivity`.
  - **Part C - Product Detail Screen**:
    - Hero section (~42% height): dynamic category gradient (`bg_hero_gradient_snack`, `drink`, `candy`), soft white radial glow (`bg_illustration_glow`), floating translateY 6dp loop animation (2.5s), fade/scale-in entrance animation.
    - Top circular action buttons: Back, Favorite (synced via `FavoriteManager`), and Cart with bounce animation and live badge count.
    - White content sheet (32dp top corners, overlapping hero by 24dp): tag pill, category, product name (24sp bold, max 2 lines), horizontal info chips (rating star, sold count, "15 phút", size info), price row with strikethrough original price and "-xx%" discount badge.
    - Expandable description with 3-line max and "Xem thêm / Thu gọn" toggle.
    - "Thông tin chi tiết" card dynamically displaying non-empty specification rows with 3-line toggle for long values.
    - "Cam kết từ SnackGo" card with 3 icons (Giao nhanh 15 phút, Tươi mới mỗi ngày, Đổi trả dễ dàng).
    - "Có thể bạn cũng thích" horizontal list of up to 6 same-category products with direct tap navigation and `+` add-to-cart button.
    - Sticky bottom bar with quantity stepper, outlined pill button ("Thêm • <total>"), and ink pill button ("Mua ngay" which adds to cart and opens Checkout).
    - Full screen rotation state preservation and 360dp width responsive layout.
  - **Onboarding UI Polish & Illustration Enhancement**:
    - Designed 3 vibrant custom vector illustrations matching each slide's theme:
      - `il_onboarding_snacks.xml`: Snack bag bursting with chips, soda cup with straw, chocolate cookie, and golden sparkles ("Khám phá món ngon mỗi ngày").
      - `il_onboarding_order.xml`: Smartphone displaying shopping cart with food, success checkmark badge, and touch ripple gesture ("Đặt hàng chỉ với vài chạm").
      - `il_onboarding_delivery.xml`: Delivery scooter with SnackGo rear thermal food box, speed motion lines, and 15-minute badge ("Giao nhanh, tiện lợi").
    - Replaced reflective drawable resolution with direct resource IDs (`R.drawable.il_onboarding_*`) in `OnboardingActivity.java`.
    - Added gentle floating animation (`translationY` 8dp, 2.4s cycle) for the illustrations.
    - Updated `bg_onboarding_gradient.xml` with warm, appetizing amber-to-orange gradient (`#FFB300` -> `#FF8A00` -> `#FF7000`).
    - Handled `WindowInsets` for `bottomSheet` to add dynamic bottom padding based on system navigation bar insets, preventing gesture/navigation bars from clipping text or buttons.
    - Balanced spacing, title typography (22sp bold), description line spacing, and skip button aesthetics.

---

## Task 9A: Home UI Polish
- **Status**: IN PROGRESS
- **Completed Items**:
  - Reduced product card visual height with a responsive image ratio, tighter spacing, and a subtle divider stroke.
  - Replaced long product descriptions in the Home grid with localized category labels for faster scanning.
  - Increased bottom list padding so the floating mini-cart does not cover the final products.
  - Moved mini-cart secondary text color into the resource palette.
  - Added consistent divider strokes and safe bottom spacing to the Cart cards.
  - Replaced remaining inline success and muted-icon colors in Cart, Checkout, Orders, and Admin layouts with shared color resources.
  - Increased Product Detail action targets and quantity controls for comfortable tapping.
  - Localized the Product Detail category label instead of showing internal values such as `candy`.
  - Removed unused imports and simplified ProductAdapter content comparison with `Objects.equals`.
  - Centralized pastel card and hero background colors in resources, removing duplicate hardcoded palettes from adapters and Product Detail.
  - Moved discount formatting and default quantity text into Vietnamese string resources.
  - Added subtle borders to Product Detail, Checkout, and Profile cards.
  - Increased bottom content spacing and status chip touch areas in Checkout, Orders, and Profile.
  - Localized comments in the polished UI and adapter files into clear Vietnamese with lowercase sentence starts.
  - Simplified Checkout payment cards by removing secondary description text and unused string resources.
  - Made the Home avatar navigate directly to the Profile tab.
  - Replaced the Home notification icon button with a centered 48dp card and bounded ripple effect.
  - Reduced the visual size of Home header icons while keeping the header actions tappable.
  - Added a runtime AppBarLayout sticky header: the greeting collapses while search and category controls stay pinned during scrolling.
  - Unified the AppBar, greeting, search, and category backgrounds to one shared surface color so no gradient seams appear.
  - Added a shadow transition when the sticky header becomes active.
  - Tightened the product image ratio so the Home grid shows more products per scroll.
  - Added a fade transition when switching the main bottom-navigation tabs.
  - Added a Home loading indicator and a separate product-load error state with retry copy.
  - Moved Home product queries off the UI thread and shut down the executor with the fragment view lifecycle.
- **Validation**:
  - `:app:assembleDebug` passes successfully.
  - Verified the Home sticky search/category header and compact action icons on the emulator.

---

## Remaining Tasks
- [ ] **Task 9**: Polish, error/empty states, testing, final README

## Repository Setup
- Initialized the Git repository on the `main` branch.
- Added Android-specific `.gitignore` rules and the project `README.md` before publishing.

---

## Known Issues
- None.
