package vn.phn.repository;

import vn.phn.entity.Role;
import vn.phn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByRole(Role role);

    List<User> findByTeam(String team);

    boolean existsByUsername(String username);
}
