-- SQL sinh tự động từ Quan ly cong viec_PHN.csv
USE phn;


INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Xây dựng mạng lưới đường đi trục Nam-Bắc, Bắc-Nam theo các hình thức: đường bộ, đường sắt, đường thủy và đường bay', 'Khảo sát với P.HTML về luồng chạy, kho dự kiến, nghiên cứu điểm đặt kho tại HN, HCM để tối ưu cho việc triển khai KD', 'Việc xây dựng mạng lưới cần phối hợp cùng với P.HTML để tránh trùng nhau, sơ đồ đường đi cần thu thập thông tin về luồng vận hành hiện có, sau đó tính toán phương án tối ưu phù hợp với việc triển khai XHH, nội dung khi thực hiện sẽ báo cáo xin ý kiến trực tiếp chỉ huy nếu có vướng mắc', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Rà soát các khách hàng ngành dược', 'Rà soat doanh thu, sản lượng, chính sách đang áp dụng với các khách hàng dược phẩm hiện nay', 'Báo cáo danh sách KH, doanh thu, sản lượng, dịch vụ  Quý 4.2025
Phân tích sản lượng BQ, tuyến, loại hình dịch vụ', '2026-01-17 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Khảo sát danh sách khách hàng hiện hữu chưa sử dụng dv hàng nặng', 'Tạo Form khảo sát chi nhánh và tổng hợp kết quả báo cáo', 'Tổng hợp kết quả', '2026-01-15 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Nghiên cứu phương án triển khai nền tảng VT Move', '- Nắm lại các nội dung về sàn vận tải hiện có', '- Căn cứ theo "Chỉ thị về việc triển khai Công tác trên hệ thống sàn vận tải" số 37CT-VPost Log, yêu cầu các chi nhánh:
Các chi nhánh bắt buộc sử dụng Sàn vận tải để tạo đơn với các dịch vụ vận tải đã chuẩn hóa.
- Phát triển và quản lý 100% đối tác vận tải: ký hợp đồng đầy đủ, cập nhật thông tin đối tác và phương tiện lên hệ thống.
- Triển khai App Smart TMS:
+ 100% đơn vị kinh doanh, bưu cục và đối tác vận tải phải sử dụng App.
+ Mỗi chi nhánh đăng tối thiểu 5 nhu cầu tìm vận tải/ngày trên App.
+ Đảm bảo toàn bộ CBCNV nắm rõ và thực hiện nghiêm nội dung chỉ thị.
+ Theo dõi, giám sát, phản hồi lỗi và góp ý cải tiến hệ thống trong quá trình triển khai.
=> Hiện PTGĐ Lê Tuấn Anh yêu cầu triển khai ban dự án để rõ đầu mối chủ trì và chịu trách nhiệm chính', '2026-02-28 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Đào tạo CPCT cho TBC toàn quốc', 'Yêu cầu triển khai đào tạo lần lượt cho toàn quốc, phòng hoặc tỉnh đào tạo, nếu tỉnh đào tạo thì phòng tham gia', 'Mục tiêu từ file CSV ngày 20/01/2026', '2026-01-31 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Thống kê các tỉnh đang có đối tác Ahamove và Lalamove, đưa ra phương án hợp tác làm dịch vụ CPCT', 'Xây dựng form khảo sát chi nhánh và tổng hợp kết quả báo cáo, đưa ra phương án hợp tác làm dịch vụ CPCT', 'Mục tiêu từ file CSV ngày 21/01/2026', '2026-02-07 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Theo dõi hoạt động của bưu cục Xuất Nhập khẩu LCIXNK của chi nhánh LCI', 'Báo cáo doanh thu hàng ngày của BC XNK Lào cai', 'Mục tiêu từ file CSV ngày 22/01/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Đề xuất các tuyến đi thẳng', 'Dựa trên dữ liệu từ file "Tinh toan sap xep xe XHH" đề xuất các chuyển đi thẳng các tỉnh - tỉnh, tỉnh - các tỉnh, tỉnh - TTKT', 'Mục tiêu từ file CSV ngày 30/01/2026', '2026-02-07 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Báo cáo nghiên cứu thị trường hàng nặng', 'Thu thập thông tin và báo cáo, có đánh giá, yêu cầu đánh giá bằng bản word và phụ lục excel số liệu', 'Mục tiêu từ file CSV ngày 20/01/2026', '2026-01-25 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn An' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn An' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Trình ký kế hoạch Chi phí, lợi nhuận năm 2026', 'Trình ký kế hoạch Chi phí, lợi nhuận năm 2026', 'Đã báo cáo bản tính toán sơ bộ', '2026-02-09 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Tối ưu quy trình nhập và thanh quyết toán doanh thu dịch vụ tự thu tự phát, không dùng giấy tờ, ko trình ký văn bản qua voffice', 'Rà soát lại quy trình, các văn bản hiện hành và tối ưu, không phải trình ký qua voffice mà vẫn quyết toán được', 'Mục tiêu từ file CSV ngày 29/01/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Đánh giá lại quy chế chi trả chi phí phát hàng nặng, đề xuất thay đổi', 'Đánh giá lại quy chế chi trả chi phí phát hàng nặng, đề xuất thay đổi, đề xuất phương án kiểm soát sử dụng chi phí phát hàng nặng ở các tỉnh. Yêu cầu có văn bản trình ký thay đổi, theo hướng tiết kiệm', 'Mục tiêu từ file CSV ngày 29/01/2026', '2026-02-07 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Ban hành hướng dẫn triển khai dịch vụ hàng nặng nội tỉnh', 'Tính toán chi phí xe nội tỉnh, ban hành để tỉnh thực hiện hạch toán thu phát tại tỉnh đối với hàng nặng', 'Mục tiêu từ file CSV ngày 02/02/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Ban hàng bảng giá đường bay hàng Dâu Tây cho Chi nhánh Sơn La', '', 'Mục tiêu từ file CSV ngày 02/02/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Ban hành hướng dẫn triển khai thuê xe cá nhân hoặc hộ kinh doanh để làm dịch vụ tự thu tự phát', '', 'Mục tiêu từ file CSV ngày 02/02/2026', '2026-02-10 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Ban hành bảng giá bay hàng nặng HNI, HCM và ngược lại', 'Ban hành bảng giá bay hàng nặng HNI, HCM và ngược lại', 'Mục tiêu từ file CSV ngày 02/02/2026', '2026-02-07 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Tự động tính nguồn V1HG', 'Làm vc Cntt xây dựng bài toán, PYC đưa lên hệ thống tính tự động', 'Mục tiêu từ file CSV ngày 03/02/2026', '2026-02-28 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Chuyển nhập hàng > 3m về EVTP2', '', 'Mục tiêu từ file CSV ngày 03/02/2026', '2026-02-28 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Điều chỉnh hệ thống, buộc phải nhập kích thước, trọng lượng với các đơn hàng nặng', '', 'Mục tiêu từ file CSV ngày 03/02/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Phát triển phần mềm quản lý công việc', 'Xây dựng phần mềm thay thế Google Sheet, tích hợp template phiếu giao nhiệm vụ và đánh giá kết quả hàng tháng', 'Mục tiêu từ file CSV ngày 03/02/2026', '2026-02-28 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Đỗ Khánh Ly' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Đỗ Khánh Ly' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Xin kinh phí giao dịch cuối năm', 'Xin kinh phí giao dịch cuối năm', 'Mục tiêu từ file CSV ngày 04/02/2026', '2026-02-05 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Rà soát Khách hàng lớn mở chặn ( >1,5m hoặc 150Kg)', '- Tổng hợp danh sách
- Tờ trình chủ trương
- Phân tích số liệu qua TTKT', 'Mục tiêu từ file CSV ngày 03/02/2026', '2026-02-07 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Xây dựng quy trình giám sát đối với hàng nặng, giảm thiểu việc gian lận cước, ảnh hưởng tới doanh thu hàng nặng', '', 'Mục tiêu từ file CSV ngày 07/02/2026', '2026-02-15 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Hoàn thiện PGNV phòng T2 và đánh giá tháng 1', '- Hoàn thiện đánh giá tháng 1, tổng kết KI phòng
- Gửi PGV tháng 2 về đầu mối TCTH
- Hoàn thiện nghị quyết chi bộ phòng SPHN tháng 02', 'Mục tiêu từ file CSV ngày 06/02/2026', '2026-02-06 00:00:00', 0.5, 'COMPLETED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Tổng kết chương trình tết vẹn toàn, đẩy voucher cho KH', 'Đẩy được voucher cho KH trước khi nghỉ tết', 'Mục tiêu từ file CSV ngày 10/02/2026', '2026-02-14 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Báo cáo kết quả doanh thu các chính sách đã ban hành', '', 'Mục tiêu từ file CSV ngày 11/02/2026', '2026-02-11 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Báo cáo đánh giá KPI vận hành hàng nặng', 'Xây dựng báo cáo, và thực hiện báo cáo hàng ngày', 'Mục tiêu từ file CSV ngày 10/02/2026', '2026-02-11 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Rà soát quy hoạch lại dịch vụ phòng', '', 'Mục tiêu từ file CSV ngày 11/02/2026', '2026-02-11 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   NOW(6));

INSERT INTO tasks
  (title, content, objective, deadline, weight, status, assigner_id, leader_id, assignee_id, created_at)
VALUES
  ('Phụ phí nâng hạ Hàng nặng', '', 'Mục tiêu từ file CSV ngày 12/02/2026', '2026-02-12 00:00:00', 0.5, 'ACCEPTED',
   (SELECT id FROM users WHERE username = 'admin' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
   NOW(6));
