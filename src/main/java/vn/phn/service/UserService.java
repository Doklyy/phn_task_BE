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
     * Danh sách nhân sự theo quyền (không loại admin). Gọi khi không dùng personnelOnly.
     */
    public List<UserDto> findUsersForCurrentUser(Long currentUserId) {
        return findUsersForCurrentUser(currentUserId, false);
    }

    /** Danh sách tất cả user trừ ADMIN (dùng cho màn Nhân sự). */
    public List<UserDto> findAllExceptAdmin() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() != Role.ADMIN)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Danh sách nhân sự theo quyền: ADMIN = tất cả; LEADER = cùng nhóm (team); STAFF = chỉ bản thân.
     * @param excludeAdmin nếu true thì loại ADMIN khỏi danh sách (dùng cho màn Nhân sự).
     */
    public List<UserDto> findUsersForCurrentUser(Long currentUserId, boolean excludeAdmin) {
        User current = userRepository.findById(currentUserId).orElse(null);
        if (current == null) return List.of();
        List<UserDto> list;
        if (current.getRole() == Role.ADMIN) {
            list = excludeAdmin ? findAllExceptAdmin() : findAll();
        } else if (current.getRole() == Role.LEADER && current.getTeam() != null && !current.getTeam().isBlank()) {
            list = userRepository.findByTeam(current.getTeam()).stream().map(this::toDto).collect(Collectors.toList());
        } else {
            list = List.of(toDto(current));
        }
        if (excludeAdmin) {
            list = list.stream().filter(dto -> dto.getRole() != Role.ADMIN).collect(Collectors.toList());
        }
        return list;
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
                .team(req.getTeam() != null && !req.getTeam().isBlank() ? req.getTeam().trim() : null)
                .canManageAttendance(Boolean.TRUE.equals(req.getCanManageAttendance()))
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

    /**
     * Admin cập nhật nhóm (team) cho user.
     */
    @Transactional
    public UserDto updateTeamByAdmin(Long targetUserId, String team, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return null;
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) return null;
        target.setTeam(team != null && !team.isBlank() ? team.trim() : null);
        target = userRepository.save(target);
        return toDto(target);
    }

    /**
     * Admin cập nhật quyền chấm công cho user.
     */
    @Transactional
    public UserDto updateAttendancePermissionByAdmin(Long targetUserId, boolean allowed, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return null;
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) return null;
        target.setCanManageAttendance(allowed);
        target = userRepository.save(target);
        return toDto(target);
    }

    /**
     * Admin xóa nhân viên (không xóa được ADMIN). Trả về true nếu xóa thành công.
     * Có thể thất bại do ràng buộc khóa ngoại (user có tasks, reports, ...).
     */
    @Transactional
    public boolean deleteByAdmin(Long targetUserId, Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() != Role.ADMIN) return false;
        User target = userRepository.findById(targetUserId).orElse(null);
        if (target == null) return false;
        if (target.getRole() == Role.ADMIN) return false; // Không cho xóa admin
        try {
            userRepository.delete(target);
            return true;
        } catch (Exception e) {
            return false; // Ràng buộc FK hoặc lỗi khác
        }
    }

    /** Admin có quyền chấm công không (luôn true). User khác theo trường canManageAttendance. */
    public boolean canManageAttendance(Long userId) {
        return userRepository.findById(userId)
                .map(u -> u.getRole() == Role.ADMIN || Boolean.TRUE.equals(u.getCanManageAttendance()))
                .orElse(false);
    }

    public UserDto toDto(User u) {
        if (u == null) return null;
        return UserDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .name(u.getName())
                .role(u.getRole())
                .team(u.getTeam())
                .canManageAttendance(u.getRole() == Role.ADMIN || Boolean.TRUE.equals(u.getCanManageAttendance()))
                .build();
    }
}
