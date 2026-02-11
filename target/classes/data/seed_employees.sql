-- Seed 8 tài khoản nhân viên theo danh sách
-- Trang & Nam = Trưởng nhóm (LEADER), còn lại = STAFF
-- Mật khẩu mặc định: 123456 (đổi sau khi đăng nhập lần đầu nếu cần)

INSERT INTO users (username, password, name, role, created_at) VALUES
('duong', '123456', 'Phạm Thùy Dương', 'STAFF', NOW()),
('doan', '123456', 'Vũ Văn Đoàn', 'STAFF', NOW()),
('khai', '123456', 'Phạm Quang Khải', 'STAFF', NOW()),
('nhat', '123456', 'Trần Minh Nhất', 'STAFF', NOW()),
('an', '123456', 'Nguyễn An', 'STAFF', NOW()),
('trang', '123456', 'Tạ Minh Trang', 'LEADER', NOW()),
('nam', '123456', 'Nguyễn Phụ Nam', 'LEADER', NOW()),
('ly', '123456', 'Đỗ Khánh Ly', 'STAFF', NOW());
