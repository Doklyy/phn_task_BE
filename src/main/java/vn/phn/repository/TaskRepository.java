package vn.phn.repository;

import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByDeletedAtIsNull();

    List<Task> findByAssigneeIdAndDeletedAtIsNullOrderByDeadlineAsc(Long assigneeId);

    List<Task> findByLeaderIdOrAssigneeIdAndDeletedAtIsNullOrderByDeadlineAsc(Long leaderId, Long assigneeId);

    List<Task> findByStatus(TaskStatus status);

    /** Lọc theo assignee và khoảng thời gian deadline */
    List<Task> findByAssigneeIdAndDeadlineBetweenAndDeletedAtIsNullOrderByDeadlineAsc(
            Long assigneeId, LocalDateTime start, LocalDateTime end);

    /** Lọc tất cả task theo khoảng thời gian (Admin), chưa xóa mềm */
    List<Task> findByDeadlineBetweenAndDeletedAtIsNullOrderByDeadlineAsc(LocalDateTime start, LocalDateTime end);

    /** Thùng rác admin: đã xóa mềm, mới xóa trước */
    List<Task> findAllByDeletedAtIsNotNullOrderByDeletedAtDesc();
}
