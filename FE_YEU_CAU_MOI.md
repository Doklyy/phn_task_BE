# Hướng dẫn Frontend – Yêu cầu mới (Admin, Nhân sự, Chấm công)

## 1. Admin không cần báo cáo ngày / báo cáo công việc

- **Frontend:** Ẩn hoàn toàn phần **Báo cáo ngày** và **Báo cáo công việc** (nút, form, menu) khi user đăng nhập có `role === 'ADMIN'`.
- Backend vẫn nhận gửi báo cáo từ admin nếu FE gửi; chỉ cần FE không hiển thị và không gọi cho admin.

---

## 2. Danh sách Nhân sự – Tách admin ra, thêm/bớt nhân viên

### API danh sách nhân sự (không có admin)

- **GET** `/api/users?currentUserId={id}&personnelOnly=true`
- Trả về danh sách user **không bao gồm ADMIN** (chỉ LEADER, STAFF).
- Dùng cho màn **Nhân sự**: gọi API này thay vì gọi không có `personnelOnly` hoặc có nhưng không set `true`.

### Thêm nhân viên (đã có sẵn)

- **POST** `/api/users` với body có thêm field tùy chọn:
  - `canManageAttendance` (boolean, mặc định false): cấp quyền chấm công cho user mới.

### Bớt nhân viên (mới)

- **DELETE** `/api/users/{id}?adminId={adminId}`
- Chỉ admin. Không xóa được user có role ADMIN. Có thể lỗi 400 nếu user có dữ liệu liên quan (tasks, reports...).

---

## 3. Quyền chấm công – Chỉ admin và người được cấp quyền thấy nút Chấm công

### Dữ liệu từ login

- **LoginResponse** (sau đăng nhập) có thêm field: **`canManageAttendance`** (boolean).
- Logic: **Chỉ hiển thị nút "Chấm công"** khi:
  - `role === 'ADMIN'` **hoặc**
  - `canManageAttendance === true`
- Các user còn lại (STAFF/LEADER không có quyền) **không hiển thị** nút Chấm công.

### Cấp / thu hồi quyền chấm công (Admin)

- **PATCH** `/api/users/{id}/attendance-permission?adminId={adminId}&allowed=true|false`
- Admin gọi để bật (`allowed=true`) hoặc tắt (`allowed=false`) quyền chấm công cho user.

### API chấm công – Thay đổi

- **GET** `/api/attendance/records` bắt buộc thêm query:
  - `currentUserId={id}` (user đang đăng nhập)
  - `userId={id}` (user cần xem bản ghi)
  - Nếu xem bản ghi của người khác mà không phải ADMIN và không có `canManageAttendance` → backend trả 403.

---

## Tóm tắt thay đổi API

| Mục | Thay đổi |
|-----|----------|
| LoginResponse | Thêm `canManageAttendance: boolean` |
| GET /api/users | Thêm query `personnelOnly=true` để lấy danh sách nhân sự (không có admin) |
| POST /api/users (body) | Thêm tùy chọn `canManageAttendance: boolean` |
| PATCH /api/users/{id}/attendance-permission | Mới: bật/tắt quyền chấm công |
| DELETE /api/users/{id}?adminId= | Mới: xóa nhân viên (chỉ admin) |
| GET /api/attendance/records | Bắt buộc thêm `currentUserId` (và giữ `userId`) |

---

## Database

- Bảng `users` có thêm cột **`can_manage_attendance`** (boolean). JPA `ddl-auto=update` tự thêm khi deploy.
