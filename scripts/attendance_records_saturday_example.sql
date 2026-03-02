-- Mẫu dữ liệu chấm công cho ngày Thứ 7 (vd: 28/2) — 5 người đi làm, 3 người nghỉ.
-- Chạy sau khi thay đổi tên bảng/cột và user_id cho đúng DB của bạn.
--
-- Giả định:
-- - Bảng chấm công: attendance_records (hoặc attendance) với các cột: user_id, record_date, attendance_code.
-- - 5 người đi làm T7: Nhất, Khải, Đoàn, An, Ly  → mã đi làm: T7 hoặc TRUC_T7 hoặc SAT_WORK.
-- - 3 người nghỉ T7: Nam, Trang, Dương → mã nghỉ: N_T7 hoặc NGHI_T7 hoặc SAT_OFF.

-- Ví dụ tên bảng: attendance_records
-- Cột: user_id (bigint/uuid), record_date (date), attendance_code (varchar)

-- Bước 1: Thay 101,102,103,104,105 bằng user_id thực của Nhất, Khải, Đoàn, An, Ly.
-- Bước 2: Thay 201,202,203 bằng user_id thực của Nam, Trang, Dương.
-- Bước 3: Đổi tên bảng/cột nếu DB bạn dùng khác (vd: attendance thay vì attendance_records).

-- Ngày Thứ 7 ví dụ: 2026-02-28
-- 5 người đi làm (trực T7) — mã T7 hoặc TRUC_T7
INSERT INTO attendance_records (user_id, record_date, attendance_code)
VALUES
  (101, '2026-02-28', 'T7'),   -- Nhất
  (102, '2026-02-28', 'T7'),   -- Khải
  (103, '2026-02-28', 'T7'),   -- Đoàn
  (104, '2026-02-28', 'T7'),   -- An
  (105, '2026-02-28', 'T7')    -- Ly
ON CONFLICT (user_id, record_date) DO UPDATE SET attendance_code = EXCLUDED.attendance_code;
-- Nếu bảng không có UNIQUE(user_id, record_date) thì dùng INSERT bình thường và tránh insert trùng.

-- 3 người nghỉ thứ 7 — mã N_T7 hoặc NGHI_T7
INSERT INTO attendance_records (user_id, record_date, attendance_code)
VALUES
  (201, '2026-02-28', 'N_T7'),   -- Nam
  (202, '2026-02-28', 'N_T7'),   -- Trang
  (203, '2026-02-28', 'N_T7')    -- Dương
ON CONFLICT (user_id, record_date) DO UPDATE SET attendance_code = EXCLUDED.attendance_code;

-- Nếu DB dùng INSERT thuần (không có ON CONFLICT), có thể chạy từng lệnh và xử lý trùng bằng cách
-- DELETE FROM attendance_records WHERE record_date = '2026-02-28' AND user_id IN (101,102,103,104,105,201,202,203);
-- rồi chạy lại các INSERT trên.
--
-- MySQL: thay ON CONFLICT ... DO UPDATE bằng INSERT IGNORE hoặc
-- INSERT INTO ... ON DUPLICATE KEY UPDATE attendance_code = VALUES(attendance_code);
-- (cần UNIQUE KEY trên (user_id, record_date)).
