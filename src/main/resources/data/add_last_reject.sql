-- Lịch sử trả về: lý do và thời điểm lần trả về tồn đọng gần nhất (Leader/Admin từ chối hoàn thành)
-- Chạy một lần. Nếu cột đã tồn tại thì bỏ qua hoặc comment 2 dòng dưới.
ALTER TABLE tasks ADD COLUMN last_reject_reason VARCHAR(2000) NULL;
ALTER TABLE tasks ADD COLUMN last_reject_at DATETIME NULL;
