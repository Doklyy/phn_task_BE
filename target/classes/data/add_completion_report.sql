-- Báo cáo hoàn thành + trạng thái Đợi duyệt (chạy 1 lần khi nâng cấp)
-- MySQL: nếu báo lỗi "column exists" thì bỏ qua dòng đó.

ALTER TABLE tasks ADD COLUMN completion_note VARCHAR(4000) NULL;
ALTER TABLE tasks ADD COLUMN completion_link VARCHAR(1000) NULL;
ALTER TABLE tasks ADD COLUMN completion_file_path VARCHAR(500) NULL;

-- Đảm bảo cột status đủ dài cho giá trị PENDING_APPROVAL (16 ký tự)
-- ALTER TABLE tasks MODIFY COLUMN status VARCHAR(20) NOT NULL;
