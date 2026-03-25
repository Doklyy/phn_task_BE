-- ============================================================================
-- PHN Task — Deploy: accepted_at + xóa mềm (deleted_at) / hoàn tác
-- Dành cho PostgreSQL. Chạy một lần trên môi trường (staging/production).
-- Không cần nút FE: API restore hoặc hoàn tác trực tiếp bằng SQL mục 4.
-- ============================================================================

-- 1) Ngày giờ tiếp nhận (báo cáo tiến độ tính từ đây; đã có thì bỏ qua)
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP WITHOUT TIME ZONE;

-- created_at thường là timestamptz (Instant). Nếu lỗi kiểu, đổi dòng SET thành: accepted_at = created_at::timestamp
UPDATE tasks
SET accepted_at = created_at AT TIME ZONE 'UTC'
WHERE accepted_at IS NULL
  AND UPPER(status::text) IN ('ACCEPTED', 'PENDING_APPROVAL', 'COMPLETED', 'PAUSED');

-- 2) Xóa mềm: cột thời điểm xóa (NULL = đang dùng bình thường)
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;

-- 3) Kiểm tra nhanh sau khi chạy
-- SELECT column_name, data_type
-- FROM information_schema.columns
-- WHERE table_name = 'tasks' AND column_name IN ('accepted_at', 'deleted_at');

-- ============================================================================
-- 4) HOÀN TÁC THỦ CÔNG (không cần API/FE): gỡ xóa mềm theo id nhiệm vụ
--    Bảng daily_reports không đụng — task_id giữ nguyên nên báo cáo vẫn khớp.
-- ============================================================================
-- UPDATE tasks SET deleted_at = NULL WHERE id = <ID_NHIEM_VU>;
--
-- Xem thùng rác (đã xóa mềm):
-- SELECT id, title, status, deleted_at FROM tasks WHERE deleted_at IS NOT NULL ORDER BY deleted_at DESC;
