-- Cập nhật tasks và daily_reports từ sheet Báo cáo CV cuối ngày (CSV)
-- Sinh bởi update_from_bao_cao_cv_csv.py. Chạy trên PostgreSQL (Render).
SET client_encoding = 'UTF8';

UPDATE tasks SET status = 'ACCEPTED', weight = 0.8, quality = 0.9, objective = 'Việc xây dựng mạng lưới cần phối hợp cùng với P.HTML để tránh trùng nhau, sơ đồ đường đi cần thu thập thông tin về luồng vận hành hiện có, sau đó tính toán phương án tối ưu phù hợp với việc triển khai XHH, nội dung khi thực hiện sẽ báo cáo xin ý kiến trực tiếp chỉ huy nếu có vướng mắc'
WHERE title = 'Xây dựng mạng lưới đường đi trục Nam-Bắc, Bắc-Nam theo các hình thức: đường bộ, đường sắt, đường thủy và đường bay'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.4, quality = 0.9, objective = 'Báo cáo danh sách KH, doanh thu, sản lượng, dịch vụ  Quý 4.2025
Phân tích sản lượng BQ, tuyến, loại hình dịch vụ'
WHERE title = 'Rà soát các khách hàng ngành dược'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.6, quality = 0.9, objective = 'Tổng hợp kết quả'
WHERE title = 'Khảo sát danh sách khách hàng hiện hữu chưa sử dụng dv hàng nặng'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.8, quality = 0.9, objective = '- Căn cứ theo "Chỉ thị về việc triển khai Công tác trên hệ thống sàn vận tải" số 37CT-VPost Log, yêu cầu các chi nhánh:
Các chi nhánh bắt buộc sử dụng Sàn vận tải để tạo đơn với các dịch vụ vận tải đã chuẩn hóa.
- Phát triển và quản lý 100% đối tác vận tải: ký hợp đồng đầy đủ, cập nhật thông tin đối tác và phương tiện lên hệ thống.
- Triển khai App Smart TMS:
+ 100% đơn vị kinh doanh, bưu cục và đối tác vận tải phải sử dụng App.
+ Mỗi chi nhánh đăng tối thiểu 5 nhu cầu tìm vận tải/ngày trên App.
+ Đảm bảo toàn bộ CBCNV nắm rõ và thực hiện nghiêm nội dung chỉ thị.
+ Theo dõi, giám sát, phản hồi lỗi và góp ý cải tiến hệ thống trong quá trình triển khai.
=> Hiện PTGĐ Lê Tuấn Anh yêu cầu triển khai ban dự án để rõ đầu mối chủ trì và chịu trách nhiệm chính'
WHERE title = 'Nghiên cứu phương án triển khai nền tảng VT Move'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.4, quality = 0.9
WHERE title = 'Đào tạo CPCT cho TBC toàn quốc'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Thống kê các tỉnh đang có đối tác Ahamove và Lalamove, đưa ra phương án hợp tác làm dịch vụ CPCT'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.2, quality = 0.9
WHERE title = 'Theo dõi hoạt động của bưu cục Xuất Nhập khẩu LCIXNK của chi nhánh LCI'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.8
WHERE title = 'Đề xuất các tuyến đi thẳng'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6, quality = 0.9
WHERE title = 'Báo cáo nghiên cứu thị trường hàng nặng'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn An' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.8, objective = 'Đã báo cáo bản tính toán sơ bộ'
WHERE title = 'Trình ký kế hoạch Chi phí, lợi nhuận năm 2026'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1);

UPDATE tasks SET weight = 0.8
WHERE title = 'Tối ưu quy trình nhập và thanh quyết toán doanh thu dịch vụ tự thu tự phát, không dùng giấy tờ, ko trình ký văn bản qua voffice'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.8
WHERE title = 'Đánh giá lại quy chế chi trả chi phí phát hàng nặng, đề xuất thay đổi'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Ban hành hướng dẫn triển khai dịch vụ hàng nặng nội tỉnh'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Ban hàng bảng giá đường bay hàng Dâu Tây cho Chi nhánh Sơn La'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6, quality = 0.9
WHERE title = 'Ban hành hướng dẫn triển khai thuê xe cá nhân hoặc hộ kinh doanh để làm dịch vụ tự thu tự phát'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Ban hành bảng giá bay hàng nặng HNI, HCM và ngược lại'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Tự động tính nguồn V1HG'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.6
WHERE title = 'Chuyển nhập hàng > 3m về EVTP2'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.4
WHERE title = 'Điều chỉnh hệ thống, buộc phải nhập kích thước, trọng lượng với các đơn hàng nặng'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1);

UPDATE tasks SET status = 'ACCEPTED', weight = 0.8
WHERE title = 'Phát triển phần mềm quản lý công việc'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Đỗ Khánh Ly' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.4, quality = 0.9
WHERE title = 'Xin kinh phí giao dịch cuối năm'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.4, quality = 0.9
WHERE title = 'Rà soát Khách hàng lớn mở chặn ( >1,5m hoặc 150Kg)'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1);

UPDATE tasks SET weight = 0.6
WHERE title = 'Xây dựng quy trình giám sát đối với hàng nặng, giảm thiểu việc gian lận cước, ảnh hưởng tới doanh thu hàng nặng'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1);

UPDATE tasks SET status = 'COMPLETED', weight = 0.2, quality = 0.9
WHERE title = 'Hoàn thiện PGNV phòng T2 và đánh giá tháng 1'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1);

UPDATE tasks SET weight = 0.4
WHERE title = 'Tổng kết chương trình tết vẹn toàn, đẩy voucher cho KH'
  AND assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1);

-- 2) INSERT daily_reports (báo cáo cuối ngày cho dashboard theo dõi)
-- Chỉ thêm bản ghi có nội dung Kết quả thực hiện; report_date = Ngày hoàn thành hoặc Ngày giao
INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '2026-01-07'::date, 'Việc xây dựng mạng lưới cần phối hợp cùng với P.HTML để tránh trùng nhau, sơ đồ đường đi cần thu thập thông tin về luồng vận hành hiện có, sau đó tính toán phương án tối ưu phù hợp với việc triển khai XHH, nội dung khi thực hiện sẽ báo cáo xin ý kiến trực tiếp chỉ huy nếu có vướng mắc', 0.8, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND t.title = 'Xây dựng mạng lưới đường đi trục Nam-Bắc, Bắc-Nam theo các hình thức: đường bộ, đường sắt, đường thủy và đường bay'
WHERE u.name = 'Vũ Văn Đoàn'
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '2026-01-07'::date
  );

INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '2026-01-07'::date, 'Báo cáo danh sách KH, doanh thu, sản lượng, dịch vụ  Quý 4.2025
Phân tích sản lượng BQ, tuyến, loại hình dịch vụ', 0.4, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND t.title = 'Rà soát các khách hàng ngành dược'
WHERE u.name = 'Trần Minh Nhất'
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '2026-01-07'::date
  );

INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '2026-01-08'::date, 'Tổng hợp kết quả', 0.6, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND t.title = 'Khảo sát danh sách khách hàng hiện hữu chưa sử dụng dv hàng nặng'
WHERE u.name = 'Phạm Thùy Dương'
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '2026-01-08'::date
  );

INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '2026-01-08'::date, '- Căn cứ theo "Chỉ thị về việc triển khai Công tác trên hệ thống sàn vận tải" số 37CT-VPost Log, yêu cầu các chi nhánh:
Các chi nhánh bắt buộc sử dụng Sàn vận tải để tạo đơn với các dịch vụ vận tải đã chuẩn hóa.
- Phát triển và quản lý 100% đối tác vận tải: ký hợp đồng đầy đủ, cập nhật thông tin đối tác và phương tiện lên hệ thống.
- Triển khai App Smart TMS:
+ 100% đơn vị kinh doanh, bưu cục và đối tác vận tải phải sử dụng App.
+ Mỗi chi nhánh đăng tối thiểu 5 nhu cầu tìm vận tải/ngày trên App.
+ Đảm bảo toàn bộ CBCNV nắm rõ và thực hiện nghiêm nội dung chỉ thị.
+ Theo dõi, giám sát, phản hồi lỗi và góp ý cải tiến hệ thống trong quá trình triển khai.
=> Hiện PTGĐ Lê Tuấn Anh yêu cầu triển khai ban dự án để rõ đầu mối chủ trì và chịu trách nhiệm chính', 0.8, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND t.title = 'Nghiên cứu phương án triển khai nền tảng VT Move'
WHERE u.name = 'Tạ Minh Trang'
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '2026-01-08'::date
  );

INSERT INTO daily_reports (user_id, task_id, report_date, result, weight, submitted_at)
SELECT u.id, t.id, '2026-01-29'::date, 'Đã báo cáo bản tính toán sơ bộ', 0.8, NOW()
FROM users u
JOIN tasks t ON t.assignee_id = u.id AND t.title = 'Trình ký kế hoạch Chi phí, lợi nhuận năm 2026'
WHERE u.name = 'Phạm Thùy Dương'
  AND NOT EXISTS (
    SELECT 1 FROM daily_reports dr
    WHERE dr.user_id = u.id AND dr.task_id = t.id AND dr.report_date = '2026-01-29'::date
  );
