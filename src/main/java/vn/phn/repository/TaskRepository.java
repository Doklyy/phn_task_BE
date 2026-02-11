package vn.phn.repository;

import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssigneeIdOrderByDeadlineAsc(Long assigneeId);

    List<Task> findByLeaderIdOrAssigneeIdOrderByDeadlineAsc(Long leaderId, Long assigneeId);

    List<Task> findByStatus(TaskStatus status);

    /** Lọc theo assignee và khoảng thời gian deadline */
    List<Task> findByAssigneeIdAndDeadlineBetweenOrderByDeadlineAsc(
            Long assigneeId, LocalDateTime start, LocalDateTime end);

    /** Lọc tất cả task theo khoảng thời gian (Admin) */
    List<Task> findByDeadlineBetweenOrderByDeadlineAsc(LocalDateTime start, LocalDateTime end);
}
