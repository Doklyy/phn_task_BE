-- Thêm cột nhóm (team) và gán 2 nhóm: sản phẩm cũ (Trang, Đoàn, Dương, An) và sản phẩm mới (Nam, Nhất, Khải, Ly)
-- Chạy sau khi đã có bảng users và dữ liệu nhân viên.

-- Nếu cột team đã có thì bỏ qua dòng dưới
ALTER TABLE users ADD COLUMN team VARCHAR(50) NULL;

-- Nhóm sản phẩm cũ: Trưởng nhóm Trang + Đoàn, Dương, An
UPDATE users SET team = 'old_product' WHERE name IN ('Tạ Minh Trang', 'Vũ Văn Đoàn', 'Phạm Thùy Dương', 'Nguyễn An');

-- Nhóm sản phẩm mới: Trưởng nhóm Nam + Nhất, Khải, Ly
UPDATE users SET team = 'new_product' WHERE name IN ('Nguyễn Phụ Nam', 'Trần Minh Nhất', 'Phạm Quang Khải', 'Đỗ Khánh Ly');

-- Admin (nếu có) giữ team = NULL để xem toàn bộ
-- UPDATE users SET team = NULL WHERE role = 'ADMIN';
