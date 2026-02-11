-- ============================================================
-- PHN - Hệ thống quản lý công việc + chấm công
-- Chạy toàn bộ file này trên MySQL/MariaDB (port 3307) để tạo lại DB và bảng (không có dữ liệu mẫu).
-- Database: phn. Dữ liệu do ứng dụng Spring Boot tạo (DataLoader) hoặc bạn tự thêm.
-- ============================================================

CREATE DATABASE IF NOT EXISTS phn
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE phn;

-- ------------------------------------------------------------
-- 1. Bảng users
-- ------------------------------------------------------------
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS leave_requests;
DROP TABLE IF EXISTS daily_reports;
DROP TABLE IF EXISTS tasks;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
  id BIGINT NOT NULL AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(150) NOT NULL,
  role VARCHAR(20) NOT NULL,
  team VARCHAR(50) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 2. Bảng tasks
-- ------------------------------------------------------------
CREATE TABLE tasks (
  id BIGINT NOT NULL AUTO_INCREMENT,
  title VARCHAR(500) NOT NULL,
  content VARCHAR(2000) NULL,
  objective VARCHAR(1000) NULL,
  deadline DATETIME NOT NULL,
  weight DOUBLE NOT NULL,
  status VARCHAR(20) NOT NULL,
  quality DOUBLE NULL,
  assigner_id BIGINT NOT NULL,
  leader_id BIGINT NOT NULL,
  assignee_id BIGINT NOT NULL,
  attachment_path VARCHAR(500) NULL,
  completion_note VARCHAR(4000) NULL,
  completion_link VARCHAR(1000) NULL,
  completion_file_path VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  completed_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_tasks_assignee (assignee_id),
  KEY idx_tasks_leader (leader_id),
  KEY idx_tasks_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 3. Bảng daily_reports (báo cáo kết quả ngày)
-- ------------------------------------------------------------
CREATE TABLE daily_reports (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  task_id BIGINT NOT NULL,
  report_date DATE NOT NULL,
  result VARCHAR(4000) NOT NULL,
  weight DOUBLE NULL,
  submitted_at DATETIME NOT NULL,
  attachment_path VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_daily_reports_user (user_id),
  KEY idx_daily_reports_task (task_id),
  KEY idx_daily_reports_date (report_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 4. Bảng leave_requests (đơn xin nghỉ / xin muộn / xin về sớm)
-- ------------------------------------------------------------
CREATE TABLE leave_requests (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  from_date DATE NOT NULL,
  to_date DATE NOT NULL,
  from_time TIME NULL,
  to_time TIME NULL,
  reason VARCHAR(2000) NOT NULL,
  status VARCHAR(20) NOT NULL,
  reviewed_by BIGINT NULL,
  reviewed_at DATETIME(6) NULL,
  reject_reason VARCHAR(500) NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  KEY idx_leave_requests_user (user_id),
  KEY idx_leave_requests_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- 5. Bảng attendance_records (chấm công)
-- ------------------------------------------------------------
CREATE TABLE attendance_records (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  record_date DATE NOT NULL,
  attendance_code VARCHAR(30) NOT NULL,
  points INT NOT NULL,
  check_in_at TIME NULL,
  check_out_at TIME NULL,
  is_late TINYINT(1) NULL,
  is_early_leave TINYINT(1) NULL,
  note VARCHAR(1000) NULL,
  leave_request_id BIGINT NULL,
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uk_attendance_user_date (user_id, record_date),
  KEY idx_attendance_user (user_id),
  KEY idx_attendance_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Nhân viên trong phòng (như cũ)
-- ============================================================

-- Quản trị hệ thống
INSERT INTO users (username, password, name, role, team, created_at) VALUES
('admin', 'admin123', 'Nguyễn Đình Dũng', 'ADMIN', NULL, NOW(6));

-- 8 nhân viên phòng: Trang & Nam = LEADER, còn lại = STAFF. Mật khẩu mặc định: 123456
INSERT INTO users (username, password, name, role, team, created_at) VALUES
('duong', '123456', 'Phạm Thùy Dương', 'STAFF', 'old_product', NOW(6)),
('doan', '123456', 'Vũ Văn Đoàn', 'STAFF', 'old_product', NOW(6)),
('khai', '123456', 'Phạm Quang Khải', 'STAFF', 'new_product', NOW(6)),
('nhat', '123456', 'Trần Minh Nhất', 'STAFF', 'new_product', NOW(6)),
('an', '123456', 'Nguyễn An', 'STAFF', 'old_product', NOW(6)),
('trang', '123456', 'Tạ Minh Trang', 'LEADER', 'old_product', NOW(6)),
('nam', '123456', 'Nguyễn Phụ Nam', 'LEADER', 'new_product', NOW(6)),
('ly', '123456', 'Đỗ Khánh Ly', 'STAFF', 'new_product', NOW(6));

-- ============================================================
-- Ghi chú
-- ============================================================
-- Role: ADMIN, LEADER, STAFF
-- TaskStatus: NEW, ACCEPTED, PENDING_APPROVAL, COMPLETED, PAUSED
-- LeaveRequestType: FULL_DAY, HALF_DAY_MORNING, HALF_DAY_AFTERNOON, LATE_ARRIVAL, EARLY_LEAVE, BEREAVEMENT
-- LeaveRequestStatus: PENDING, APPROVED, REJECTED
-- AttendanceCode: L, N_FULL, N_HALF, N_LATE, N_EARLY, M, V, L_HOLIDAY, T_HOLIDAY, CN, T7, TT7, TCN
-- Team: old_product, new_product (hoặc NULL cho admin)
