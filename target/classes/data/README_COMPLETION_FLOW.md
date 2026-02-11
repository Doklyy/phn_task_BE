# Báo cáo hoàn thành – Luồng duyệt (không cần bảng mới)

## Có cần thêm bảng mới không?

**Không.** Toàn bộ luồng dùng bảng `tasks`:

| Trường trong `tasks` | Ý nghĩa |
|---------------------|--------|
| `status` | NEW → ACCEPTED → **PENDING_APPROVAL** (đợi duyệt) → COMPLETED |
| `completion_note` | Ghi chú báo cáo hoàn thành (assignee gửi) |
| `completion_link` | Link đính kèm |
| `completion_file_path` | Đường dẫn file đính kèm (sau khi upload) |
| `quality`, `completed_at` | Điền khi Leader **duyệt** (chuyển COMPLETED) |

- Assignee bấm **Hoàn thành** → API cập nhật task: `status = PENDING_APPROVAL`, ghi `completion_*`.
- **Leader** (người phân công) **duyệt** → `status = COMPLETED`, `completed_at`, `quality`.
- Leader **từ chối** → `status = ACCEPTED`, xóa `completion_*` (trả về công việc tồn).

Admin chỉ **theo dõi**, không duyệt.

## Nếu thiếu cột (lỗi khi chạy)

Chạy script SQL một lần (nếu cột đã có thì bỏ qua lỗi):

```bash
# Trong thư mục backend
mysql -u root -p phn < src/main/resources/data/add_completion_report.sql
```

Hoặc chạy từng lệnh trong MySQL client; nếu báo "Duplicate column" thì bỏ qua.

## Lỗi 404 khi bấm "Hoàn thành"

1. **Backend đang chạy:** `mvn spring-boot:run` trong thư mục `backend`.
2. **Kiểm tra API:** Mở trình duyệt → `http://localhost:8080/api/health` → phải thấy `{"status":"OK",...}`.
3. **Frontend gọi đúng URL:** Trong DevTools (F12) → tab Network → bấm "Hoàn thành" → xem request: phải là **POST** `http://localhost:8080/api/tasks/42/complete?userId=11` (đúng task id và userId).
4. **.env:** Trong project frontend, file `.env` có `VITE_API_URL=http://localhost:8080/api` (hoặc `http://localhost:8080` – code đã tự thêm `/api`).

Nếu `/api/health` đã OK mà vẫn 404 khi gửi complete → kiểm tra đúng URL trong tab Network và port 8080.
