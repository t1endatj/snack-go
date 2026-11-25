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

---

## Known Issues
- None. Build passes cleanly with zero errors.
