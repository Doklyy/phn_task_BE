package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.CreateUserRequest;
import vn.phn.dto.SetPasswordRequest;
import vn.phn.dto.UserDto;
import vn.phn.entity.Role;
import vn.phn.entity.User;
import vn.phn.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Danh sách nhân sự theo quyền: ADMIN = tất cả; LEADER = cùng nhóm (team); STAFF = chỉ bản thân.
     */
    public List<UserDto> findUsersForCurrentUser(Long currentUserId) {
        User current = userRepository.findById(currentUserId).orElse(null);
        if (current == null) return List.of();
        if (current.getRole() == Role.ADMIN) return findAll();
        if (current.getRole() == Role.LEADER && current.getTeam() != null && !current.getTeam().isBlank()) {
            return userRepository.findByTeam(current.getTeam()).stream().map(this::toDto).collect(Collectors.toList());
        }
        if (current.getRole() == Role.STAFF) return List.of(toDto(current));
        return List.of(toDto(current));
    }

    public List<UserDto> findByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toDto).collect(Collectors.toList());
    }

    public UserDto findById(Long id) {
        return userRepository.findById(id).map(this::toDto).orElse(null);
    }

    public User getEntity(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Role getRole(Long userId) {
        return userRepository.findById(userId).map(User::getRole).orElse(null);
    }

    /**
     * Tạo user mới, chỉ cho phép nếu creator là ADMIN.
     * Trả về null nếu không có quyền hoặc dữ liệu không hợp lệ (vd: username trùng).
     */
    @Transactional
    public UserDto createUser(CreateUserRequest req, Long creatorId) {
        User creator = userRepository.findById(creatorId).orElse(null);
        if (creator == null || creator.getRole() != Role.ADMIN) {
            // Không có quyền
            return null;
        }

        if (userRepository.existsByUsername(req.getUsername())) {
            // Username đã tồn tại
            return null;
        }

        User user = User.builder()
                .username(req.getUsername())
                .password(req.getPassword()) // TODO: mã hóa password (BCrypt) nếu cần
                .name(req.getName())
                .role(req.getRole())
                .build();
        user = userRepository.save(user);
        return toDto(user);
    }

    /**
     * Admin đặt lại mật khẩu cho user (theo id). Chỉ ADMIN mới có quyền.
     */
    @Transactional
    public boolean setPasswordByAdmin(Long targetUserId, SetPasswordRequest req, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return false;
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) return false;
        target.setPassword(req.getNewPassword());
        userRepository.save(target);
        return true;
    }

    /**
     * Admin cập nhật role cho user (STAFF/LEADER/ADMIN).
     */
    @Transactional
    public UserDto updateRoleByAdmin(Long targetUserId, Role newRole, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return null;
        }
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) {
            return null;
        }
        target.setRole(newRole);
        target = userRepository.save(target);
        return toDto(target);
    }

    public UserDto toDto(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .role(u.getRole())
                .team(u.getTeam())
                .build();
    }
}
