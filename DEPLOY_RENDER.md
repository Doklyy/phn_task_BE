# Deploy Backend lên Render

## 1. Tạo PostgreSQL (Free)

1. Đăng ký / đăng nhập [render.com](https://render.com).
2. Dashboard → **New +** → **PostgreSQL**.
3. Chọn Free, đặt tên (vd: `phn-db`) → **Create Database**.
4. Khi tạo xong, vào database → tab **Info** (hoặc **Connect**), ghi lại:
   - **Internal Database URL** (chỉ dùng giữa các service trong Render), hoặc
   - **Host**, **Port**, **Database**, **Username**, **Password**.

Cách lấy JDBC URL:
- Host: `dpg-xxxx-a.oregon-postgres.render.com`
- Port: `5432`
- Database: `phn` (hoặc tên bạn đặt)
- **DB_URL** = `jdbc:postgresql://<Host>:<Port>/<Database>`
  - Ví dụ: `jdbc:postgresql://dpg-xxx.oregon-postgres.render.com:5432/phn`

---

## 2. Tạo Web Service (Backend)

1. **New +** → **Web Service**.
2. Kết nối repo GitHub chứa **backend** (repo có folder `backend` hoặc repo riêng backend).
3. Cấu hình:
   - **Name**: `phn-backend` (tùy chọn).
   - **Region**: Oregon (US West) hoặc gần bạn.
   - **Branch**: `main`.
   - **Root Directory**: Nếu code backend nằm trong subfolder thì điền `backend`, không thì để trống.
   - **Runtime**: Java.
   - **Build Command**:
     ```bash
     ./mvnw clean package -DskipTests
     ```
     (Nếu không có `mvnw`: `mvn clean package -DskipTests`)
   - **Start Command**:
     ```bash
     java -jar target/phn-backend-1.0.0.jar
     ```

4. **Environment** (Environment Variables):
   - `SPRING_PROFILES_ACTIVE` = `prod`
   - `DB_URL` = `jdbc:postgresql://<HOST>:<PORT>/<DATABASE>`
   - `DB_USER` = username Postgres
   - `DB_PASSWORD` = password Postgres

5. **Create Web Service**.

Sau khi deploy xong, Render cấp URL kiểu: `https://phn-backend.onrender.com`.  
API base: `https://phn-backend.onrender.com/api`.

---

## 3. Cho phép CORS (Frontend gọi API)

Backend đã cấu hình `@CrossOrigin` cho `localhost:5173`. Khi frontend deploy lên Netlify/Vercel, cần thêm origin của domain đó vào (sửa trong code hoặc dùng biến môi trường).
