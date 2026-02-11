package vn.phn.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.phn.entity.Role;
import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import vn.phn.entity.User;
import vn.phn.repository.TaskRepository;
import vn.phn.repository.UserRepository;

import java.time.LocalDateTime;

/**
 * Khởi tạo dữ liệu mẫu: nhân viên (admin, leader, staff) và vài task (chạy lần đầu).
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) return;

        User admin = User.builder()
                .username("admin")
                .password("admin123")
                .name("Nguyễn Đình Dũng")
                .role(Role.ADMIN)
                .build();
        userRepository.save(admin);
    }
}
