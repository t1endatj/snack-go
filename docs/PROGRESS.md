# SnackGo Implementation Progress

## Project Status Overview
- **Architecture**: MVC (Model - View - Controller), SQLite via SQLiteOpenHelper, Material Design 3.
- **Language**: Java 11 / Java 17.
- **Database Version**: 30 (`snackgo.db`).
- **Latest Build Status**: `:app:assembleDebug` SUCCESSFUL.

---

## Completed Tasks

### Task 1: Foundation
- Gradle configuration, Material 3 themes, color palette, Poppins font styles, shapes (24dp card corners).
- Constants (`AppConstants`, `DbConstants`, `IntentKeys`, `PrefConstants`).
- `SessionManager`, `PriceFormatter` (VND formatting).

### Task 2: Models & Database
- Data models (`User`, `Product`, `CartItem`, `OrderItem`, `Order`).
- `DatabaseHelper` (singleton SQLiteOpenHelper, foreign keys, 5 tables, seed data for 1 admin and 13 products).
- DAOs: `UserDao`, `ProductDao`, `ProductImageDao`, `CartDao`, `OrderDao`.
- Security: `PasswordUtils` with SHA-256 + 16-byte random salt.

### Task 3: Onboarding & Auth
- `SplashActivity`: session & onboarding routing, image syncing.
- `OnboardingActivity`: 3 ViewPager2 slides with custom vector illustrations, dot indicator.
- `LoginActivity`: email or phone login, password toggle, inline validation.
- `RegisterActivity`: regex validations, terms checkbox.
- `AuthController` & `UserController`.

### Task 4: Home Catalog & Discovery
- `MainActivity` & `HomeFragment`: 2-column product grid, search-as-you-type, category chip filters (Tất cả, Snack, Nước uống, Bánh kẹo).
- Floating Mini-Cart bar dynamically shown when cart is not empty.
- `FavoriteManager` for local wishlist toggling.

### Task 5: Product Detail & Cart
- `ProductDetailActivity`:
  - ViewPager2 gallery with multi-angle images, page indicator, horizontal thumbnail strip.
  - Full-screen pinch-to-zoom image viewer dialog.
  - Clean product narrative description with expandable toggle.
  - 2-column Key-Value Specification Card (`cardProductSpecs`) dynamically parsing rich attributes (Origin, Ingredients, Shelf life, Storage, etc.) with 3-line collapse/expand.
  - Related products horizontal recommendation strip.
- `CartFragment`: item quantity steppers (+/-), item removal, coupon `SNACK10` (10% discount), free shipping threshold logic.

### Task 6: Checkout & Orders
- `CheckoutActivity`: delivery address & phone prefilling, payment methods (COD, Bank Transfer, E-wallet).
- Atomic order placement with SQLite Transactions and snapshot pricing in `order_items`.
- `OrderSuccessActivity` with generated code `SGyyMMddxxxx`.

### Task 7: Profile & History
- `ProfileFragment`: user info, navigation to orders, edit profile, change password, admin entry point.
- `EditProfileActivity` & `ChangePasswordActivity`.
- `OrdersFragment` & `OrderDetailActivity`: order status tracking chips (Chờ xác nhận, Đang giao, Đã giao, Đã hủy).

### Task 8: Admin Product Management
- `AdminProductListActivity`: admin product catalog view, search, soft delete.
- `AdminProductFormActivity`: product creation & update form, multi-image picker with cover star selection.

### Recent Fixes & Improvements
- **Product Detail Specifications**: Implemented smart extraction of structured specifications (`• [Tên]: [Giá trị]`) into the Material Card Key-Value Table (`cardProductSpecs`), keeping the general description clean and readable.
- **Category Filter Correction**: Corrected category of "Bánh Pâté Chaud nhân thịt" from `snack` to `candy` (`AppConstants.CATEGORY_CANDY`) in seed data and database migration v30, ensuring it displays under "Bánh kẹo" rather than "Snack".
- **Order Thumbnail Image Mapping**: Corrected `OrderDao` to use `product.getCoverImage()` and updated `OrderAdapter` to load thumbnails via `ImageUtils.loadProductImage()`, resolving image mismatch in order history and synchronizing `snack_que_dorkbua.jpg`.
- **Cart & Checkout Product Image Attachment**: Updated `CartDao.getItems()` to attach images from `ProductImageDao`, ensuring `getCoverImage()` returns the authentic product asset in Cart and Checkout, and synchronized `candy_hershey_kisses.jpg`.
- **Vietnamese Phone Regex Validation**: Tightened phone number regex across `AuthController`, `UserController`, and `OrderController` with `AppConstants.PHONE_REGEX` to strictly validate Vietnamese carrier prefixes (`03`, `05`, `07`, `08`, `09` or `3`, `5`, `7`, `8`, `9` with country code `+84`).
- **Search & Filter Accuracy**: Removed long product descriptions from search matching in `ProductController` and `ProductDao.searchActive` (preventing false matches on brand boilerplate like "các loại bánh kẹo" in snack descriptions), and matched against normalized product name, tag, and Vietnamese category keywords.
- **Product Detail Cart Badge Clipping**: Moved `tvCartBadge` outside the circular `MaterialCardView` (`btnCart`) into a shared `FrameLayout` with `elevation="4dp"` and a dedicated circular/pill `bg_cart_badge` drawable so the badge is never clipped by the card's corner radius.
- **Selective Cart Checkout**: Added item selection checkboxes (`cbSelectCartItem`) and a "Chọn tất cả" option (`cbSelectAll`) in `CartFragment` & `CartAdapter` (unselected by default), filtering summary calculations, `CheckoutActivity`, `OrderController`, and `OrderDao` so only selected items are ordered and removed from the cart.
- **Favorite Product Filter Chip**: Added a "Yêu thích" filter chip (`chipFavorite`) to `HomeFragment` so users can filter and browse their hearted products directly from the Home screen, with live removal when un-hearted and a dedicated empty state.

---

## Known Issues
- None. Build passes cleanly with zero errors.

## 2026-11-25 - Pull origin/main

### Đã làm
- Đọc tổng quan code và kiểm tra các nhánh remote; cả hai nhánh `fix/category-and-product-specs` và `fix/ui-and-cart-selection` đã được gộp vào `main`.
- Chạy `git pull --ff-only origin main`, cập nhật local `main` từ `88c3676` lên `e9d4e6d` (13 commit).
- Xác nhận `HEAD` và `origin/main` đồng bộ, không phát sinh merge commit.
- Build bản vừa pull bằng `:app:assembleDebug`: BUILD SUCCESSFUL.
- Giữ bản ghi kiểm tra local trước đó trong `.git/PROGRESS-before-pull-f021a7a72d9d43a893f7ad47aae73612.md` để tránh trùng file khi pull.
- Giữ nội dung tiến độ từ remote và bổ sung bản ghi này; không sửa mã nguồn ngoài các thay đổi được pull.

### Chưa làm
- Chưa kiểm thử ứng dụng trên thiết bị hoặc máy ảo.

### Vấn đề đã biết
- Gradle cảnh báo tùy chọn `android.overridePathCheck=true` đang ở trạng thái thử nghiệm.

## 2026-11-25 - Hoàn tác sửa backend

- Đã hoàn tác 20 file mã nguồn/tài nguyên về HEAD `e9d4e6d`, xóa `PhoneUtils.java` và `BackendRegressionTest.java` vừa tạo.
- Giữ bản code đã pull từ `origin/main` và bản ghi pull trước đó; chưa commit/push.
- File còn thay đổi local: `docs/PROGRESS.md` (bản ghi pull và hoàn tác).
- Build lại sau hoàn tác: `:app:assembleDebug` BUILD SUCCESSFUL; mã nguồn khớp HEAD.
- Chưa cài lại APK lên máy ảo; không thay đổi dữ liệu ứng dụng trên máy ảo.

## 2026-11-25 - Sửa backend gọn và tách hàm

### Đã làm
- Tách `placeOrder` thành kiểm tra giao hàng, lọc món, tạo đơn và snapshot; tách `createOrder` thành map dữ liệu và lưu chi tiết; tách map giỏ hàng, kiểm tra giá admin, resize ảnh và đọc danh sách asset.
- Gộp lưu sản phẩm/ảnh trong transaction; bỏ tìm kiếm SQL trùng với ProductController, hàm xóa giỏ không có nơi gọi và các hàm quản lý ảnh/session không còn dùng.
- Chặn số lượng <= 0, tràn số và thêm sản phẩm đã xóa; tăng số lượng trong transaction để tránh mất cập nhật.
- Đặt hàng dùng transaction và xóa có điều kiện theo số lượng/giá hiện tại để chặn đặt trùng hoặc snapshot cũ; chỉ xóa món đã chọn. Hủy đơn chỉ áp dụng cho chủ đơn khi PENDING.
- Kiểm tra chủ tài khoản và quyền admin ở controller; session chỉ giữ id, role và cờ, đọc hồ sơ từ SQLite, giữ yêu thích khi đăng xuất. Checkout không ghi đè hồ sơ người mua.
- Giữ thứ tự ảnh bìa khi lưu/sync, giữ file ảnh khi soft delete/khôi phục, bảo toàn sold_count khi sửa sản phẩm. Mô tả bắt buộc, giới hạn 2000 ký tự để lưu được mô tả mẫu hiện có.
- Mã trong `app/src/main` giảm ròng 92 dòng so với HEAD; giữ database v30 và các model hiện tại, không thêm thư viện.
- Thêm một file kiểm thử tập trung cho các luồng thay đổi.

### Xác minh
- `:app:assembleDebug :app:connectedDebugAndroidTest`: BUILD SUCCESSFUL; 11 tests, 0 failures, 0 errors, 0 skipped trên máy ảo API 37.
- Cài APK debug, mở lạnh thành công đến OnboardingActivity và xác nhận tiến trình vẫn chạy.
- `git diff --check` qua; không sửa model, schema/database version hoặc Gradle. Chưa commit/push.

### File tạo / sửa
- Tạo: `app/src/androidTest/java/com/linh/snackgo/BackendSmokeTest.java`.
- Sửa trong `app/src/main/java/com/linh/snackgo/controller/`: `AdminProductController.java`, `AuthController.java`, `CartController.java`, `OrderController.java`, `UserController.java`.
- Sửa trong `app/src/main/java/com/linh/snackgo/data/`: `CartDao.java`, `OrderDao.java`, `ProductDao.java`, `ProductImageDao.java`.
- Sửa trong `app/src/main/java/com/linh/snackgo/utils/`: `ProductImageSyncer.java`, `SessionManager.java`.
- Sửa: `app/src/main/java/com/linh/snackgo/constants/AppConstants.java`, `app/src/main/res/values/strings.xml`, `docs/PROGRESS.md`.

### Chưa làm / vấn đề còn lại
- Chưa kiểm tra toàn bộ giao diện, thiết bị thật hoặc API 24.
- Schema v30 chưa lưu tên người nhận riêng theo đơn; giao diện chi tiết đơn vẫn hiển thị tên tài khoản.
- Cấu hình font Poppins từ bản gốc còn lỗi lint đã biết; lần này giữ phạm vi backend.

## 2026-11-25 - Sửa chú thích

- Đã đổi 36 chú thích trong 13 file sang tiếng Việt có dấu, chữ đầu viết thường và ngắn gọn; giữ nguyên logic và nội dung chuỗi hiển thị.
- File sửa: `AppConstants.java`; 5 controller và 4 DAO ở mục trên; `ProductImageSyncer.java`, `BackendSmokeTest.java`, `strings.xml`, `docs/PROGRESS.md`. Không tạo file mới.
- Đã đối chiếu nội dung ngoài chú thích trước/sau để xác nhận không đổi mã thực thi.
- Build debug thành công; không chạy lại kiểm thử nghiệp vụ vì chỉ sửa chú thích. Các vấn đề đã biết giữ như mục trên.

## 2026-11-25 - Phát hành lên main

- Chuẩn bị commit toàn bộ phần sửa backend và chú thích theo yêu cầu; nhánh đích là `origin/main`.
- Fetch xác nhận local/remote cùng `e9d4e6d`; build debug lại thành công. Kết quả 11 kiểm thử nghiệp vụ và các hạn chế vẫn như trên.
- Phạm vi gồm 15 file: danh sách mã nguồn, tài nguyên, kiểm thử và tài liệu đã ghi ở các mục trước; không tạo thêm file.
