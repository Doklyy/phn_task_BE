# PHN Backend - Spring Boot

Backend cho hệ thống quản lý công việc PHN: Database, phân quyền 3 cấp, API, WQT, bộ lọc và xuất Excel.

## Yêu cầu

- JDK 17+
- Maven 3.6+

## Chạy

```bash
cd backend
mvn spring-boot:run
```

Server chạy tại `http://localhost:8080`. Console H2: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:file:./data/phn`, user: `sa`, password để trống).

## Cơ sở dữ liệu

- **MySQL** (localhost:3307, database: `phn`), cấu hình trong `application.properties`.
- **Bảng:** `users`, `tasks`, `daily_reports`.
- **Dữ liệu mẫu** (tự tạo lần đầu):
  - Admin: username `admin`, password `admin123`
  - Leader: username `leader`, password `leader123`
  - Staff: username `staff`, password `staff123`
  - 2 task mẫu (1 NEW, 1 ACCEPTED).

## Phân quyền (3 cấp)

| Vai trò | Quyền |
|--------|--------|
| **ADMIN** | Xem/sửa tất cả task, tạo task, đánh giá, xuất Excel toàn hệ thống |
| **LEADER** | Giao việc, chỉnh sửa task đã giao, đánh giá nhóm, xem task của mình + assignee |
| **STAFF** | Xem task được giao, tiếp nhận, báo cáo kết quả ngày |

Backend nhận `userId` (Long) qua query/param; role lấy từ DB theo `userId`.

## API

Base URL: `http://localhost:8080/api`

### Kiểm tra backend (Health)
- `GET /api/health` – Kiểm tra backend đang chạy và base path đúng. Trả về `{ "status": "OK", "message": "..." }`. Dùng để debug lỗi 404 (gọi thử trong trình duyệt: `http://localhost:8080/api/health`).

### Authentication (Xác thực)
- `POST /api/auth/login` – Đăng nhập (body: `username`, `password`). Trả về `userId`, `username`, `name`, `role`, `token`.
- `GET /api/auth/user/{userId}` – Lấy thông tin user theo userId

### Users
- `GET /api/users` – Danh sách user
- `GET /api/users/{id}` – Chi tiết user
- `GET /api/users/role/{ADMIN|LEADER|STAFF}` – Lọc theo vai trò

### Tasks
- `GET /api/tasks?userId=1` – Danh sách task theo quyền (userId = id user đăng nhập). **Tự động sắp xếp:** NEW/ACCEPTED lên trên, COMPLETED xuống dưới, sau đó theo deadline.
- `GET /api/tasks?userId=1&filter=month&year=2024&month=5` – Lọc theo tháng
- `GET /api/tasks?userId=1&filter=quarter&year=2024&quarter=2` – Lọc theo quý
- `GET /api/tasks?userId=1&filter=year&year=2024` – Lọc theo năm
- `POST /api/tasks?assignerId=1` – Tạo task (body: CreateTaskRequest JSON)
- `PATCH /api/tasks/{taskId}/accept?userId=3` – Tiếp nhận task (NEW → ACCEPTED)
- `PATCH /api/tasks/{taskId}?userId=1&status=COMPLETED&quality=0.9` – Cập nhật trạng thái/chất lượng (Leader/Admin)
- `GET /api/tasks/export?userId=1` – Xuất Excel (thêm `filter`, `year`, `month`/`quarter` nếu cần)

### Upload file
- **`POST /api/upload`** – Upload file (multipart/form-data, field name: `file`). Trả về `{ "path": "uploads/xxx.ext" }`. Dùng path này gửi kèm khi gửi báo cáo hoàn thành hoặc báo cáo ngày. Thư mục lưu: `uploads/` (cấu hình `app.upload.dir` trong `application.properties`).

### Báo cáo ngày (Lịch sử báo cáo)
- `POST /api/reports?userId=3` – Gửi báo cáo (body: `taskId`, `reportDate`, `result`, `weight`, `attachmentPath` tùy chọn). Thời gian gửi = `submittedAt` (tự động = thời điểm nhấn Gửi). File đính kèm: upload qua `/api/upload` rồi gửi `attachmentPath` trong body.
- `GET /api/reports?userId=3` – Danh sách báo cáo của user (sắp xếp theo ngày giảm dần)

### Chấm điểm (Scoring)
- `GET /api/scoring/user/{userId}` – Tính điểm chuyên cần và chất lượng (WQT) cho một user:
  - **Điểm chuyên cần:** Tỷ lệ số ngày báo cáo / số ngày làm việc (30 ngày gần nhất)
  - **Điểm chất lượng:** WQT trung bình từ các task đã hoàn thành có quality
  - **Tổng điểm:** (chuyên cần × 0.4) + (chất lượng × 0.6)
- `GET /api/scoring/ranking` – Bảng xếp hạng điểm cho tất cả user (giảm dần theo tổng điểm)

## WQT

Công thức: **WQT = weight × quality** (quality 0..1 do Leader đánh giá). Cập nhật `quality` qua `PATCH /api/tasks/{id}`.

## Tính năng đặc biệt

### Logic lọc tự động
Khi người dùng truy cập, BE tự động sắp xếp danh sách task:
- **Công việc chưa hoàn thành** (NEW, ACCEPTED) → **lên trên cùng**
- **Công việc đã hoàn thành** (COMPLETED) → **xuống dưới**
- Sau đó sắp xếp theo deadline (sớm nhất trước)

### Lưu trữ lịch sử báo cáo
- Ghi lại **nội dung báo cáo** (`result`)
- **Thời gian thực hiện** (`submittedAt`): bao gồm cả ngày và giờ (tự động = lúc nhấn Gửi)
- **File đính kèm** (`attachmentPath`): đường dẫn file

### Hệ thống chấm điểm tự động
- **Điểm chuyên cần:** Dựa trên số ngày báo cáo trong 30 ngày gần nhất
- **Điểm chất lượng (WQT):** Từ các task đã hoàn thành có đánh giá quality
- **Tổng điểm:** Công thức (chuyên cần × 40%) + (chất lượng × 60%)

## Kết nối Frontend

1. **Đăng nhập:** `POST /api/auth/login` với `username` và `password` → nhận `userId`, `role`
2. **Lấy task:** `GET /api/tasks?userId={userId}` → danh sách đã tự động sắp xếp (chưa hoàn thành lên trên)
3. **Gửi báo cáo:** `POST /api/reports?userId={userId}` → lưu với `submittedAt` = thời điểm gửi
4. **Xem điểm:** `GET /api/scoring/user/{userId}` → điểm chuyên cần, chất lượng, tổng điểm

Cấu hình FE (Vite): trong `.env` đặt `VITE_API_URL=http://localhost:8080/api`.
