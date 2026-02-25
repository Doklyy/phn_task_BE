# Lịch sử trả về nhiệm vụ

Khi Leader/Admin bấm **Trả về tồn đọng** và nhập lý do, backend lưu vào bảng `tasks`.

## Chạy migration (một lần)

Nếu database chưa có cột, chạy:

```sql
-- Trong thư mục backend/src/main/resources/data/
-- File: add_last_reject.sql
ALTER TABLE tasks ADD COLUMN last_reject_reason VARCHAR(2000) NULL;
ALTER TABLE tasks ADD COLUMN last_reject_at DATETIME NULL;
```

Sau đó ứng dụng sẽ trả về `lastRejectReason` và `lastRejectAt` trong API task, và giao diện sẽ hiển thị **Lịch sử trả về** trong chi tiết nhiệm vụ (cho cả người thực hiện và Admin).
