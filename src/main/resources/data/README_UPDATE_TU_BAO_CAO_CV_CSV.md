# Cập nhật dashboard từ sheet "Báo cáo CV cuối ngày" (CSV)

Dữ liệu từ file CSV (export sheet **Báo cáo CV cuối ngày** của file Quản lý công việc PHN) được dùng để:

1. **UPDATE tasks**: cập nhật trạng thái (Tiến độ → status), trọng số (Trọng số CV → weight), chất lượng (Chất lượng CV → quality), mục tiêu/kết quả (→ objective), ngày hoàn thành (→ completed_at).
2. **INSERT daily_reports**: thêm báo cáo cuối ngày để dashboard theo dõi hiển thị đúng (mỗi dòng có "Kết quả thực hiện" sẽ tạo một bản ghi báo cáo theo ngày).

## Cách làm

### Bước 1: Export CSV từ Google Sheet / Excel

- Mở file **Quản lý công việc PHN**, chọn sheet **"Báo cáo CV cuối ngày"**.
- Export (hoặc lưu) thành file CSV, phân cách **dấu chấm phẩy (;)**, encoding **UTF-8**.
- Đặt file vào: `backend/target/classes/data/Quan ly cong viec_PHN.csv`  
  (hoặc dùng đường dẫn khác và truyền vào bước 2).

### Bước 2: Sinh file SQL từ CSV

Trong thư mục **backend**:

```bash
python src/main/resources/data/update_from_bao_cao_cv_csv.py
```

Hoặc chỉ rõ đường dẫn CSV:

```bash
python src/main/resources/data/update_from_bao_cao_cv_csv.py "target/classes/data/Quan ly cong viec_PHN.csv"
```

Script sẽ tạo file: `src/main/resources/data/update_dashboard_from_bao_cao_cv.sql`.

### Bước 3: Chạy SQL trên PostgreSQL (Render)

1. Vào [Render Dashboard](https://dashboard.render.com) → chọn dịch vụ PostgreSQL của bạn.
2. Lấy **Connection string** (hoặc host, database, user, password).
3. Chạy nội dung file `update_dashboard_from_bao_cao_cv.sql`:

   - Trên máy local (nếu đã cài `psql`):
     ```bash
     psql "postgresql://user:pass@host/dbname?sslmode=require" -f src/main/resources/data/update_dashboard_from_bao_cao_cv.sql
     ```
   - Hoặc dùng công cụ SQL (pgAdmin, DBeaver, …) kết nối tới DB Render và paste/chạy từng phần hoặc cả file.

Sau khi chạy xong:

- **Tasks** được cập nhật theo Tiến độ, Trọng số, Chất lượng, Kết quả và Ngày hoàn thành trong CSV.
- **daily_reports** có thêm các bản ghi báo cáo cuối ngày → dashboard theo dõi (và báo cáo hàng ngày) sẽ hiển thị đúng dữ liệu từ sheet.

## Ánh xạ cột CSV → DB

| Cột CSV | Dùng cho |
|--------|----------|
| Tên công việc | Ghép với tasks.title (và Chủ trì → assignee) |
| Chủ trì | users.name → assignee_id |
| Tiến độ | Đang thực hiện → ACCEPTED, Hoàn thành → COMPLETED |
| Trọng số CV | Rất cao→0.8, Cao→0.6, Bình thường→0.4, Rất thấp→0.2 |
| Chất lượng CV | Đạt chuẩn→0.9, Không đạt→0.3 |
| Kết quả thực hiện hoặc mục tiêu cần đạt | tasks.objective và daily_reports.result |
| Ngày hoàn thành / Ngày giao | tasks.completed_at, daily_reports.report_date |

## Lưu ý

- Task trong DB phải trùng **title** và **assignee** (theo tên Chủ trì) thì mới cập nhật/ghép đúng. Nếu chưa có task tương ứng, cần import tasks trước (ví dụ bằng `import_tasks_from_csv_postgres.sql`).
- Script **không xóa** báo cáo cũ; chỉ **thêm** bản ghi daily_reports nếu chưa tồn tại (cùng user, task, report_date).
