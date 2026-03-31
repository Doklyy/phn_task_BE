-- =============================================================================
-- Bổ sung báo cáo tiến độ cho anh Phạm Quang Khải từ ngày 05 -> 18 (tháng 03/2026)
-- PostgreSQL. Nếu khác năm/tháng thì sửa 2 biến d_from, d_to bên dưới.
-- =============================================================================

ROLLBACK;
BEGIN;

DO $$
DECLARE
  v_user_id bigint;
  d_from date := DATE '2026-03-05';
  d_to   date := DATE '2026-03-18';
BEGIN
  -- Tìm user Khải (không phụ thuộc dấu tiếng Việt)
  SELECT id INTO v_user_id
  FROM users
  WHERE translate(lower(coalesce(name, '')),
    'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
    'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd'
  ) LIKE '%phamquangkhai%'
  ORDER BY id
  LIMIT 1;

  IF v_user_id IS NULL THEN
    RAISE EXCEPTION 'Không tìm thấy user Phạm Quang Khải.';
  END IF;

  /*
   * Chèn báo cáo cho mọi task của Khải trong từng ngày từ d_from..d_to nếu:
   * - task đã được giao trước/đúng ngày đó
   * - task không bị xóa mềm
   * - task không ở trạng thái NEW/PAUSED
   * - nếu có accepted_at: chỉ tính từ ngày accepted_at trở đi
   * - nếu có completed_at: chỉ tính tới ngày completed_at
   * - chưa có báo cáo cho (task, user, day) thì mới chèn
   */
  INSERT INTO daily_reports (task_id, user_id, report_date, result, created_at, submitted_at)
  SELECT
    t.id,
    v_user_id,
    g.dt::date AS report_date,
    'Đã báo cáo tiến độ (khôi phục dữ liệu)',
    NOW(),
    (g.dt::date + TIME '17:00:00')
  FROM tasks t
  JOIN generate_series(d_from, d_to, INTERVAL '1 day') AS g(dt) ON TRUE
  WHERE t.assignee_id = v_user_id
    AND t.created_at::date <= g.dt::date
    AND (t.deleted_at IS NULL)
    AND UPPER(COALESCE(t.status::text, '')) NOT IN ('NEW', 'PAUSED')
    AND (t.accepted_at IS NULL OR t.accepted_at::date <= g.dt::date)
    AND (t.completed_at IS NULL OR t.completed_at::date >= g.dt::date)
    AND NOT EXISTS (
      SELECT 1
      FROM daily_reports r
      WHERE r.task_id = t.id
        AND r.user_id = v_user_id
        AND r.report_date = g.dt::date
    );
END $$;

COMMIT;

-- Kiểm tra nhanh sau khi chạy:
-- SELECT r.report_date, r.task_id, t.title
-- FROM daily_reports r
-- JOIN tasks t ON t.id = r.task_id
-- WHERE r.user_id = (
--   SELECT id FROM users
--   WHERE translate(lower(coalesce(name, '')),
--     'àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđ',
--     'aaaaaaaaaaaaaaaaaeeeeeeeeeeeiiiiiooooooooooooooooouuuuuuuuuuuyyyyyd'
--   ) LIKE '%phamquangkhai%'
--   ORDER BY id LIMIT 1
-- )
-- AND r.report_date BETWEEN DATE '2026-03-05' AND DATE '2026-03-18'
-- ORDER BY r.report_date, r.task_id;
