package vn.phn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /** Nhóm: old_product (Trang, Đoàn, Dương, An) | new_product (Nam, Nhất, Khải, Ly). Admin = null. */
    @Column(name = "team", length = 50)
    private String team;

    /** Quyền chấm công: được xem bảng chấm công và chấm công cho người khác. Admin luôn có quyền. */
    @Column(name = "can_manage_attendance")
    private Boolean canManageAttendance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Getter/setter tường minh để build Docker (Lombok có thể không chạy đủ trong môi trường build). */
    public Boolean getCanManageAttendance() {
        return canManageAttendance;
    }

    public void setCanManageAttendance(Boolean canManageAttendance) {
        this.canManageAttendance = canManageAttendance;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
