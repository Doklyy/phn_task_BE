-- ============================================================
-- CHI TIẾT TẠI SAO ĐỨNG ĐẦU BẢNG TOP VINH DANH
-- Công thức: Tổng điểm = Chuyên cần 40% + Chất lượng 60%
--   Kỳ tính: từ mùng 1 tháng hiện tại đến hôm nay — mỗi đầu tháng (mùng 1) điểm reset.
--   - Chuyên cần = Số ngày đã báo cáo / Số ngày làm việc (T2–T6) trong tháng hiện tại
--   - Chất lượng = Tổng(W×Q×T) / Tổng(W); Q = điểm chất lượng (hoặc 1 nếu hoàn thành chưa chấm), T = 1 đúng hạn / 0.5 trễ hoặc chưa đến hạn / 0 quá hạn
-- Chạy bằng psql: psql -U user -d phn -f ranking_top_vinh_danh_detail.sql  (sẽ chạy query PostgreSQL bên dưới)
-- ============================================================

-- ========== POSTGRESQL (chạy bằng psql) ==========
WITH params AS (
  SELECT
    CURRENT_DATE AS end_date,
    date_trunc('month', CURRENT_DATE)::date AS start_date   -- mùng 1 đầu tháng → reset điểm
),
date_range AS (
  SELECT generate_series(
    (SELECT start_date FROM params),
    (SELECT end_date FROM params),
    '1 day'::interval
  )::date AS d
),
working_days_in_period AS (
  SELECT COUNT(*)::bigint AS cnt
  FROM date_range
  WHERE EXTRACT(DOW FROM d) NOT IN (0, 6)  -- 0=CN, 6=T7
),
attendance AS (
  SELECT user_id, COUNT(DISTINCT report_date) AS reported_days
  FROM daily_reports dr, params p
  WHERE dr.report_date BETWEEN p.start_date AND p.end_date
  GROUP BY user_id
),
task_scores AS (
  SELECT
    t.assignee_id AS user_id,
    t.id AS task_id,
    t.title AS task_title,
    t.weight AS w,
    COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) AS q,
    CASE
      WHEN t.completed_at IS NOT NULL THEN CASE WHEN t.completed_at <= t.deadline THEN 1.0 ELSE 0.5 END
      ELSE CASE WHEN NOW() > t.deadline THEN 0.0 ELSE 0.5 END
    END AS t_factor,
    (t.weight * COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) *
     CASE WHEN t.completed_at IS NOT NULL THEN CASE WHEN t.completed_at <= t.deadline THEN 1.0 ELSE 0.5 END ELSE CASE WHEN NOW() > t.deadline THEN 0.0 ELSE 0.5 END END) AS wqt
  FROM tasks t, params p
  WHERE t.deadline IS NOT NULL
    AND t.deadline::date BETWEEN p.start_date AND p.end_date
    AND t.weight IS NOT NULL AND t.weight > 0
),
quality_agg AS (
  SELECT user_id, SUM(w) AS total_assigned_w, SUM(wqt) AS total_achieved
  FROM task_scores
  GROUP BY user_id
)
SELECT
  u.id AS user_id,
  u.name,
  u.username,
  COALESCE(a.reported_days, 0) AS so_ngay_bao_cao,
  (SELECT cnt FROM working_days_in_period) AS so_ngay_lam_viec,
  ROUND((LEAST(1.0::numeric, COALESCE(a.reported_days, 0)::numeric / NULLIF((SELECT cnt FROM working_days_in_period), 0)))::numeric, 4) AS diem_chuyen_can,
  COALESCE(q.total_assigned_w, 0) AS tong_trong_so_giao,
  ROUND((COALESCE(q.total_achieved, 0))::numeric, 4) AS tong_diem_dat_WQT,
  ROUND((LEAST(1.0::numeric, (COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0))::numeric))::numeric, 4) AS diem_chat_luong,
  ROUND((
    (LEAST(1.0::numeric, COALESCE(a.reported_days, 0)::numeric / NULLIF((SELECT cnt FROM working_days_in_period), 0))) * 0.4 +
    (LEAST(1.0::numeric, (COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0))::numeric)) * 0.6
  )::numeric, 4) AS tong_diem,
  RANK() OVER (ORDER BY
    ((LEAST(1.0::numeric, COALESCE(a.reported_days, 0)::numeric / NULLIF((SELECT cnt FROM working_days_in_period), 0))) * 0.4 +
     (LEAST(1.0::numeric, (COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0))::numeric)) * 0.6) DESC NULLS LAST
  ) AS xep_hang
FROM users u
LEFT JOIN attendance a ON a.user_id = u.id
LEFT JOIN quality_agg q ON q.user_id = u.id
WHERE LOWER(COALESCE(u.role, '')) <> 'admin'
ORDER BY tong_diem DESC NULLS LAST, u.id;


-- ============================================================
-- PHẦN 2: CHI TIẾT TỪNG NHIỆM VỤ (tại sao điểm chất lượng lại như vậy)
-- Mỗi dòng = 1 user + 1 task, có W, Q, T và điểm đóng góp W×Q×T
-- Bỏ comment đoạn dưới và chạy riêng nếu cần xem từng task.
-- ============================================================
-- WITH RECURSIVE date_range(d) AS (
--   SELECT @start_date UNION ALL SELECT d + INTERVAL 1 DAY FROM date_range WHERE d < @end_date
-- ),
-- working_days_in_period AS ( SELECT COUNT(*) AS cnt FROM date_range WHERE DAYOFWEEK(d) NOT IN (1, 7) ),
-- attendance AS (
--   SELECT dr.user_id, COUNT(DISTINCT dr.report_date) AS reported_days
--   FROM daily_reports dr WHERE dr.report_date BETWEEN @start_date AND @end_date GROUP BY dr.user_id
-- ),
-- task_detail AS (
--   SELECT t.assignee_id AS user_id, t.id AS task_id, t.title AS task_title, t.status, t.deadline, t.completed_at,
--     t.weight AS w,
--     COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) AS q,
--     CASE WHEN t.completed_at IS NOT NULL THEN IF(t.completed_at <= t.deadline, 1.0, 0.5) ELSE IF(NOW() > t.deadline, 0.0, 0.5) END AS t_factor,
--     (t.weight * COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) *
--      CASE WHEN t.completed_at IS NOT NULL THEN IF(t.completed_at <= t.deadline, 1.0, 0.5) ELSE IF(NOW() > t.deadline, 0.0, 0.5) END) AS wqt
--   FROM tasks t
--   WHERE t.deadline IS NOT NULL AND DATE(t.deadline) BETWEEN @start_date AND @end_date AND t.weight IS NOT NULL AND t.weight > 0
-- )
-- SELECT u.name, td.task_id, td.task_title, td.status, td.w AS trong_so_W, td.q AS chat_luong_Q, td.t_factor AS tien_do_T,
--   ROUND(td.wqt, 4) AS diem_dong_gop_WQT, COALESCE(a.reported_days, 0) AS so_ngay_bao_cao
-- FROM users u JOIN task_detail td ON td.user_id = u.id LEFT JOIN attendance a ON a.user_id = u.id
-- WHERE LOWER(COALESCE(u.role, '')) <> 'admin' ORDER BY u.name, td.task_id;


-- ============================================================
-- MySQL (chỉ khi dùng MySQL/MariaDB — copy ra file khác hoặc bỏ comment để chạy)
-- ============================================================
/*
SET @end_date   = CURDATE();
SET @start_date = DATE_FORMAT(@end_date, '%Y-%m-01');   -- mùng 1 đầu tháng → reset điểm
WITH RECURSIVE date_range(d) AS (
  SELECT @start_date UNION ALL SELECT d + INTERVAL 1 DAY FROM date_range WHERE d < @end_date
),
working_days_in_period AS ( SELECT COUNT(*) AS cnt FROM date_range WHERE DAYOFWEEK(d) NOT IN (1, 7) ),
attendance AS (
  SELECT dr.user_id, COUNT(DISTINCT dr.report_date) AS reported_days
  FROM daily_reports dr WHERE dr.report_date BETWEEN @start_date AND @end_date GROUP BY dr.user_id
),
task_scores AS (
  SELECT t.assignee_id AS user_id, t.id AS task_id, t.title AS task_title, t.weight AS w,
    COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) AS q,
    CASE WHEN t.completed_at IS NOT NULL THEN IF(t.completed_at <= t.deadline, 1.0, 0.5) ELSE IF(NOW() > t.deadline, 0.0, 0.5) END AS t_factor,
    (t.weight * COALESCE(t.quality, CASE WHEN t.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) *
     CASE WHEN t.completed_at IS NOT NULL THEN IF(t.completed_at <= t.deadline, 1.0, 0.5) ELSE IF(NOW() > t.deadline, 0.0, 0.5) END) AS wqt
  FROM tasks t WHERE t.deadline IS NOT NULL AND DATE(t.deadline) BETWEEN @start_date AND @end_date AND t.weight IS NOT NULL AND t.weight > 0
),
quality_agg AS ( SELECT user_id, SUM(w) AS total_assigned_w, SUM(wqt) AS total_achieved FROM task_scores GROUP BY user_id )
SELECT u.id, u.name, u.username, COALESCE(a.reported_days, 0) AS so_ngay_bao_cao, (SELECT cnt FROM working_days_in_period) AS so_ngay_lam_viec,
  ROUND(LEAST(1.0, COALESCE(a.reported_days, 0) * 1.0 / NULLIF((SELECT cnt FROM working_days_in_period), 0)), 4) AS diem_chuyen_can,
  COALESCE(q.total_assigned_w, 0) AS tong_trong_so_giao, ROUND(COALESCE(q.total_achieved, 0), 4) AS tong_diem_dat_WQT,
  ROUND(LEAST(1.0, COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0)), 4) AS diem_chat_luong,
  ROUND(COALESCE((LEAST(1.0, COALESCE(a.reported_days, 0) * 1.0 / NULLIF((SELECT cnt FROM working_days_in_period), 0)) * 0.4) + (LEAST(1.0, COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0)) * 0.6), 0), 4) AS tong_diem,
  RANK() OVER (ORDER BY COALESCE((LEAST(1.0, COALESCE(a.reported_days, 0) * 1.0 / NULLIF((SELECT cnt FROM working_days_in_period), 0)) * 0.4) + (LEAST(1.0, COALESCE(q.total_achieved, 0) / NULLIF(q.total_assigned_w, 0)) * 0.6), 0) DESC) AS xep_hang
FROM users u LEFT JOIN attendance a ON a.user_id = u.id LEFT JOIN quality_agg q ON q.user_id = u.id
WHERE LOWER(COALESCE(u.role, '')) <> 'admin' ORDER BY tong_diem DESC, u.id;
*/
