package vn.phn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.ChangePasswordRequest;
import vn.phn.dto.LoginRequest;
import vn.phn.dto.LoginResponse;
import vn.phn.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class AuthController {

    private final AuthService authService;

    /**
     * API xác thực đăng nhập: kiểm tra username và password.
     * Trả về thông tin user (userId, username, name, role) để FE dùng cho các API khác.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.authenticate(request);
        if (response == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin user theo userId (dùng sau khi đăng nhập).
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<LoginResponse> getUserInfo(@PathVariable Long userId) {
        LoginResponse response = authService.getUserInfo(userId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    /**
     * Nhân viên tự đổi mật khẩu (cần gửi userId + mật khẩu hiện tại + mật khẩu mới).
     */
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        boolean ok = authService.changePassword(request);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
