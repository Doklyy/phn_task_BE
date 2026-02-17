package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.ChangePasswordRequest;
import vn.phn.dto.LoginRequest;
import vn.phn.dto.LoginResponse;
import vn.phn.entity.User;
import vn.phn.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    /**
     * Xác thực đăng nhập theo username/password.
     * Hiện tại dùng so sánh plain text cho đơn giản (TODO: mã hóa bằng BCrypt nếu cần).
     */
    public LoginResponse authenticate(LoginRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .filter(user -> user.getPassword().equals(request.getPassword()))
                .map(this::toLoginResponse)
                .orElse(null);
    }

    /**
     * Lấy lại thông tin user theo userId (sau khi đăng nhập).
     */
    public LoginResponse getUserInfo(Long userId) {
        return userRepository.findById(userId)
                .map(this::toLoginResponse)
                .orElse(null);
    }

    /**
     * Nhân viên tự đổi mật khẩu: kiểm tra mật khẩu hiện tại đúng thì cập nhật sang mật khẩu mới.
     * Trả về true nếu đổi thành công, false nếu user không tồn tại hoặc mật khẩu hiện tại sai.
     */
    @Transactional
    public boolean changePassword(ChangePasswordRequest request) {
        User user = userRepository.findById(request.getUserId()).orElse(null);
        if (user == null) return false;
        if (!user.getPassword().equals(request.getCurrentPassword())) return false;
        user.setPassword(request.getNewPassword());
        userRepository.save(user);
        return true;
    }

    private LoginResponse toLoginResponse(User user) {
        boolean canManage = user.getRole() == vn.phn.entity.Role.ADMIN
                || Boolean.TRUE.equals(user.getCanManageAttendance());
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .team(user.getTeam())
                .token(String.valueOf(user.getId()))
                .canManageAttendance(canManage)
                .build();
    }
}
