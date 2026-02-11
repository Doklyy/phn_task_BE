-- Gán lại người chủ trì (leader_id, assignee_id) cho từng nhiệm vụ
-- Dựa theo cột "Chủ trì" trong file Quan ly cong viec_PHN.csv
-- Cách dùng:
-- 1. Mở database phn trong HeidiSQL
-- 2. Mở file này và chạy toàn bộ lệnh

USE phn;

-- Helper: ID admin
SET @admin_id := (SELECT id FROM users WHERE username = 'admin' LIMIT 1);

-- 1. Vũ Văn Đoàn
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1)
WHERE title = 'Xây dựng mạng lưới đường đi trục Nam-Bắc, Bắc-Nam theo các hình thức: đường bộ, đường sắt, đường thủy và đường bay';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1)
WHERE title = 'Đánh giá lại quy chế chi trả chi phí phát hàng nặng, đề xuất thay đổi';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1)
WHERE title = 'Xây dựng quy trình giám sát đối với hàng nặng, giảm thiểu việc gian lận cước, ảnh hưởng tới doanh thu hàng nặng';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Vũ Văn Đoàn' LIMIT 1)
WHERE title = 'Báo cáo đánh giá KPI vận hành hàng nặng';

-- 2. Trần Minh Nhất
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Rà soát các khách hàng ngành dược';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Đề xuất các tuyến đi thẳng';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Tối ưu quy trình nhập và thanh quyết toán doanh thu dịch vụ tự thu tự phát, không dùng giấy tờ, ko trình ký văn bản qua voffice';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Tự động tính nguồn V1HG';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Chuyển nhập hàng > 3m về EVTP2';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Trần Minh Nhất' LIMIT 1)
WHERE title = 'Điều chỉnh hệ thống, buộc phải nhập kích thước, trọng lượng với các đơn hàng nặng';

-- 3. Phạm Thùy Dương
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1)
WHERE title = 'Khảo sát danh sách khách hàng hiện hữu chưa sử dụng dv hàng nặng';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1)
WHERE title = 'Trình ký kế hoạch Chi phí, lợi nhuận năm 2026';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1)
WHERE title = 'Hoàn thiện PGNV phòng T2 và đánh giá tháng 1';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1)
WHERE title = 'Tổng kết chương trình tết vẹn toàn, đẩy voucher cho KH';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Thùy Dương' LIMIT 1)
WHERE title = 'Báo cáo kết quả doanh thu các chính sách đã ban hành';

-- 4. Tạ Minh Trang
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1)
WHERE title = 'Nghiên cứu phương án triển khai nền tảng VT Move';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1)
WHERE title = 'Đào tạo CPCT cho TBC toàn quốc';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1)
WHERE title = 'Thống kê các tỉnh đang có đối tác Ahamove và Lalamove, đưa ra phương án hợp tác làm dịch vụ CPCT';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1)
WHERE title = 'Theo dõi hoạt động của bưu cục Xuất Nhập khẩu LCIXNK của chi nhánh LCI';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Tạ Minh Trang' LIMIT 1)
WHERE title = 'Ban hành hướng dẫn triển khai thuê xe cá nhân hoặc hộ kinh doanh để làm dịch vụ tự thu tự phát';

-- 5. Nguyễn An
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Nguyễn An' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn An' LIMIT 1)
WHERE title = 'Báo cáo nghiên cứu thị trường hàng nặng';

-- 6. Phạm Quang Khải
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1)
WHERE title = 'Ban hành hướng dẫn triển khai dịch vụ hàng nặng nội tỉnh';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1)
WHERE title = 'Rà soát Khách hàng lớn mở chặn ( >1,5m hoặc 150Kg)';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1)
WHERE title = 'Rà soát quy hoạch lại dịch vụ phòng';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Phạm Quang Khải' LIMIT 1)
WHERE title = 'Phụ phí nâng hạ Hàng nặng';

-- 7. Nguyễn Phụ Nam
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1)
WHERE title = 'Ban hàng bảng giá đường bay hàng Dâu Tây cho Chi nhánh Sơn La';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1)
WHERE title = 'Ban hành bảng giá bay hàng nặng HNI, HCM và ngược lại';

UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Nguyễn Phụ Nam' LIMIT 1)
WHERE title = 'Xin kinh phí giao dịch cuối năm';

-- 8. Đỗ Khánh Ly
UPDATE tasks
SET assigner_id = @admin_id,
    leader_id   = (SELECT id FROM users WHERE name = 'Đỗ Khánh Ly' LIMIT 1),
    assignee_id = (SELECT id FROM users WHERE name = 'Đỗ Khánh Ly' LIMIT 1)
WHERE title = 'Phát triển phần mềm quản lý công việc';

