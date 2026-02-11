package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.TaskDto;
import vn.phn.dto.CreateTaskRequest;
import vn.phn.entity.Role;
import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import vn.phn.entity.User;
import vn.phn.repository.TaskRepository;
import vn.phn.repository.UserRepository;

import java.util.Objects;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    /**
     * Công thức WQT: WQT = weight * quality (chất lượng 0..1 do Leader đánh giá).
     */
    public static double calculateWqt(Double weight, Double quality) {
        if (weight == null || quality == null) return 0;
        return Math.min(1, weight * quality);
    }

    /**
     * Lấy danh sách task theo quyền:
     * - Admin: tất cả
     * - Leader: task mà mình là leader hoặc assignee
     * - Staff: task mà mình là assignee
     * Logic lọc: Tự động sắp xếp - công việc chưa hoàn thành (NEW, ACCEPTED) lên trên cùng, COMPLETED xuống dưới.
     */
    public List<TaskDto> getTasksForUser(Long currentUserId, Role role) {
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) return List.of();

        List<Task> tasks;
        if (role == Role.ADMIN) {
            tasks = taskRepository.findAll();
        } else if (role == Role.LEADER) {
            tasks = taskRepository.findByLeaderIdOrAssigneeIdOrderByDeadlineAsc(currentUserId, currentUserId);
        } else {
            tasks = taskRepository.findByAssigneeIdOrderByDeadlineAsc(currentUserId);
        }

        // Sắp xếp: NEW/ACCEPTED lên trên, COMPLETED xuống dưới, sau đó theo deadline
        tasks.sort((t1, t2) -> {
            boolean t1Incomplete = t1.getStatus() != TaskStatus.COMPLETED;
            boolean t2Incomplete = t2.getStatus() != TaskStatus.COMPLETED;
            if (t1Incomplete != t2Incomplete) {
                return t1Incomplete ? -1 : 1; // Incomplete lên trên
            }
            return t1.getDeadline().compareTo(t2.getDeadline());
        });

        return tasks.stream().map(t -> toDto(t, currentUser)).collect(Collectors.toList());
    }

    /**
     * Lọc theo tháng (year, month). Trả về task có deadline trong tháng đó.
     */
    public List<TaskDto> getTasksFilteredByMonth(Long currentUserId, Role role, int year, int month) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);

        List<Task> tasks;
        if (role == Role.ADMIN) {
            tasks = taskRepository.findByDeadlineBetweenOrderByDeadlineAsc(start, end);
        } else if (role == Role.LEADER) {
            List<Task> all = taskRepository.findByLeaderIdOrAssigneeIdOrderByDeadlineAsc(currentUserId, currentUserId);
            tasks = all.stream().filter(t -> !t.getDeadline().isBefore(start) && !t.getDeadline().isAfter(end)).collect(Collectors.toList());
        } else {
            tasks = taskRepository.findByAssigneeIdAndDeadlineBetweenOrderByDeadlineAsc(currentUserId, start, end);
        }
        // Sắp xếp: NEW/ACCEPTED lên trên, COMPLETED xuống dưới
        tasks.sort((t1, t2) -> {
            boolean t1Incomplete = t1.getStatus() != TaskStatus.COMPLETED;
            boolean t2Incomplete = t2.getStatus() != TaskStatus.COMPLETED;
            if (t1Incomplete != t2Incomplete) {
                return t1Incomplete ? -1 : 1;
            }
            return t1.getDeadline().compareTo(t2.getDeadline());
        });

        User currentUser = userRepository.findById(currentUserId).orElse(null);
        return tasks.stream().map(t -> toDto(t, currentUser)).collect(Collectors.toList());
    }

    /**
     * Lọc theo quý (1-4).
     */
    public List<TaskDto> getTasksFilteredByQuarter(Long currentUserId, Role role, int year, int quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        LocalDateTime start = LocalDateTime.of(year, startMonth, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, startMonth + 2, YearMonth.of(year, startMonth + 2).lengthOfMonth(), 23, 59, 59);
        return filterByDateRange(currentUserId, role, start, end);
    }

    /**
     * Lọc theo năm.
     */
    public List<TaskDto> getTasksFilteredByYear(Long currentUserId, Role role, int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return filterByDateRange(currentUserId, role, start, end);
    }

    private List<TaskDto> filterByDateRange(Long currentUserId, Role role, LocalDateTime start, LocalDateTime end) {
        List<Task> tasks;
        if (role == Role.ADMIN) {
            tasks = taskRepository.findByDeadlineBetweenOrderByDeadlineAsc(start, end);
        } else if (role == Role.LEADER) {
            List<Task> all = taskRepository.findByLeaderIdOrAssigneeIdOrderByDeadlineAsc(currentUserId, currentUserId);
            tasks = all.stream().filter(t -> !t.getDeadline().isBefore(start) && !t.getDeadline().isAfter(end)).collect(Collectors.toList());
        } else {
            tasks = taskRepository.findByAssigneeIdAndDeadlineBetweenOrderByDeadlineAsc(currentUserId, start, end);
        }

        // Sắp xếp: NEW/ACCEPTED lên trên, COMPLETED xuống dưới
        tasks.sort((t1, t2) -> {
            boolean t1Incomplete = t1.getStatus() != TaskStatus.COMPLETED;
            boolean t2Incomplete = t2.getStatus() != TaskStatus.COMPLETED;
            if (t1Incomplete != t2Incomplete) {
                return t1Incomplete ? -1 : 1;
            }
            return t1.getDeadline().compareTo(t2.getDeadline());
        });

        User currentUser = userRepository.findById(currentUserId).orElse(null);
        return tasks.stream().map(t -> toDto(t, currentUser)).collect(Collectors.toList());
    }

    @Transactional
    public TaskDto createTask(CreateTaskRequest req, Long assignerId) {
        Task task = Task.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .objective(req.getObjective())
                .deadline(req.getDeadline())
                .weight(req.getWeight())
                .status(TaskStatus.NEW)
                .assignerId(assignerId)
                .leaderId(req.getLeaderId())
                .assigneeId(req.getAssigneeId())
                .build();
        task = taskRepository.save(task);
        return toDto(task, null);
    }

    /** Tiếp nhận task: chuyển NEW -> ACCEPTED */
    @Transactional
    public TaskDto acceptTask(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null || !task.getAssigneeId().equals(userId) || task.getStatus() != TaskStatus.NEW)
            return null;
        task.setStatus(TaskStatus.ACCEPTED);
        task = taskRepository.save(task);
        return toDto(task, userRepository.findById(userId).orElse(null));
    }

    /** Cập nhật trạng thái (Leader/Admin có thể đánh giá quality, đánh dấu hoàn thành) */
    @Transactional
    public TaskDto updateTask(Long taskId, TaskStatus status, Double quality, Long currentUserId, Role role) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return null;
        if (role != Role.ADMIN && role != Role.LEADER) return null;
        if (quality != null) task.setQuality(quality);
        if (status != null) {
            task.setStatus(status);
            if (status == TaskStatus.COMPLETED)
                task.setCompletedAt(LocalDateTime.now());
        }
        task = taskRepository.save(task);
        return toDto(task, userRepository.findById(currentUserId).orElse(null));
    }

    public TaskDto toDto(Task t, User currentUser) {
        if (t == null) return null;
        double wqt = calculateWqt(t.getWeight(), t.getQuality());

        String assigneeName = null;
        if (t.getAssigneeId() != null) {
            User assignee = userRepository.findById(t.getAssigneeId()).orElse(null);
            if (assignee != null) {
                assigneeName = assignee.getName();
            }
        }

        String leaderName = null;
        if (t.getLeaderId() != null) {
            User leader = userRepository.findById(t.getLeaderId()).orElse(null);
            if (leader != null) {
                leaderName = leader.getName();
            }
        }

        return TaskDto.builder()
                .id(t.getId())
                .title(t.getTitle())
                .content(t.getContent())
                .objective(t.getObjective())
                .deadline(t.getDeadline())
                .weight(t.getWeight())
                .status(t.getStatus())
                .quality(t.getQuality())
                .wqt(wqt)
                .assignerId(t.getAssignerId())
                .leaderId(t.getLeaderId())
                .assigneeId(t.getAssigneeId())
                .leaderName(leaderName)
                .assigneeName(assigneeName)
                .attachmentPath(t.getAttachmentPath())
                .createdAt(t.getCreatedAt() != null ? t.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .completedAt(t.getCompletedAt())
                .completionNote(t.getCompletionNote())
                .completionLink(t.getCompletionLink())
                .completionFilePath(t.getCompletionFilePath())
                .build();
    }

    /**
     * Assignee gửi báo cáo hoàn thành → chuyển sang Đợi duyệt (PENDING_APPROVAL).
     */
    @Transactional
    public TaskDto submitCompletion(Long taskId, Long userId, String note, String link, String filePath) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return null;
        if (!Objects.equals(task.getAssigneeId(), userId)) return null;
        if (task.getStatus() != TaskStatus.ACCEPTED) return null;
        task.setCompletionNote(note != null && !note.isBlank() ? note.trim() : null);
        task.setCompletionLink(link != null && !link.isBlank() ? link.trim() : null);
        task.setCompletionFilePath(filePath != null && !filePath.isBlank() ? filePath.trim() : null);
        task.setStatus(TaskStatus.PENDING_APPROVAL);
        task = taskRepository.save(task);
        return toDto(task, userRepository.findById(userId).orElse(null));
    }

    /**
     * Leader (người phân công) duyệt báo cáo hoàn thành → COMPLETED, đánh giá chất lượng (quality 0..1).
     * Leader hoặc Admin được duyệt.
     * - Admin: được duyệt / từ chối tất cả task.
     * - Leader: chỉ được duyệt / từ chối các task của nhân viên trong **team** của mình.
     */
    @Transactional
    public TaskDto approveCompletion(Long taskId, Long approverId, Double quality) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return null;
        User approver = userRepository.findById(approverId).orElse(null);
        if (approver == null || (approver.getRole() != Role.LEADER && approver.getRole() != Role.ADMIN)) {
            return null;
        }

        // Nếu là Leader: chỉ review task của nhân viên trong cùng team
        if (approver.getRole() == Role.LEADER) {
            User assignee = userRepository.findById(task.getAssigneeId()).orElse(null);
            if (assignee == null) return null;
            String leaderTeam = approver.getTeam();
            String assigneeTeam = assignee.getTeam();
            if (leaderTeam == null || assigneeTeam == null || !leaderTeam.equals(assigneeTeam)) {
                return null;
            }
        }

        // Admin được duyệt tất cả task
        if (task.getStatus() != TaskStatus.PENDING_APPROVAL) return null;
        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());
        if (quality != null) task.setQuality(Math.max(0, Math.min(1, quality)));
        task = taskRepository.save(task);
        return toDto(task, approver);
    }

    /**
     * Leader (người phân công) hoặc Admin từ chối → trả về công việc còn tồn đọng (ACCEPTED).
     */
    @Transactional
    public TaskDto rejectCompletion(Long taskId, Long approverId) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) return null;
        User approver = userRepository.findById(approverId).orElse(null);
        if (approver == null || (approver.getRole() != Role.LEADER && approver.getRole() != Role.ADMIN)) {
            return null;
        }

        // Nếu là Leader: chỉ được từ chối task của nhân viên trong cùng team
        if (approver.getRole() == Role.LEADER) {
            User assignee = userRepository.findById(task.getAssigneeId()).orElse(null);
            if (assignee == null) return null;
            String leaderTeam = approver.getTeam();
            String assigneeTeam = assignee.getTeam();
            if (leaderTeam == null || assigneeTeam == null || !leaderTeam.equals(assigneeTeam)) {
                return null;
            }
        }

        // Admin được từ chối tất cả task
        if (task.getStatus() != TaskStatus.PENDING_APPROVAL) return null;
        task.setStatus(TaskStatus.ACCEPTED);
        task.setCompletionNote(null);
        task.setCompletionLink(null);
        task.setCompletionFilePath(null);
        task = taskRepository.save(task);
        return toDto(task, approver);
    }
}
