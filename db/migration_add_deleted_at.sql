-- Xóa mềm nhiệm vụ (undo restore). Chạy một lần trên DB production.

ALTER TABLE tasks ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
