-- Thêm cột ngày giờ tiếp nhận nhiệm vụ (báo cáo tiến độ chỉ tính từ đây).
-- Chạy một lần trên DB đang dùng (PostgreSQL). Với H2 dev, có thể để Hibernate ddl-auto tạo cột.

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP;

-- Dữ liệu cũ: gán mốc tạm = created_at cho các task đã không còn NEW (đã từng/đang làm).
UPDATE tasks
SET accepted_at = created_at
WHERE accepted_at IS NULL
  AND UPPER(status) IN ('ACCEPTED', 'PENDING_APPROVAL', 'COMPLETED', 'PAUSED');
