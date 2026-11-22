package com.linh.snackgo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.linh.snackgo.constants.AppConstants;
import com.linh.snackgo.constants.DbConstants;
import com.linh.snackgo.utils.PasswordUtils;

/**
 * SQLite database helper singleton.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String SEED_PROBI_DESCRIPTION = "Probi là dòng sản phẩm đến từ thương hiệu sữa Vinamilk - với hệ thống nhà máy hiện đại và trang bị công nghệ tiên tiến - luôn mang đến cho bạn những giải pháp dinh dưỡng chất lượng quốc tế.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Probi (Việt Nam)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 65ml x 5 chai\n"
            + "• Thành phần: Nước, đường tinh luyện, sữa bột (3.3%), xirô fructoza, chất ổn định (405, 466), hương dứa tổng hợp tự nhiên và giống tự nhiên dùng cho thực phẩm, probiotic Lactobacillus paracasei LCASEI 431, vitamin D3\n"
            + "• Đặc tính hàng hóa: Nên dùng 2 - 3 chai mỗi ngày sau bữa ăn\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp\n"
            + "• Hướng dẫn bảo quản: Bảo quản lạnh ở nhiệt độ 6 - 8°C\n"
            + "• Hạn sử dụng: 50 ngày kể từ ngày sản xuất\n"
            + "• Đơn vị chịu trách nhiệm: CÔNG TY CỔ PHẦN SỮA VIỆT NAM (VINAMILK) - Số 10 Tân Trào, P. Tân Mỹ, TP.HCM\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_CUSTAS_TIRAMISU_DESCRIPTION = "Orion là một thương hiệu thực phẩm nổi tiếng từ Hàn Quốc, chuyên sản xuất và phân phối các loại bánh kẹo và snack đa dạng. Các sản phẩm của Orion bao gồm bánh quy socola, bánh snack khoai tây, và các loại kẹo trái cây,... được biết đến với chất lượng cao và hương vị đa dạng, đem đến cho người tiêu dùng trải nghiệm thưởng thức đồ ngọt đậm đà và tiện lợi.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Custas (Hàn Quốc)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Quy cách: 281,4g (hộp 12 gói)\n"
            + "• Thành phần: Trứng, bột mì, đường, dầu thực vật, chất béo thực vật, mạch nha, maltose, chất nhũ hoá, chất làm ẩm, đường nâu, cồn thực phẩm, mạch nha ISMALTO OLIGO, bột kem sữa, bột sữa nguyên kem, bột phô mai, chất ổn định, bột cacao, bột whey, muối, chất tạo xốp, chất màu tạo tự nhiên, bột cà phê, hương liệu tổng hợp (kem phô mai, cà phê, sữa chua), hương liệu giống tự nhiên (cà phê, phô mai), hương liệu tự nhiên (kem), muối i-ốt, bột quế, chất tạo màu tổng hợp.\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo và thoáng mát, tránh ánh nắng trực tiếp.\n"
            + "• Hạn sử dụng: 10 tháng kể từ ngày sản xuất.\n"
            + "• Đơn vị chịu trách nhiệm: CÔNG TY TNHH THỰC PHẨM ORION VINA - Lô E-13CN, Đường NA3, KCN Mỹ Phước, Thị xã Bến Cát, Tỉnh Bình Dương.\n"
            + "• Lưu ý: Không nên sử dụng nếu dị ứng thành phần sản phẩm, sản phẩm có dấu hiệu ẩm mốc, hết hạn để tránh gây ảnh hưởng không tốt đến sức khỏe. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_DORKBUA_DESCRIPTION = "Dorkbua là một nhãn hiệu bánh nổi tiếng đến từ Thái Lan, chuyên cung cấp các sản phẩm bánh que ngon miệng và đậm đà. Bánh que Dorkbua có nhiều hương vị hấp dẫn, trong đó có các sản phẩm như bánh que vị Mala, là sự kết hợp giữa bột mì và gia vị lẩu.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Dorkbua (Thái Lan)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 110g\n"
            + "• Thành phần: Bột mì, đường, tinh bột sắn, dầu cọ, hương BBQ tổng hợp, chất điều vị, muối, xiro glucose, tiêu, ớt sa tế,...\n"
            + "• Đặc tính sản phẩm: Snack bim bim bánh tăm que cọng Dorkbua mang hương vị thơm ngon với từng chiếc que giòn xốp hấp dẫn tan chảy trong khóe miệng.\n"
            + "• Hướng dẫn sử dụng: Dùng ăn trực tiếp sau khi mở bao bì sản phẩm. Không dùng cho trẻ dưới 1 tuổi.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng mặt trời.\n"
            + "• Hạn sử dụng: 12 tháng kể từ ngày sản xuất.\n"
            + "• Đơn vị chịu trách nhiệm: CTY TNHH CƠ HỘI VÀ THÁCH THỨC - 60/26 Yên Thế, P.2, Q. Tân Bình, TP.HCM\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_SU_KEM_DESCRIPTION = "Bánh su kem phô mai GO! sở hữu lớp vỏ xốp mềm đặc trưng kết hợp cùng lớp bột phô mai phủ ngoài và nhân kem phô mai béo ngậy tan chảy, mang lại trải nghiệm đồ ngọt thượng hạng khó cưỡng.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu: GO!\n"
            + "• Trọng lượng / Dung tích: 180g (hộp 3 bánh)\n"
            + "• Hạn sử dụng: 1 ngày (dùng tươi ngon nhất trong ngày)\n"
            + "• Hướng dẫn bảo quản: Bảo quản trong ngăn mát tủ lạnh từ 2 - 8°C để giữ trọn độ tươi ngon của kem phô mai.\n"
            + "• Đơn vị chịu trách nhiệm: CÔNG TY TNHH DỊCH VỤ EB - 163 Phan Đăng Lưu, Phường 1, Quận Phú Nhuận, TP. Hồ Chí Minh, Việt Nam.\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_PATE_CHAUD_DESCRIPTION = "Bánh Pâté Chaud 80g sở hữu lớp vỏ ngàn lớp vàng ươm giòn xốp kết hợp cùng nhân thịt pâté thơm lừng đậm đà nóng hổi, là món ăn vặt thơm ngon lý tưởng nạp nhanh năng lượng.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Xuất xứ: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 80g\n"
            + "• Đặc tính sản phẩm: Vỏ bánh ngàn lớp nướng giòn rụm, nhân thịt pâté thơm ngon đậm đà.\n"
            + "• Hạn sử dụng: Trong 1 ngày kể từ ngày sản xuất (dùng ngon nhất khi còn nóng).\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp hoặc hâm nóng lại trước khi thưởng thức.\n"
            + "• Đơn vị chịu trách nhiệm: Công ty TNHH DV EB - 163 Phan Đăng Lưu, P.1, Q. Phú Nhuận, TP. Hồ Chí Minh.\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_TOONIES_DESCRIPTION = "Toonies là dòng sản phẩm snack khoai tây thuộc thương hiệu nổi tiếng Orion của Hàn Quốc, chuyên sản xuất và kinh doanh các loại bánh kẹo. Các sản phẩm của Orion bao gồm bánh quy socola, bánh snack khoai tây và các loại kẹo trái cây,... được biết đến với chất lượng cao và hương vị đa dạng, đem đến cho người tiêu dùng trải nghiệm thưởng thức đồ ngọt đậm đà và tiện lợi.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Toonies (Hàn Quốc)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 60g\n"
            + "• Thành phần: Bột bắp, dầu thực vật, bột gia vị gà 6.3% (đường, maltodextrin, muối, chất điều vị, hương liệu (tự nhiên, giống tự nhiên, tổng hợp), vảy rau ngò tây, chất điều chỉnh độ acid, chất chống đông vón), chất tạo màu tổng hợp, vảy khoai tây, bột gạo, chất chống oxy hóa.\n"
            + "• Đặc tính sản phẩm: Giòn tan với vị gà rán đậm đà thơm ngon khó cưỡng càng làm kích thích vị giác của bạn.\n"
            + "• Hướng dẫn sử dụng: Dùng ăn trực tiếp.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng mặt trời.\n"
            + "• Hạn sử dụng: 6 tháng kể từ ngày sản xuất.\n"
            + "• Đơn vị chịu trách nhiệm: CTY TNHH THỰC PHẨM ORION VINA - Lô E-13CN, Đường NA3, KCN Mỹ Phước, Thị xã Bến Cát, Tỉnh Bình Dương.\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_STING_DESCRIPTION = "Sting – một công thức chất lượng đến từ Tập đoàn PepsiCo, cung cấp các chất bổ sung năng lượng cho cơ thể như: Taurine, Inositol, Vitamin B, và Caffeine, mang đến những khoảnh khắc sảng khoái và bứt phá.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Sting (Mỹ)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 320ml (lon)\n"
            + "• Thành phần: Nước bão hòa CO2, chất điều chỉnh độ axit, hỗn hợp hương dâu tây tự nhiên, nhân sâm,...\n"
            + "• Đặc tính hàng hóa: Nước tăng lực Sting hương dâu tây đỏ với vị dâu ngon sảng khoái cùng công thức chứa Hồng sâm 4 năm tuổi, cho bạn nguồn năng lượng mạnh mẽ để biến mỗi ngày thành một cuộc phiêu lưu.\n"
            + "• Hướng dẫn sử dụng: Lắc nhẹ trước khi uống, dùng ngay sau khi mở nắp. Ngon hơn khi uống lạnh.\n"
            + "• Hướng dẫn bảo quản: Để nơi khô ráo, thoáng mát, tránh ánh sáng trực tiếp hoặc nơi có nhiệt độ cao.\n"
            + "• Hạn sử dụng: 12 tháng kể từ ngày sản xuất.\n"
            + "• Đơn vị chịu trách nhiệm: CÔNG TY TNHH NƯỚC GIẢI KHÁT SUNTORY PEPSICO VIỆT NAM - Lầu 5, Tòa nhà Blue Sky, Số 1 Đường Bạch Đằng, P.2, Q. Tân Bình, TP.HCM\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_HERSHEY_DESCRIPTION = "Hershey's là một trong những thương hiệu socola lâu đời nhất tại Mỹ, được thành lập vào năm 1894 bởi Milton Hershey. Trải qua hơn một thế kỷ phát triển và mở rộng, Hershey's đã trở thành một trong những thương hiệu socola lớn nhất trên thế giới.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Hershey's (Mỹ)\n"
            + "• Sản xuất tại: Mỹ\n"
            + "• Trọng lượng / Dung tích: 146g\n"
            + "• Thành phần: Đường, chất khô từ sữa (23%), hạnh nhân (12%), cacao khối (11%), bơ cacao (10%), dầu thực vật không hydro hóa (322, 476), hương liệu tự nhiên, giống tự nhiên và tổng hợp (hương vani, hương socola).\n"
            + "• Đặc tính hàng hóa: Socola sữa hạnh nhân Hershey’s Kisses là sự kết hợp socola sữa ngọt dịu và hạt hạnh nhân giòn bùi, giàu dưỡng chất.\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo và thoáng mát, tránh ánh nắng trực tiếp.\n"
            + "• Hạn sử dụng: 12 tháng kể từ ngày sản xuất.\n"
            + "• Lưu ý: Không nên sử dụng nếu dị ứng thành phần sản phẩm, sản phẩm có dấu hiệu ẩm mốc, hết hạn để tránh gây ảnh hưởng không tốt đến sức khỏe. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.\n"
            + "• Đơn vị chịu trách nhiệm: CÔNG TY TNHH DKSH VIỆT NAM - 23 Đại Lộ Độc Lập, KCN Việt Nam - Singapore, H. Thuận An, T. Bình Dương.";

    public static final String SEED_LAYS_DESCRIPTION = "Lay’s – Snack Khoai Tây số 1 Thế Giới đến từ Mỹ, được làm từ 100% khoai tây tươi tự nhiên cắt lát, biến hóa với hương vị thơm ngon khó cưỡng. Lay's đã có mặt ở hơn 100 quốc gia và chinh phục được sự yêu thích của hàng triệu người trên toàn thế giới. Thương hiệu Lay's hiện đang trực thuộc tập đoàn PepsiCo.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Lay's (Mỹ)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Dung tích: 53g\n"
            + "• Thành phần: Khoai tây, dầu thực vật, bột Phô Mai 6.5% (bột sữa whey, đường, muối, hương tự nhiên và giống tự nhiên, chất điều vị (E621, E627, E631), tinh bột (khoai tây, bắp, sắn), chất nhũ hoá (E414, E471, E339ii, E415, E307), chất chống đông vón (E551), chất điều chỉnh độ chua (E330)).\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.\n"
            + "• Hạn sử dụng: 6 tháng kể từ ngày sản xuất.\n"
            + "• Lưu ý: Sản phẩm có chứa nguyên liệu từ sữa. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.\n"
            + "• Đơn vị chịu trách nhiệm: CTY TNHH TP PEPSICO VIỆT NAM - Số 3-4-5, Lô CN, Đ. Số 2, KCN Sóng Thần 3, P. Phú Tân, TP. Thủ Dầu Một, Tỉnh Bình Dương.";

    public static final String SEED_ENAAK_DESCRIPTION = "Snack mì hương gà Enaak Gemez mang hình dáng của những sợi mì giòn rụm kết hợp cùng hương vị gà thơm lừng và chút cay cay hấp dẫn, tạo cảm giác lạ miệng kích thích vị giác, là món ăn vặt tiện lợi và an toàn.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Enaak (Indonesia)\n"
            + "• Sản xuất tại: Indonesia\n"
            + "• Trọng lượng / Dung tích: 30g\n"
            + "• Thành phần: Bột mì, dầu cọ, tinh bột sắn, bột hành, bột tỏi, sốt đậu nành, bột gia vị gà (đường, chất điều vị (E621), hương gà giống tự nhiên), chất điều vị (E621), đường, chất tạo xốp, muối, chất tạo ngọt.\n"
            + "• Đặc tính hàng hóa: Snack mì giòn rụm cay nhẹ thơm ngon, bóp vụn trước khi ăn để thưởng thức trọn vẹn hương vị.\n"
            + "• Hướng dẫn sử dụng: Dùng trực tiếp.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo và thoáng mát, tránh ánh nắng trực tiếp.\n"
            + "• Hạn sử dụng: 6 tháng kể từ ngày sản xuất.\n"
            + "• Lưu ý: Không nên sử dụng nếu dị ứng thành phần sản phẩm, sản phẩm có dấu hiệu ẩm mốc, hết hạn. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.\n"
            + "• Đơn vị chịu trách nhiệm: CTY CP TẬP ĐOÀN KIDO - 138-142 Hai Bà Trưng, Phường Đa Kao, Quận 1, Thành phố Hồ Chí Minh.";

    public static final String SEED_OREO_DESCRIPTION = "Bánh Oreo Socola Pie vị Red Velvet thơm ngon, mềm xốp với lớp phủ socola đậm vị và nhân marshmallow dẻo dai béo ngậy tan chảy, là món ăn nhẹ tuyệt hảo cho mọi gia đình.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Kinh Đô (Việt Nam)\n"
            + "• Sản xuất tại: Việt Nam\n"
            + "• Trọng lượng / Quy cách: 336g (hộp 12 bánh)\n"
            + "• Thành phần: Đường, bột mì, mạch nha, dầu thực vật (dầu cọ), trứng, chất làm ẩm (422, 420(ii), 420(i)), bột cacao (4.2%), dextroza, chất nhũ hóa, gelatin, hương liệu tổng hợp, muối i-ốt, chất tạo xốp, chất ổn định, chất tạo màu tổng hợp.\n"
            + "• Hướng dẫn sử dụng: Bánh được ăn trực tiếp, thích hợp làm món ăn nhẹ trong ngày.\n"
            + "• Hướng dẫn bảo quản: Bảo quản nơi khô ráo, thoáng mát, tránh ánh nắng trực tiếp.\n"
            + "• Hạn sử dụng: 10 tháng kể từ ngày sản xuất.\n"
            + "• Lưu ý: Sản phẩm có chứa lúa mì, trứng, sữa, đậu nành. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.\n"
            + "• Đơn vị chịu trách nhiệm: Công ty cổ phần Mondelez Kinh Đô Việt Nam - Km 22 Quốc lộ 5A, Thị trấn Bần Yên Nhân, Huyện Mỹ Hào, Tỉnh Hưng Yên.";

    public static final String SEED_KIRIN_DESCRIPTION = "Kirin – một công ty Nhật Bản đứng hàng đầu Châu Á về lĩnh vực nước uống và thực phẩm. Kirin sản xuất nhiều loại nước giải khát với hương vị đa dạng và phong phú, làm hài lòng người tiêu dùng trên khắp thế giới bằng chất lượng sản phẩm và sự đổi mới trong công nghệ chế biến.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: Kirin (Nhật Bản)\n"
            + "• Sản xuất tại: Nhật Bản\n"
            + "• Trọng lượng / Dung tích: 280ml\n"
            + "• Thành phần: Nước, đường, sữa bột gầy, chất xơ đậu nành,...\n"
            + "• Đặc tính hàng hóa: Sữa chua và chanh thanh mát hòa quyện hài hòa, độc đáo vào nước uống cho cảm giác uống thú vị, sảng khoái. Sản phẩm cung cấp nhiều vitamin và dưỡng chất giúp cơ thể khỏe mạnh.\n"
            + "• Hướng dẫn sử dụng: Lắc nhẹ trước khi uống, dùng ngay sau khi mở nắp. Ngon hơn khi uống lạnh.\n"
            + "• Hướng dẫn bảo quản: Để nơi khô ráo, thoáng mát, tránh ánh sáng trực tiếp hoặc nơi có nhiệt độ cao.\n"
            + "• Lưu ý: Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.";

    public static final String SEED_OSTAR_DESCRIPTION = "Snack khoai tây vị phô mai trứng muối O'Star gói 56g là loại snack khoai tây với vị mặn bùi bùi của trứng muối cùng vị thơm béo của phô mai hòa quyện trong từng lát bánh giòn rụm. Snack O'Star được làm từ lát khoai tây tươi ngon chọn lọc, gia vị hài hòa sẽ là món ăn vặt hấp dẫn cho bạn và gia đình.\n\n"
            + "Chi tiết sản phẩm:\n"
            + "• Thương hiệu / Xuất xứ: O'Star (Hàn Quốc)\n"
            + "• Nơi sản xuất: Việt Nam\n"
            + "• Trọng lượng / Quy cách: 56g\n"
            + "• Năng lượng: 560 kcal / 100g\n"
            + "• Thành phần: Khoai tây 55.6%, dầu thực vật, bột gia vị phô mai trứng muối 5% (đường, muối, chất điều vị, bột phô mai, hương liệu, chất chống đông vón, chất điều chỉnh độ acid, chất tạo màu tự nhiên), bột trứng muối 2.5%, bơ thực vật, chất chống oxy hóa.\n"
            + "• Khuyên dùng: Dùng trực tiếp sau khi mở gói. Không dùng cho người dị ứng các thành phần sản phẩm.\n"
            + "• Lưu ý: Sản phẩm có chứa sữa, trứng và có thể chứa lúa mì, đậu nành, tôm, cá, quả hạch. Bao bì sản phẩm và nơi sản xuất có thể khác thực tế tùy đợt nhập hàng.\n"
            + "• Bảo quản: Nơi khô ráo, thoáng mát đảm bảo điều kiện an toàn vệ sinh thực phẩm, tránh ánh sáng mặt trời trực tiếp.";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DbConstants.DB_NAME, null, DbConstants.DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + DbConstants.TABLE_USERS + " ("
                + DbConstants.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_USER_FULL_NAME + " TEXT NOT NULL, "
                + DbConstants.COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + DbConstants.COLUMN_USER_PHONE + " TEXT, "
                + DbConstants.COLUMN_USER_PASSWORD_HASH + " TEXT NOT NULL, "
                + DbConstants.COLUMN_USER_SALT + " TEXT NOT NULL, "
                + DbConstants.COLUMN_USER_ADDRESS + " TEXT, "
                + DbConstants.COLUMN_USER_AVATAR_PATH + " TEXT, "
                + DbConstants.COLUMN_USER_ROLE + " TEXT NOT NULL, "
                + DbConstants.COLUMN_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        // Create Products Table
        String createProductsTable = "CREATE TABLE " + DbConstants.TABLE_PRODUCTS + " ("
                + DbConstants.COLUMN_PRODUCT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + DbConstants.COLUMN_PRODUCT_DESCRIPTION + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_CATEGORY + " TEXT NOT NULL, "
                + DbConstants.COLUMN_PRODUCT_PRICE + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_PRODUCT_ORIGINAL_PRICE + " INTEGER, "
                + DbConstants.COLUMN_PRODUCT_TAG + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_IMAGE + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_IS_ACTIVE + " INTEGER DEFAULT 1, "
                + DbConstants.COLUMN_PRODUCT_ORIGIN + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_MADE_IN + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_SIZE_INFO + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_INGREDIENTS + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_FEATURES + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_USAGE_GUIDE + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_STORAGE_GUIDE + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_SHELF_LIFE + " TEXT, "
                + DbConstants.COLUMN_PRODUCT_RATING + " REAL DEFAULT 0, "
                + DbConstants.COLUMN_PRODUCT_SOLD_COUNT + " INTEGER DEFAULT 0, "
                + DbConstants.COLUMN_PRODUCT_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        // Create Product Images Table
        String createProductImagesTable = "CREATE TABLE " + DbConstants.TABLE_PRODUCT_IMAGES + " ("
                + DbConstants.COLUMN_PRODUCT_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_PRODUCT_IMAGE_PATH + " TEXT NOT NULL, "
                + DbConstants.COLUMN_PRODUCT_IMAGE_SORT_ORDER + " INTEGER NOT NULL DEFAULT 0, "
                + "FOREIGN KEY(" + DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + ") REFERENCES " + DbConstants.TABLE_PRODUCTS + "(" + DbConstants.COLUMN_PRODUCT_ID + ") ON DELETE CASCADE"
                + ");";

        // Create Cart Items Table
        String createCartItemsTable = "CREATE TABLE " + DbConstants.TABLE_CART_ITEMS + " ("
                + DbConstants.COLUMN_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_CART_USER_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_CART_PRODUCT_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_CART_QUANTITY + " INTEGER NOT NULL DEFAULT 1, "
                + "UNIQUE(" + DbConstants.COLUMN_CART_USER_ID + ", " + DbConstants.COLUMN_CART_PRODUCT_ID + "), "
                + "FOREIGN KEY(" + DbConstants.COLUMN_CART_USER_ID + ") REFERENCES " + DbConstants.TABLE_USERS + "(" + DbConstants.COLUMN_USER_ID + ") ON DELETE CASCADE, "
                + "FOREIGN KEY(" + DbConstants.COLUMN_CART_PRODUCT_ID + ") REFERENCES " + DbConstants.TABLE_PRODUCTS + "(" + DbConstants.COLUMN_PRODUCT_ID + ") ON DELETE CASCADE"
                + ");";

        // Create Orders Table
        String createOrdersTable = "CREATE TABLE " + DbConstants.TABLE_ORDERS + " ("
                + DbConstants.COLUMN_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_ORDER_USER_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_ORDER_CODE + " TEXT UNIQUE NOT NULL, "
                + DbConstants.COLUMN_ORDER_TOTAL_AMOUNT + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_ORDER_DISCOUNT_AMOUNT + " INTEGER DEFAULT 0, "
                + DbConstants.COLUMN_ORDER_DELIVERY_FEE + " INTEGER DEFAULT 0, "
                + DbConstants.COLUMN_ORDER_PAYMENT_METHOD + " TEXT NOT NULL, "
                + DbConstants.COLUMN_ORDER_DELIVERY_ADDRESS + " TEXT NOT NULL, "
                + DbConstants.COLUMN_ORDER_PHONE + " TEXT NOT NULL, "
                + DbConstants.COLUMN_ORDER_STATUS + " TEXT NOT NULL, "
                + DbConstants.COLUMN_ORDER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY(" + DbConstants.COLUMN_ORDER_USER_ID + ") REFERENCES " + DbConstants.TABLE_USERS + "(" + DbConstants.COLUMN_USER_ID + ")"
                + ");";

        // Create Order Items Table
        String createOrderItemsTable = "CREATE TABLE " + DbConstants.TABLE_ORDER_ITEMS + " ("
                + DbConstants.COLUMN_ORDER_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbConstants.COLUMN_ORDER_ITEM_ORDER_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_ORDER_ITEM_PRODUCT_ID + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_ORDER_ITEM_PRODUCT_NAME + " TEXT NOT NULL, "
                + DbConstants.COLUMN_ORDER_ITEM_UNIT_PRICE + " INTEGER NOT NULL, "
                + DbConstants.COLUMN_ORDER_ITEM_QUANTITY + " INTEGER NOT NULL, "
                + "FOREIGN KEY(" + DbConstants.COLUMN_ORDER_ITEM_ORDER_ID + ") REFERENCES " + DbConstants.TABLE_ORDERS + "(" + DbConstants.COLUMN_ORDER_ID + ") ON DELETE CASCADE"
                + ");";

        db.execSQL(createUsersTable);
        db.execSQL(createProductsTable);
        db.execSQL(createProductImagesTable);
        db.execSQL(createCartItemsTable);
        db.execSQL(createOrdersTable);
        db.execSQL(createOrderItemsTable);

        // Seed initial data
        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        // 1. Seed Admin User
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(AppConstants.SEED_ADMIN_PASSWORD, salt);

        ContentValues adminValues = new ContentValues();
        adminValues.put(DbConstants.COLUMN_USER_FULL_NAME, AppConstants.SEED_ADMIN_NAME);
        adminValues.put(DbConstants.COLUMN_USER_EMAIL, AppConstants.SEED_ADMIN_EMAIL);
        adminValues.put(DbConstants.COLUMN_USER_PHONE, AppConstants.SEED_ADMIN_PHONE);
        adminValues.put(DbConstants.COLUMN_USER_PASSWORD_HASH, passwordHash);
        adminValues.put(DbConstants.COLUMN_USER_SALT, salt);
        adminValues.put(DbConstants.COLUMN_USER_ADDRESS, "TP. Hồ Chí Minh");
        adminValues.put(DbConstants.COLUMN_USER_ROLE, AppConstants.ROLE_ADMIN);

        db.insert(DbConstants.TABLE_USERS, null, adminValues);

        // 2. Seed Products
        seedProduct(db, "Khoai tây phô mai O\'Star", SEED_OSTAR_DESCRIPTION, AppConstants.CATEGORY_SNACK, 25000L, 30000L, "Bán chạy", "snack_khoai_tay_pho_mai");
        seedProduct(db, "Bánh Oreo Socola Pie Red Velvet", SEED_OREO_DESCRIPTION, AppConstants.CATEGORY_CANDY, 52000L, 58000L, "Hot", "candy_oreo_red_velvet");
        seedProduct(db, "Snack mì Gemez Enaak Extra", SEED_ENAAK_DESCRIPTION, AppConstants.CATEGORY_SNACK, 10000L, 12000L, "Siêu hot", "snack_mi_enaak_gemez");
        seedProduct(db, "Snack khoai tây Lay\'s Phô Mai Cheddar", SEED_LAYS_DESCRIPTION, AppConstants.CATEGORY_SNACK, 16000L, 19000L, "Snack giòn", "snack_lays_cheddar");
        seedProduct(db, "Nước uống Kirin iMUSE sữa chua & chanh", SEED_KIRIN_DESCRIPTION, AppConstants.CATEGORY_DRINK, 15000L, 18000L, "Giải nhiệt", "drink_kirin_imuse");
        seedProduct(db, "Sữa chua uống Vinamilk Probi hương dứa", SEED_PROBI_DESCRIPTION, AppConstants.CATEGORY_DRINK, 26000L, 30000L, "Bán chạy", "drink_probi_dua");
        seedProduct(db, "Bánh su kem phô mai GO!", SEED_SU_KEM_DESCRIPTION, AppConstants.CATEGORY_CANDY, 35000L, 40000L, "Thượng hạng", "candy_banh_su_kem_pho_mai");
        seedProduct(db, "Bánh Pâté Chaud nhân thịt", SEED_PATE_CHAUD_DESCRIPTION, AppConstants.CATEGORY_SNACK, 18000L, 22000L, "Nóng hổi", "snack_pate_chaud");
        seedProduct(db, "Bánh Custas Tiramisu Orion", SEED_CUSTAS_TIRAMISU_DESCRIPTION, AppConstants.CATEGORY_CANDY, 56000L, 62000L, "Siêu hot", "candy_custas_tiramisu");
        seedProduct(db, "Snack bánh que Dorkbua Lotus", SEED_DORKBUA_DESCRIPTION, AppConstants.CATEGORY_SNACK, 22000L, 26000L, "Snack giòn", "snack_que_dorkbua");
        seedProduct(db, "Snack Toonies vị gà rán Orion", SEED_TOONIES_DESCRIPTION, AppConstants.CATEGORY_SNACK, 12000L, 15000L, "Bán chạy", "snack_toonies_ga_ran");
        seedProduct(db, "Nước tăng lực Sting hương dâu", SEED_STING_DESCRIPTION, AppConstants.CATEGORY_DRINK, 11000L, 13000L, "Bán chạy", "drink_sting_dau");
        seedProduct(db, "Socola sữa hạnh nhân Hershey\'s Kisses", SEED_HERSHEY_DESCRIPTION, AppConstants.CATEGORY_CANDY, 78000L, 85000L, "Thượng hạng", "candy_hershey_kisses");
    }

    private void seedProduct(SQLiteDatabase db, String name, String description, String category,
                             long price, long originalPrice, String tag, String image) {
        ContentValues values = new ContentValues();
        values.put(DbConstants.COLUMN_PRODUCT_NAME, name);
        values.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(DbConstants.COLUMN_PRODUCT_CATEGORY, category);
        values.put(DbConstants.COLUMN_PRODUCT_PRICE, price);
        values.put(DbConstants.COLUMN_PRODUCT_ORIGINAL_PRICE, originalPrice);
        values.put(DbConstants.COLUMN_PRODUCT_TAG, tag);
        values.put(DbConstants.COLUMN_PRODUCT_IMAGE, image);
        values.put(DbConstants.COLUMN_PRODUCT_IS_ACTIVE, 1);

        db.insert(DbConstants.TABLE_PRODUCTS, null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Incremental migration for version 15: add product_images table without dropping existing tables
        if (oldVersion < 15) {
            String createProductImagesTable = "CREATE TABLE IF NOT EXISTS " + DbConstants.TABLE_PRODUCT_IMAGES + " ("
                    + DbConstants.COLUMN_PRODUCT_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + " INTEGER NOT NULL, "
                    + DbConstants.COLUMN_PRODUCT_IMAGE_PATH + " TEXT NOT NULL, "
                    + DbConstants.COLUMN_PRODUCT_IMAGE_SORT_ORDER + " INTEGER NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY(" + DbConstants.COLUMN_PRODUCT_IMAGE_PRODUCT_ID + ") REFERENCES " + DbConstants.TABLE_PRODUCTS + "(" + DbConstants.COLUMN_PRODUCT_ID + ") ON DELETE CASCADE"
                    + ");";
            db.execSQL(createProductImagesTable);
        }

        // Incremental migration for version 16: update probi description with rich details
        if (oldVersion < 16) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_PROBI_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"drink_probi_dua"});
        }

        // Incremental migration for version 17: update custas tiramisu description with rich details
        if (oldVersion < 17) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_CUSTAS_TIRAMISU_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"candy_custas_tiramisu"});
        }

        // Incremental migration for version 18: update dorkbua lotus description with rich details
        if (oldVersion < 18) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_DORKBUA_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_que_dorkbua"});
        }

        // Incremental migration for version 19: update banh su kem pho mai description with rich details
        if (oldVersion < 19) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_SU_KEM_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"candy_banh_su_kem_pho_mai"});
        }

        // Incremental migration for version 20: update pate chaud description with rich details
        if (oldVersion < 20) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_PATE_CHAUD_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_pate_chaud"});
        }

        // Incremental migration for version 21: update toonies ga ran description with rich details
        if (oldVersion < 21) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_TOONIES_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_toonies_ga_ran"});
        }

        // Incremental migration for version 22: update sting dau description with rich details
        if (oldVersion < 22) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_STING_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"drink_sting_dau"});
        }

        // Incremental migration for version 23: update hershey kisses description with rich details
        if (oldVersion < 23) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_HERSHEY_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"candy_hershey_kisses"});
        }

        // Incremental migration for version 24: update lays cheddar description with rich details
        if (oldVersion < 24) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_LAYS_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_lays_cheddar"});
        }

        // Incremental migration for version 25: update enaak gemez description with rich details
        if (oldVersion < 25) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_ENAAK_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_mi_enaak_gemez"});
        }

        // Incremental migration for version 26: update oreo red velvet description with rich details
        if (oldVersion < 26) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_OREO_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"candy_oreo_red_velvet"});
        }

        // Incremental migration for version 27: update kirin imuse description with rich details
        if (oldVersion < 27) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_KIRIN_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"drink_kirin_imuse"});
        }

        // Incremental migration for version 28: update o'star khoai tay pho mai description with rich details
        if (oldVersion < 28) {
            ContentValues cv = new ContentValues();
            cv.put(DbConstants.COLUMN_PRODUCT_DESCRIPTION, SEED_OSTAR_DESCRIPTION);
            db.update(DbConstants.TABLE_PRODUCTS, cv, DbConstants.COLUMN_PRODUCT_IMAGE + " = ?", new String[]{"snack_khoai_tay_pho_mai"});
        }

        // Incremental migration for version 29: add rich product columns safely
        if (oldVersion < 29) {
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_ORIGIN, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_MADE_IN, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_SIZE_INFO, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_INGREDIENTS, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_FEATURES, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_USAGE_GUIDE, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_STORAGE_GUIDE, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_SHELF_LIFE, "TEXT");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_RATING, "REAL DEFAULT 0");
            addColumnIfNotExists(db, DbConstants.TABLE_PRODUCTS, DbConstants.COLUMN_PRODUCT_SOLD_COUNT, "INTEGER DEFAULT 0");
        }
    }

    private void addColumnIfNotExists(SQLiteDatabase db, String table, String column, String type) {
        boolean exists = false;
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex != -1) {
                    do {
                        if (column.equalsIgnoreCase(cursor.getString(nameIndex))) {
                            exists = true;
                            break;
                        }
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception ignored) {
        }
        if (!exists) {
            db.execSQL("ALTER TABLE " + table + " ADD COLUMN " + column + " " + type);
        }
    }
}
