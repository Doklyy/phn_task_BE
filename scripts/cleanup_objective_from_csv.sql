-- =============================================================================
-- DỌN DỮ LIỆU OBJECTIVE: Xóa các mục tiêu chứa "Mục tiêu từ file CSV"
-- Áp dụng cho PostgreSQL.
-- =============================================================================

BEGIN;

-- 1) Xem trước các dòng sẽ bị tác động
SELECT id, title, objective
FROM tasks
WHERE COALESCE(objective, '') <> ''
  AND (
    objective ILIKE '%Mục tiêu từ file CSV%'
    OR objective ILIKE '%Muc tieu tu file CSV%'
  )
ORDER BY id;

-- 2) Xóa nội dung objective không mong muốn (đặt về NULL)
UPDATE tasks
SET objective = NULL
WHERE COALESCE(objective, '') <> ''
  AND (
    objective ILIKE '%Mục tiêu từ file CSV%'
    OR objective ILIKE '%Muc tieu tu file CSV%'
  );

-- 3) Kiểm tra lại sau cập nhật
SELECT COUNT(*) AS remain_rows
FROM tasks
WHERE COALESCE(objective, '') <> ''
  AND (
    objective ILIKE '%Mục tiêu từ file CSV%'
    OR objective ILIKE '%Muc tieu tu file CSV%'
  );

COMMIT;

