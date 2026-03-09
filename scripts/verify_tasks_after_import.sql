-- =============================================================================
-- KIỂM TRA SAU KHI CHẠY UPDATE: Dữ liệu đã lên đúng chưa?
-- Chạy từng đoạn để xem: (1) có dòng khớp không, (2) tasks đã được cập nhật chưa.
-- =============================================================================

-- 1) Số dòng import và số dòng tasks khớp (title + ngày giao)
SELECT
  (SELECT COUNT(*) FROM phn_task_import) AS so_dong_import,
  (SELECT COUNT(*) FROM tasks t
   JOIN phn_task_import i
     ON trim(t.title) = trim(i.ten_cong_viec)
     AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')) AS so_dong_khop;

-- 2) Mẫu vài task đã cập nhật: id, title, status, weight, quality, leader_comment, completed_at
SELECT t.id, t.title,
       t.status,
       t.weight,
       t.quality,
       LEFT(t.leader_comment, 50) AS leader_comment_50,
       t.completed_at,
       t.deadline
FROM tasks t
JOIN phn_task_import i
  ON trim(t.title) = trim(i.ten_cong_viec)
  AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')
ORDER BY t.created_at
LIMIT 15;

-- 3) Nếu bảng tasks dùng tên cột khác (vd. task_status thay vì status), kiểm tra cấu trúc:
-- \d tasks
-- (trong psql) hoặc: SELECT column_name FROM information_schema.columns WHERE table_name = 'tasks';
