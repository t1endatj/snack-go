# Hướng dẫn Thêm Ảnh Sản phẩm (Multi-Image Support)

Ứng dụng SnackGo hỗ trợ hiển thị từ 1 đến nhiều hình ảnh thực tế cho mỗi sản phẩm thông qua thư mục Assets hoặc giao diện Quản lý Admin.

## Hướng dẫn thêm ảnh vào Assets

1. **Vị trí thư mục**:
   Tất cả hình ảnh của từng sản phẩm được đặt trong thư mục tương ứng tại:
   `app/src/main/assets/products/<folder_name>/`

   *Các tên thư mục trùng với tên trường `image` của sản phẩm trong CSDL:*
   - `snack_khoai_tay_pho_mai`
   - `candy_oreo_red_velvet`
   - `snack_mi_enaak_gemez`
   - `snack_lays_cheddar`
   - `drink_kirin_imuse`
   - `drink_probi_dua`
   - `candy_banh_su_kem_pho_mai`
   - `snack_pate_chaud`
   - `candy_custas_tiramisu`
   - `snack_que_dorkbua`
   - `snack_toonies_ga_ran`
   - `drink_sting_dau`
   - `candy_hershey_kisses`

2. **Quy tắc đặt tên file & Thứ tự hiển thị**:
   - Tên file chấp nhận các định dạng mở rộng: `.jpg`, `.jpeg`, `.png`, `.webp`.
   - Các file trong thư mục được sắp xếp theo tên file (Natural Sort Order).
   - **Ảnh đầu tiên (file có tên xếp đầu tiên)** sẽ được tự động chọn làm **Ảnh bìa (Cover Image)** hiển thị ở danh sách sản phẩm, giỏ hàng và đơn hàng.
   - *Mẹo:* Đặt tên file là `1.jpg`, `2.jpg`, `3.jpg`, ... để chủ động điều khiển thứ tự hiển thị.

3. **Kích hoạt thay đổi**:
   - Sau khi thả/thêm các hình ảnh vào thư mục tương ứng, bạn chỉ cần mở lại ứng dụng (SplashActivity).
   - Hệ thống `ProductImageSyncer` chạy ngầm sẽ tự động quét và đồng bộ danh sách ảnh vào ứng dụng.
