-- Seed users cho PostgreSQL (Render production)
-- Chay file nay TRUOC khi chay import_tasks_from_csv_postgres.sql
-- Mat khau mac dinh: 123456
SET client_encoding = 'UTF8';

INSERT INTO users (username, password, name, role, created_at)
VALUES
  ('admin', '123456', 'Nguyễn Đình Dũng', 'ADMIN', NOW()),
  ('duong', '123456', 'Phạm Thùy Dương', 'STAFF', NOW()),
  ('doan', '123456', 'Vũ Văn Đoàn', 'STAFF', NOW()),
  ('khai', '123456', 'Phạm Quang Khải', 'STAFF', NOW()),
  ('nhat', '123456', 'Trần Minh Nhất', 'STAFF', NOW()),
  ('an', '123456', 'Nguyễn An', 'STAFF', NOW()),
  ('trang', '123456', 'Tạ Minh Trang', 'LEADER', NOW()),
  ('nam', '123456', 'Nguyễn Phụ Nam', 'LEADER', NOW()),
  ('ly', '123456', 'Đỗ Khánh Ly', 'STAFF', NOW())
ON CONFLICT (username) DO NOTHING;
