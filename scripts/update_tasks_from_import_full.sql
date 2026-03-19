-- =============================================================================
-- CẬP NHẬT TASKS TỪ FILE "Theo dõi Nhiệm vụ" — DỮ LIỆU THÁNG 2
-- Mapping CSV: Ngày giao, Người giao, Tên công việc, Nội dung cụ thể, Deadline,
--   Ngày hoàn thành, Chủ trì, Đánh giá chỉ huy, Trọng số CV, Chất lượng CV,
--   Trạng thái CV, Tổng điểm. Điểm tính cho tháng 2.
-- Bảng import: phn_task_import (cột: ngay_giao, nguoi_giao, ten_cong_viec,
--   noi_dung_cu_the, deadline, ngay_hoan_thanh, chu_tri, danh_gia_chi_huy,
--   trong_so_cv, chat_luong_cv, trang_thai_cv, tong_diem).
-- =============================================================================

-- Bước 0: Kiểm tra số dòng khớp (title + ngày giao). Nếu = 0 thì kiểm tra lại dữ liệu import.
/*
SELECT COUNT(*) AS so_dong_khop
FROM tasks t
JOIN phn_task_import i
  ON trim(t.title) = trim(i.ten_cong_viec)
  AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY');
*/

-- Bước 1: Cập nhật đầy đủ — nội dung, deadline, đánh giá, trọng số, chất lượng, trạng thái, completed_at (tháng 2)
UPDATE tasks t
SET
  created_at = COALESCE(t.created_at, to_date(i.ngay_giao, 'DD/MM/YYYY')),
  deadline   = CASE
    WHEN i.deadline IS NOT NULL AND trim(i.deadline) <> ''
    THEN to_date(i.deadline, 'DD/MM/YYYY')
    ELSE t.deadline
  END,
  content    = CASE WHEN i.noi_dung_cu_the IS NOT NULL AND trim(i.noi_dung_cu_the) <> '' THEN trim(i.noi_dung_cu_the) ELSE t.content END,
  objective  = CASE WHEN i.ket_qua_thuc_hien IS NOT NULL AND trim(i.ket_qua_thuc_hien) <> '' THEN trim(i.ket_qua_thuc_hien) ELSE t.objective END,
  leader_comment = i.danh_gia_chi_huy,
  weight = COALESCE(
    CASE trim(lower(i.trong_so_cv))
      WHEN 'rất thấp'    THEN 0.2
      WHEN 'thấp'        THEN 0.4
      WHEN 'bình thường' THEN 0.6
      WHEN 'cao'         THEN 0.8
      WHEN 'rất cao'     THEN 1.0
      ELSE t.weight
    END,
    t.weight
  ),
  quality = COALESCE(
    CASE trim(lower(i.chat_luong_cv))
      WHEN 'không đạt'     THEN 0.0
      WHEN 'đạt tối thiểu' THEN 0.6
      WHEN 'đạt chuẩn'     THEN 1.0
      WHEN 'tốt'           THEN 1.1
      WHEN 'xuất sắc'      THEN 1.3
      ELSE t.quality
    END,
    t.quality
  ),
  status = COALESCE(
  CASE trim(lower(i.trang_thai_cv))
    WHEN 'hoàn thành đúng hạn' THEN 'COMPLETED'
    WHEN 'hoàn thành sau hạn'  THEN 'COMPLETED'
    WHEN 'đang thực hiện'      THEN 'ACCEPTED'
    WHEN 'tạm dừng'            THEN 'PAUSED'
    WHEN 'không hoàn thành'    THEN 'NEW'
    ELSE t.status
  END,
  t.status
),
  -- Ngày hoàn thành: nếu CSV có thì dùng; nếu trống nhưng trạng thái là Hoàn thành thì đặt trong tháng 2 (để tính điểm tháng 2)
  completed_at = CASE
    WHEN i.ngay_hoan_thanh IS NOT NULL AND trim(i.ngay_hoan_thanh) <> ''
    THEN to_date(i.ngay_hoan_thanh, 'DD/MM/YYYY')
    WHEN trim(lower(i.trang_thai_cv)) IN ('hoàn thành đúng hạn', 'hoàn thành sau hạn')
    THEN CASE
      WHEN i.deadline IS NOT NULL AND trim(i.deadline) <> ''
      THEN LEAST(to_date(i.deadline, 'DD/MM/YYYY'), '2026-02-28'::date)
      ELSE '2026-02-28'::date
    END
    ELSE t.completed_at
  END
FROM phn_task_import i
WHERE trim(t.title) = trim(i.ten_cong_viec);

-- Bước 2 (tùy chọn): Cập nhật người thực hiện (Chủ trì) nếu bảng users có cột name trùng với chu_tri
-- Chạy nếu bạn đã có bảng users với name và cần gán assignee_id theo tên Chủ trì.
/*
UPDATE tasks t
SET assignee_id = u.id
FROM phn_task_import i
JOIN users u ON trim(u.name) = trim(i.chu_tri)   -- hoặc u.full_name, tùy schema
WHERE trim(t.title) = trim(i.ten_cong_viec)
  AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')
  AND u.id IS NOT NULL;
*/
