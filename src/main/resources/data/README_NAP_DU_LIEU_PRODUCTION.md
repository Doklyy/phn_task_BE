# Nạp dữ liệu lên Production (Render + PostgreSQL)

## Vì sao dashboard trống (0 nhiệm vụ)?

- **Máy bạn (dev):** Backend nối **MySQL** local, đã chạy seed + import tasks → có đủ dữ liệu.
- **Production (Render):** Backend nối **PostgreSQL** trên Render, **chưa** chạy seed/import → bảng `tasks` (và có thể một phần `users`) trống.

Hai môi trường dùng **hai cơ sở dữ liệu khác nhau**. Dữ liệu bạn thấy trên máy **không** tự lên Render.

## Cách đưa dữ liệu lên PostgreSQL (Render)

### Bước 1: Lấy thông tin kết nối PostgreSQL

1. Vào [dashboard.render.com](https://dashboard.render.com).
2. Chọn **PostgreSQL** (database của backend, ví dụ `phn` hoặc tên bạn đặt).
3. Phần **Connections** → copy **Internal Database URL** (dạng `postgresql://user:pass@host/dbname?sslmode=require`).

### Bước 2: Kết nối và chạy SQL

Dùng một trong hai cách:

**Cách A – Render Shell (trong trình duyệt)**

1. Trong trang PostgreSQL của Render, mở **Shell** (nếu có).
2. Hoặc dùng **psql** từ máy local (cài PostgreSQL client), kết nối bằng Internal URL:
   ```bash
   psql "postgresql://USER:PASSWORD@HOST/DATABASE?sslmode=require"
   ```

**Cách B – Công cụ GUI (DBeaver, pgAdmin, TablePlus...)**

1. Tạo kết nối PostgreSQL mới.
2. Host, port, database, user, password lấy từ Internal Database URL (hoặc từ biến môi trường `DB_URL` của service backend trên Render).

### Bước 3: Chạy lần lượt 2 file SQL

Trong thư mục `backend/src/main/resources/data/` có sẵn:

1. **`seed_production_postgres.sql`**  
   - Thêm user `admin` và các nhân viên (duong, doan, khai, nhat, an, trang, nam, ly).  
   - Dùng `ON CONFLICT (username) DO NOTHING` nên chạy nhiều lần cũng không bị trùng.

2. **`import_tasks_from_csv_postgres.sql`**  
   - Import danh sách nhiệm vụ (gán cho admin + các user ở bước 1).  
   - **Bắt buộc chạy sau** `seed_production_postgres.sql`.

Thứ tự trong psql hoặc GUI:

```text
\i /path/to/seed_production_postgres.sql
\i /path/to/import_tasks_from_csv_postgres.sql
```

Hoặc mở từng file, copy toàn bộ nội dung và Execute.

### Bước 4: Kiểm tra

- Vào [https://phn-task.onrender.com](https://phn-task.onrender.com), đăng nhập (ví dụ `admin` / `123456`).
- Mở **Bảng điều khiển** → sẽ thấy số liệu nhiệm vụ (QUÁ HẠN, ĐANG THỰC HIỆN, HOÀN THÀNH, ...) và biểu đồ đã có dữ liệu.

## Lưu ý

- **Mật khẩu mặc định** trong seed: `123456`. Nên đổi sau lần đăng nhập đầu.
- Nếu production **đã có** user (ví dụ admin với tên khác), vẫn có thể chạy `seed_production_postgres.sql`; user trùng `username` sẽ bị bỏ qua nhờ `ON CONFLICT DO NOTHING`.
- File `import_tasks_from_csv.sql` (không có hậu tố `_postgres`) dành cho **MySQL** (local), không dùng trên Render.
