-- Migration: Thêm cột team và can_manage_attendance vào bảng users (PostgreSQL).
-- Chạy file này trên DB production (Render PostgreSQL) nếu bảng users chưa có 2 cột này.
-- Sau khi chạy xong, tính năng "Quyền chấm công" và "Nhóm" sẽ hoạt động.

SET client_encoding = 'UTF8';

-- Thêm cột team nếu chưa có
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'team'
  ) THEN
    ALTER TABLE users ADD COLUMN team VARCHAR(50) NULL;
  END IF;
END $$;

-- Thêm cột can_manage_attendance nếu chưa có
DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'users' AND column_name = 'can_manage_attendance'
  ) THEN
    ALTER TABLE users ADD COLUMN can_manage_attendance BOOLEAN NULL;
  END IF;
END $$;
