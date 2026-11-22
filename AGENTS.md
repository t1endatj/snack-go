# SnackGo - Project Spec (read this file fully before every task)

## 1. Overview
SnackGo is a small but complete Android app for ordering snacks and drinks.
Roles: USER (browse, cart, order, profile) and ADMIN (full CRUD on products).
Goal: a course project (mobile programming) that must look polished (Material Design 3) and be cleanly structured (MVC).

## 2. Hard constraints
- Language: JAVA only. No Kotlin, no Jetpack Compose. XML layouts + ViewBinding.
- Package: com.linh.snackgo. minSdk 24. Material Components (Material 3).
- Architecture: MVC.
  model/ (plain Java classes), view/activity/, view/fragment/, view/adapter/,
  controller/ (business logic), data/ (SQLite helper + DAOs), utils/, constants/
- Storage: SQLite through SQLiteOpenHelper (no Room). Session: SharedPreferences through SessionManager.
- Lists: RecyclerView + Adapter + ViewHolder. Images: Glide.
- No hardcoded strings in code. Use constants for: DB name/version, table and column names, SharedPreferences keys, Intent extra keys, roles, order statuses, categories. User-facing text goes in res/values/strings.xml (Vietnamese). Colors, dimens, styles go in resources, never inline.
- Views/Fragments only handle UI. Business rules live in controller/, SQL lives in data/.
- Passwords: store SHA-256 + random salt, never plain text. SharedPreferences stores only userId, role and flags.
- Prices are integers in VND, formatted by PriceFormatter (22000 -> "22.000đ").
- Close cursors and DB objects properly (try-with-resources). Avoid slow work on the main thread.

## 3. Database schema (constants in DbConstants)
- users(id PK, full_name, email UNIQUE, phone, password_hash, salt, address, avatar_path, role, created_at)
- products(id PK, name, description, category, price, original_price, tag, image, is_active, created_at, origin, made_in, size_info, ingredients, features, usage_guide, storage_guide, shelf_life, rating, sold_count)
  - category values: "snack", "drink", "candy" (constants)
  - image: a drawable resource name (seeded) OR an asset folder OR an absolute file path (added by admin)
  - is_active: 1 visible, 0 soft-deleted
- product_images(id PK, product_id FK ON DELETE CASCADE, path TEXT NOT NULL, sort_order INTEGER NOT NULL DEFAULT 0)
  - path values: "asset:products/<folder>/<file>" OR absolute file path
- cart_items(id PK, user_id FK, product_id FK, quantity) - UNIQUE(user_id, product_id)
- orders(id PK, user_id FK, order_code, total_amount, discount_amount, delivery_fee, payment_method, delivery_address, phone, status, created_at)
  - status constants: PENDING, DELIVERING, COMPLETED, CANCELLED
- order_items(id PK, order_id FK, product_id, product_name, unit_price, quantity)  <- snapshot of name and price at order time
- Seed on first create: 1 admin (admin@snackgo.com / Admin@123, role ADMIN) and 13 products with Vietnamese names, descriptions, categories and VND prices.

## 4. Screens and behavior
1. Splash/Onboarding: 3 pages (ViewPager2) with "Bắt đầu" and "Bỏ qua". Show once (KEY_ONBOARDING_DONE). Sync asset product images in background thread.
2. Login: email or phone + password, show/hide password, validation errors under fields. On success save session and go to Main. If already logged in, skip to Main.
3. Register: full name, email, phone (+84 prefix), password, confirm password, terms checkbox. Validate: required, email format, phone 9-10 digits, password >= MIN_PASSWORD_LENGTH, confirm matches, email not already used, terms checked.
4. Main (MainActivity + BottomNavigationView, 4 tabs: Home, Cart, Orders, Profile):
   - Home: greeting with user name, search bar (filter by name as the user types), category chips (All/Snack/Drink/Candy), 2-column grid of product cards (pastel background per card, image in rounded white frame, tag badge, name up to 2 lines, price, strikethrough original price, favorite icon, round "+" button that adds 1 to cart). Floating mini-cart bar above bottom nav when the cart is not empty ("Giỏ hàng • N món  total" + "Xem giỏ"). Empty state for no results.
   - Cart tab: list with image, name, price, quantity stepper (-/+), remove; promo code field (code SNACK10 = 10% off, validated), summary (subtotal, delivery fee, discount, total), "Đặt hàng" button. Empty state when empty. Badge on the tab shows total items.
   - Orders tab: list of the current user's orders (code, date, item count, total, status chip with color). Tap to see order detail (items, address, payment). Empty state.
   - Profile tab: avatar, name, email; rows: personal info (edit name, phone, address), my orders, logout (confirm dialog). If role is ADMIN also show "Quản lý sản phẩm".
5. Product detail: swipeable ViewPager2 image gallery, page dots indicator, horizontal thumbnail strip (2+ images), full-screen pinch-to-zoom viewer dialog, name, tag, description, quantity selector, total price, "Thêm vào giỏ hàng".
6. Checkout: delivery address and phone (prefilled from profile, editable), payment method (cash / bank transfer / e-wallet radio cards), order summary, "Xác nhận đặt hàng". On confirm: create order + order_items (snapshot), clear cart, go to success screen.
7. Order success: order code, buttons "Xem đơn hàng" and "Về trang chủ".
8. Admin - product management (only ADMIN, guard in code too):
   - List screen: RecyclerView of ALL active products (thumbnail, name, price, category), search, FAB "+" to add, edit button, delete button (confirm dialog).
   - Add/Edit form (one Activity for both; EXTRA_PRODUCT_ID present = edit mode): name, price, original price (optional), category dropdown, description, tag (optional), multi-image picker with cover star selection, horizontal strip of image thumbnails with remove button. Validate required fields, price > 0.
   - Delete = soft delete (is_active = 0) and remove that product from all carts.
   - After add/edit/delete, Home reflects changes when shown again.

## 5. Business rules
- Cart belongs to the logged-in user. Adding an existing product increases quantity. Quantity min 1 (use remove to delete). Max per item = MAX_QUANTITY_PER_ITEM.
- Delivery fee: DEFAULT_DELIVERY_FEE, free when subtotal >= FREE_DELIVERY_THRESHOLD (both constants).
- Promo SNACK10: 10% off subtotal, one code supported (constant).
- Order code format: "SG" + yyMMdd + 4 digits (unique).
- Only ADMIN can open admin screens; normal users must never reach them.

## 6. Design system (follow strictly)
- Colors: ink #1C1B1F (primary buttons, selected chips), orange #FF8A00 (accent), amber #FFB300, red #E53935 (prices, favorite, errors), background #FAF8F5, surface #FFFFFF, secondary text #6B6B6B, divider #EEEBE6.
- Pastel card backgrounds (rotate): #FFE9E4 peach, #FFF8CC yellow, #E8F3FF blue, #FFE8F1 pink.
- Font Poppins. Cards radius 24dp, pill buttons and chips fully rounded, buttons min height 52dp, touch targets >= 48dp, soft elevation 2-4dp.
- Primary button: ink background, white text, pill shape, trailing arrow icon where the design shows it.
- Every list needs an empty state; every input needs an error state; use Snackbar/Toast for feedback and a confirm dialog for destructive actions.
- Support small screens (360dp wide) with no clipped text.

## 7. Way of working
- Do ONLY the task I ask in the current message. Do not add features, screens or libraries I did not request.
- The project must build and run after every task. Fix compile errors before finishing.
- At the end of every task: list the files created/changed, and update docs/PROGRESS.md (done / not done / known issues).
- If something in this spec is ambiguous or conflicts, ask me instead of guessing.
- Keep code readable: short methods, meaningful names, Javadoc on controllers and DAOs.

## 8. Task order (I will give one package at a time)
1 Foundation (gradle, theme, constants, SessionManager, PriceFormatter)
2 Models + DatabaseHelper + DAOs + seed
3 Onboarding, Login, Register, AuthController
4 Home (chips, grid, search, ProductAdapter, mini-cart)
5 Product detail + Cart
6 Checkout + orders + success
7 Profile + order history + logout
8 Admin product CRUD
9 Polish, error/empty states, testing, final README
