-- =============================================================================
-- Khôi phục báo cáo tiến độ cho Phạm Quang Khải — chỉ 3 nhiệm vụ, ngày 19 và 20
-- PostgreSQL. Điều chỉnh :YEAR nếu dữ liệu của bạn là 2024/2025 thay vì 2026.
-- =============================================================================

-- Bước 0: Xem id user Khải (chạy riêng, không trong transaction)
-- SELECT id, name FROM users WHERE name ILIKE '%Khải%' OR name ILIKE '%Khai%';

ROLLBACK;
BEGIN;

-- Thay đổi năm nếu cần (ảnh của bạn có tháng 3/2024)
DO $$
DECLARE
  v_assignee_id bigint;
  v_assigner_id bigint;
  d19 date := DATE '2026-03-19';
  d20 date := DATE '2026-03-20';
BEGIN
  SELECT id INTO v_assignee_id
  FROM users
  WHERE translate(lower(coalesce(name, '')),
    'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
    'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd'
  ) LIKE '%phamquangkhai%'
  ORDER BY id
  LIMIT 1;

  IF v_assignee_id IS NULL THEN
    RAISE EXCEPTION 'Không tìm thấy user Phạm Quang Khải. Chạy SELECT id, name FROM users WHERE name ILIKE ''%%Khải%%'';';
  END IF;

  SELECT id INTO v_assigner_id
  FROM users
  WHERE lower(coalesce(role, '')) IN ('admin', 'leader')
  ORDER BY CASE WHEN lower(role) = 'admin' THEN 0 ELSE 1 END, id
  LIMIT 1;

  IF v_assigner_id IS NULL THEN
    RAISE EXCEPTION 'Không tìm thấy admin/leader làm assigner_id.';
  END IF;

  -- Đảm bảo có 3 task (tạo nếu chưa có, cùng ngày giao 19/03)
  INSERT INTO tasks (assigner_id, assignee_id, leader_id, title, objective, content, created_at, deadline, status, weight)
  SELECT v_assigner_id, v_assignee_id, v_assigner_id, v.title, v.title, v.title,
         TIMESTAMP '2026-03-19 08:00:00', TIMESTAMP '2026-03-19 17:00:00', 'ACCEPTED', 0.6
  FROM (VALUES
    ('Rà soát quy hoạch lại dịch vụ phòng'),
    ('Triển khai phụ phí biển đảo ở tỉnh AGG'),
    ('Triển khai phụ phí xăng dầu')
  ) AS v(title)
  WHERE NOT EXISTS (
    SELECT 1 FROM tasks t
    WHERE t.assignee_id = v_assignee_id
      AND trim(t.title) = trim(v.title)
      AND t.created_at::date = d19
  );

  -- Chèn daily_reports cho 3 task x 2 ngày (chỉ khi chưa có)
  INSERT INTO daily_reports (task_id, user_id, report_date, result, created_at, submitted_at)
  SELECT t.id, v_assignee_id, d.dt,
         'Đã báo cáo tiến độ',
         NOW(),
         (d.dt + TIME '17:00:00')
  FROM tasks t
  CROSS JOIN (VALUES (d19), (d20)) AS d(dt)
  WHERE t.assignee_id = v_assignee_id
    AND trim(t.title) IN (
      'Rà soát quy hoạch lại dịch vụ phòng',
      'Triển khai phụ phí biển đảo ở tỉnh AGG',
      'Triển khai phụ phí xăng dầu'
    )
    AND t.created_at::date = d19
    AND NOT EXISTS (
      SELECT 1 FROM daily_reports r
      WHERE r.task_id = t.id AND r.user_id = v_assignee_id AND r.report_date = d.dt
    );
END $$;

COMMIT;

-- Kiểm tra sau khi chạy:
-- SELECT r.task_id, t.title, r.report_date, r.result
-- FROM daily_reports r
-- JOIN tasks t ON t.id = r.task_id
-- WHERE r.report_date IN (DATE '2026-03-19', DATE '2026-03-20')
-- ORDER BY t.title, r.report_date;
