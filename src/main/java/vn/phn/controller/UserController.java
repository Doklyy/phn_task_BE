package vn.phn.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.phn.dto.CreateUserRequest;
import vn.phn.dto.SetPasswordRequest;
import vn.phn.dto.UserDto;
import vn.phn.entity.Role;
import vn.phn.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173", "https://phn-task.onrender.com"})
public class UserController {

    private final UserService userService;

    /**
     * GET /api/users → tất cả (admin).
     * GET /api/users?currentUserId=2 → theo quyền: admin = tất cả, leader = cùng nhóm, staff = chỉ mình.
     * GET /api/users?currentUserId=1&personnelOnly=true → danh sách nhân sự (loại ADMIN, dùng cho màn Nhân sự).
     */
    @GetMapping
    public ResponseEntity<List<UserDto>> findAll(
            @RequestParam(required = false) Long currentUserId,
            @RequestParam(required = false, defaultValue = "false") boolean personnelOnly) {
        if (currentUserId != null) {
            return ResponseEntity.ok(userService.findUsersForCurrentUser(currentUserId, personnelOnly));
        }
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        UserDto dto = userService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserDto>> findByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.findByRole(role));
    }

    /**
     * Admin tạo tài khoản mới.
     * FE truyền creatorId (id của admin đang đăng nhập) qua query param.
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody CreateUserRequest req,
            @RequestParam Long creatorId) {
        UserDto created = userService.createUser(req, creatorId);
        if (created == null) {
            // Không có quyền hoặc username đã tồn tại
            return ResponseEntity.status(400).build();
        }
        return ResponseEntity.ok(created);
    }

    /**
     * Admin đặt lại mật khẩu cho user. Gọi: PUT /api/users/{id}/password?adminId=1
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> setPassword(
            @PathVariable Long id,
            @Valid @RequestBody SetPasswordRequest req,
            @RequestParam Long adminId) {
        boolean ok = userService.setPasswordByAdmin(id, req, adminId);
        return ok ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    /**
     * Admin cập nhật role cho user.
     * Gọi: PATCH /api/users/{id}/role?adminId=1&role=ADMIN
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserDto> updateRole(
            @PathVariable Long id,
            @RequestParam Role role,
            @RequestParam Long adminId) {
        UserDto updated = userService.updateRoleByAdmin(id, role, adminId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Admin cập nhật nhóm cho user. PATCH /api/users/{id}/team?adminId=1&team=old_product
     */
    @PatchMapping("/{id}/team")
    public ResponseEntity<UserDto> updateTeam(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam(required = false) String team) {
        UserDto updated = userService.updateTeamByAdmin(id, team, adminId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Admin bật/tắt quyền chấm công cho user.
     * PATCH /api/users/{id}/attendance-permission?adminId=1&allowed=true
     */
    @PatchMapping("/{id}/attendance-permission")
    public ResponseEntity<UserDto> updateAttendancePermission(
            @PathVariable Long id,
            @RequestParam Long adminId,
            @RequestParam boolean allowed) {
        UserDto updated = userService.updateAttendancePermissionByAdmin(id, allowed, adminId);
        return updated != null ? ResponseEntity.ok(updated) : ResponseEntity.badRequest().build();
    }

    /**
     * Admin xóa nhân viên (không xóa được ADMIN). DELETE /api/users/{id}?adminId=1
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @RequestParam Long adminId) {
        boolean deleted = userService.deleteByAdmin(id, adminId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }
}
