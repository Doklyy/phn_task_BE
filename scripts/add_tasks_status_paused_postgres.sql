-- Chạy script này trên PostgreSQL (prod) để cho phép status PAUSED (Tạm dừng).
-- Lỗi: new row for relation "tasks" violates check constraint "tasks_status_check"
-- Nguyên nhân: constraint được tạo lúc trước khi thêm PAUSED vào enum TaskStatus.

-- Bỏ constraint cũ (chỉ liệt kê các status cũ)
ALTER TABLE tasks DROP CONSTRAINT IF EXISTS tasks_status_check;

-- Thêm lại constraint với đủ giá trị: NEW, ACCEPTED, PAUSED, PENDING_APPROVAL, COMPLETED
ALTER TABLE tasks ADD CONSTRAINT tasks_status_check CHECK (
  status IN ('NEW', 'ACCEPTED', 'PAUSED', 'PENDING_APPROVAL', 'COMPLETED')
);
