-- =============================================================================
-- TẠI SAO UPDATE 0? Chạy từng bước để tìm nguyên nhân.
-- =============================================================================

-- 1) Bảng import có dữ liệu không? Và ngày giao có đúng format DD/MM/YYYY không?
SELECT 'phn_task_import' AS bang, COUNT(*) AS so_dong FROM phn_task_import
UNION ALL
SELECT 'tasks' AS bang, COUNT(*) AS so_dong FROM tasks;

-- 2) Mẫu title + ngày từ import (5 dòng đầu)
SELECT ten_cong_viec, ngay_giao, trong_so_cv, chat_luong_cv, trang_thai_cv
FROM phn_task_import
LIMIT 5;

-- 3) Mẫu title + created_at từ tasks (5 dòng đầu)
SELECT id, title, created_at, created_at::date AS created_date
FROM tasks
ORDER BY id
LIMIT 5;

-- 4) Thử khớp CHỈ THEO TITLE (bỏ điều kiện ngày) — xem có trùng tên không?
SELECT i.ten_cong_viec AS title_import,
       t.title AS title_tasks,
       t.created_at::date AS task_created_date,
       i.ngay_giao AS import_ngay_giao,
       to_date(i.ngay_giao, 'DD/MM/YYYY') AS import_ngay_parsed
FROM phn_task_import i
LEFT JOIN tasks t ON trim(t.title) = trim(i.ten_cong_viec)
LIMIT 10;

-- 5) Với những task có title trùng import: so sánh ngày (created_at vs ngay_giao)
SELECT t.id, t.title,
       t.created_at::date AS task_created_date,
       i.ngay_giao AS import_ngay_giao,
       to_date(i.ngay_giao, 'DD/MM/YYYY') AS import_parsed,
       (t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')) AS ngay_co_khop
FROM tasks t
JOIN phn_task_import i ON trim(t.title) = trim(i.ten_cong_viec)
LIMIT 15;

-- 6) Nếu câu 4 trả về 0 dòng → không có title trùng. Khi đó xem title trong DB khác gì CSV:
--    Bỏ comment 2 dòng dưới để in ra vài title từ mỗi bảng, so sánh tay.
-- SELECT 'TASKS' AS nguon, title FROM tasks LIMIT 10;
-- SELECT 'IMPORT' AS nguon, ten_cong_viec AS title FROM phn_task_import LIMIT 10;
