package vn.phn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.phn.entity.LeaveRequest;
import vn.phn.entity.LeaveRequestStatus;

import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUserIdOrderByCreatedAtDesc(Long userId, org.springframework.data.domain.Pageable pageable);

    List<LeaveRequest> findByStatusOrderByCreatedAtAsc(LeaveRequestStatus status);

    List<LeaveRequest> findByUserIdAndStatus(Long userId, LeaveRequestStatus status);
}
