-- Cập nhật completed_at từ bảng import để nhiệm vụ tính đúng tháng (vd. tháng 2).
-- Chạy sau khi đã import CSV vào phn_task_import và đã UPDATE weight/quality/status/leader_comment.

-- Chỉ cập nhật completed_at (bảng tasks không có submitted_at thì bỏ dòng đó).
UPDATE tasks t
SET
  completed_at = to_date(i.ngay_hoan_thanh, 'DD/MM/YYYY')
FROM phn_task_import i
WHERE trim(t.title) = trim(i.ten_cong_viec)
  AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')
  AND i.ngay_hoan_thanh IS NOT NULL
  AND trim(i.ngay_hoan_thanh) <> '';

-- Kiểm tra số dòng khớp (chạy trước khi UPDATE để xem có match không):
-- SELECT t.id, t.title, t.created_at::date, i.ngay_giao, i.ngay_hoan_thanh, i.danh_gia_chi_huy, i.trong_so_cv, i.chat_luong_cv
-- FROM tasks t
-- JOIN phn_task_import i ON trim(t.title) = trim(i.ten_cong_viec) AND t.created_at::date = to_date(i.ngay_giao, 'DD/MM/YYYY')
-- LIMIT 20;
