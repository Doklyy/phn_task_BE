-- =============================================================================
-- KIỂM TRA TASK CỦA ANH KHẢI TRONG KHOẢNG 05/03 -> 18/03
-- PostgreSQL
-- =============================================================================

-- 1) Tìm user "Khải"
-- Dùng translate(...) để bỏ dấu tiếng Việt, không cần extension unaccent.
SELECT id, name, username, role
FROM users
WHERE translate(lower(coalesce(name, '')),
  'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
  'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
   OR translate(lower(coalesce(username, '')),
  'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
  'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
ORDER BY id;

-- 2) Danh sách task của user đó có liên quan khoảng ngày 05/03 -> 18/03
-- (lọc theo created_at, deadline, completed_at)
SELECT
  t.id,
  t.title,
  t.status,
  t.created_at::date  AS ngay_giao,
  t.deadline::date    AS han_chot,
  t.completed_at::date AS ngay_hoan_thanh,
  t.assignee_id,
  t.leader_id
FROM tasks t
JOIN users u ON u.id = t.assignee_id
WHERE (
    translate(lower(coalesce(u.name, '')),
      'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
      'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
    OR translate(lower(coalesce(u.username, '')),
      'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
      'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
  )
  AND (
    t.created_at::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
    OR t.deadline::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
    OR t.completed_at::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
  )
ORDER BY coalesce(t.created_at::date, t.deadline::date), t.id;

-- 3) Tổng hợp theo trạng thái
SELECT
  t.status,
  COUNT(*) AS so_luong
FROM tasks t
JOIN users u ON u.id = t.assignee_id
WHERE (
    translate(lower(coalesce(u.name, '')),
      'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
      'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
    OR translate(lower(coalesce(u.username, '')),
      'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
      'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd') LIKE '%khai%'
  )
  AND (
    t.created_at::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
    OR t.deadline::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
    OR t.completed_at::date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
  )
GROUP BY t.status
ORDER BY t.status;

