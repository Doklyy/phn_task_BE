package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.phn.dto.DailyReportDto;
import vn.phn.dto.ReportComplianceDto;
import vn.phn.dto.ReportReminderDto;
import vn.phn.dto.SubmitReportRequest;
import vn.phn.entity.DailyReport;
import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import vn.phn.entity.User;
import vn.phn.repository.DailyReportRepository;
import vn.phn.repository.TaskRepository;
import vn.phn.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository reportRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Gửi báo cáo kết quả ngày. Mặc định ngày giờ hoàn thành = thời điểm nhấn Gửi.
     */
    @Transactional
    public DailyReportDto submitReport(SubmitReportRequest req, Long userId) {
        LocalDateTime submittedAt = LocalDateTime.now();
        DailyReport report = DailyReport.builder()
                .userId(userId)
                .taskId(req.getTaskId())
                .reportDate(req.getReportDate())
                .result(req.getResult())
                .weight(req.getWeight())
                .submittedAt(submittedAt)
                .attachmentPath(req.getAttachmentPath())
                .build();
        report = reportRepository.save(report);
        return toDto(report);
    }

    public List<DailyReportDto> getReportsByUser(Long userId) {
        return reportRepository.findByUserIdOrderByReportDateDesc(userId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Lịch sử báo cáo theo nhiệm vụ (từng ngày) – dùng khi xem chi tiết nhiệm vụ.
     * Sắp xếp theo reportDate tăng dần (ngày 1, ngày 2, ngày 3...).
     */
    public List<DailyReportDto> getReportsByTaskId(Long taskId) {
        return reportRepository.findByTaskIdOrderByReportDateDesc(taskId).stream()
                .map(this::toDto)
                .sorted((a, b) -> a.getReportDate().compareTo(b.getReportDate()))
                .collect(Collectors.toList());
    }

    /** Múi giờ Việt Nam: dùng để xác định "hôm nay", "hôm qua" và 24:00 theo quy định. */
    private static final ZoneId VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    /**
     * Chỉ nhiệm vụ {@link TaskStatus#ACCEPTED} (đang thực hiện) mới bắt buộc báo cáo tiến độ theo ngày.
     * <ul>
     *   <li>{@link TaskStatus#PENDING_APPROVAL} — đã gửi hoàn thành, đợi duyệt: không bắt báo cáo tiến độ (TaskService.submitCompletion).</li>
     *   <li>{@link TaskStatus#COMPLETED} — đã duyệt xong: không bắt.</li>
     *   <li>{@link TaskStatus#PAUSED} — tạm dừng: không bắt.</li>
     *   <li>{@link TaskStatus#NEW} — chưa tiếp nhận: không bắt báo cáo tiến độ; sau khi tiếp nhận ({@code acceptedAt}) mới vào diện bắt buộc.</li>
     * </ul>
     * Khi sếp từ chối hoàn thành, task về lại {@link TaskStatus#ACCEPTED} (TaskService.rejectCompletion) với {@code acceptedAt} mới → bắt buộc báo cáo lại từ ngày trả về.
     */
    private static boolean requiresDailyProgressReport(Task t) {
        return t != null && t.getStatus() == TaskStatus.ACCEPTED;
    }

    /**
     * Ngày đầu tiên phải báo cáo tiến độ: ngày tiếp nhận ({@link Task#getAcceptedAt()}), hoặc dữ liệu cũ chưa có thì lấy ngày tạo.
     */
    private static LocalDate firstDailyReportObligationDay(Task t, ZoneId zone) {
        if (t == null) return null;
        if (t.getAcceptedAt() != null) {
            return t.getAcceptedAt().atZone(zone).toLocalDate();
        }
        if (t.getCreatedAt() != null) {
            return t.getCreatedAt().atZone(zone).toLocalDate();
        }
        return null;
    }

    /**
     * Vào ngày {@code day}, nhiệm vụ đã bắt đầu nghĩa vụ báo cáo tiến độ (đã tiếp nhận vào hoặc trước ngày đó).
     */
    private static boolean obligationAppliesOnOrBefore(Task t, LocalDate day, ZoneId zone) {
        LocalDate start = firstDailyReportObligationDay(t, zone);
        if (start == null) return false;
        return !start.isAfter(day);
    }

    /**
     * Kiểm tra nhắc báo cáo: nếu ngày hôm trước (theo giờ VN) user có công việc mà chưa báo cáo thì yêu cầu báo cáo bù.
     * Quy định: đến 24:00 chưa báo cáo thì coi là chưa hoàn thành; sáng hôm sau hệ thống nhắc báo cáo bù.
     */
    public ReportReminderDto getReportReminder(Long userId) {
        LocalDate today = LocalDate.now(VIETNAM);
        LocalDate yesterday = today.minusDays(1);

        List<Task> assigneeTasks = taskRepository.findByAssigneeIdAndDeletedAtIsNullOrderByDeadlineAsc(userId).stream()
                .filter(DailyReportService::requiresDailyProgressReport)
                .collect(Collectors.toList());

        // Có việc phải báo cáo hôm qua = ít nhất một task ACCEPTED đã tiếp nhận (acceptedAt) vào hoặc trước hôm qua.
        boolean hadWorkYesterday = assigneeTasks.stream()
                .anyMatch(t -> obligationAppliesOnOrBefore(t, yesterday, VIETNAM));

        if (!hadWorkYesterday) {
            return ReportReminderDto.builder()
                    .missingYesterday(false)
                    .yesterday(yesterday)
                    .message(null)
                    .missingTasks(null)
                    .build();
        }

        boolean reportedYesterday = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                .anyMatch(r -> r.getReportDate().equals(yesterday));

        if (reportedYesterday) {
            return ReportReminderDto.builder()
                    .missingYesterday(false)
                    .yesterday(yesterday)
                    .message(null)
                    .missingTasks(null)
                    .build();
        }

        // Danh sách nhiệm vụ (chỉ ACCEPTED) để hiển thị trên màn hình chặn
        List<ReportReminderDto.MissingTaskItem> missingTaskItems = assigneeTasks.stream()
                .filter(t -> obligationAppliesOnOrBefore(t, yesterday, VIETNAM))
                .map(t -> ReportReminderDto.MissingTaskItem.builder()
                        .taskId(t.getId())
                        .taskTitle(t.getTitle() != null ? t.getTitle() : "Nhiệm vụ #" + t.getId())
                        .build())
                .collect(Collectors.toList());

        return ReportReminderDto.builder()
                .missingYesterday(true)
                .yesterday(yesterday)
                .message("Bạn chưa báo cáo công việc ngày " + yesterday + ". Vui lòng báo cáo bù trước khi sử dụng hệ thống.")
                .missingTasks(missingTaskItems)
                .build();
    }

    /**
     * Lấy toàn bộ báo cáo (dùng cho admin theo dõi).
     */
    public List<DailyReportDto> getAllReports() {
        return reportRepository.findAll().stream()
                .sorted((a, b) -> b.getSubmittedAt().compareTo(a.getSubmittedAt()))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Tổng hợp theo tháng: Admin xem tất cả, Leader xem nhóm.
     * Quy tắc: mỗi ngày có công việc tồn (ACCEPTED) thì phải báo cáo trước 24h. Đủ thì +điểm, thiếu thì trừ điểm.
     */
    public List<ReportComplianceDto> getMonthlyCompliance(YearMonth month, Long currentUserId) {
        List<vn.phn.dto.UserDto> scopeUsers = userService.findUsersForCurrentUser(currentUserId);
        if (scopeUsers == null || scopeUsers.isEmpty()) return List.of();

        List<ReportComplianceDto> result = new ArrayList<>();
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        for (vn.phn.dto.UserDto u : scopeUsers) {
            Long userId = u.getId();
            if (userId == null) continue;

            Set<LocalDate> reportedDates = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                    .filter(r -> !r.getReportDate().isBefore(start) && !r.getReportDate().isAfter(end))
                    .map(DailyReport::getReportDate)
                    .collect(Collectors.toSet());

            List<Task> assigneeTasks = taskRepository.findByAssigneeIdAndDeletedAtIsNullOrderByDeadlineAsc(userId).stream()
                    .filter(t -> t.getStatus() == TaskStatus.ACCEPTED)
                    .collect(Collectors.toList());

            int requiredDays = 0;
            int missedDays = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                final LocalDate day = d;
                boolean hadBacklog = assigneeTasks.stream()
                        .anyMatch(t -> obligationAppliesOnOrBefore(t, day, VIETNAM));
                if (!hadBacklog) continue;
                requiredDays++;
                if (!reportedDates.contains(day)) missedDays++;
            }
            int reportedDays = reportedDates.size();
            int point = (reportedDays * 1) - (missedDays * 2);
            result.add(ReportComplianceDto.builder()
                    .userId(userId)
                    .userName(u.getName())
                    .team(u.getTeam())
                    .requiredDays(requiredDays)
                    .reportedDays(reportedDays)
                    .missedDays(missedDays)
                    .point(point)
                    .build());
        }
        return result;
    }

    private DailyReportDto toDto(DailyReport r) {
        if (r == null) return null;
        String taskTitle = taskRepository.findById(r.getTaskId()).map(Task::getTitle).orElse(null);
        String userName = userRepository.findById(r.getUserId()).map(User::getName).orElse(null);
        return DailyReportDto.builder()
                .id(r.getId())
                .userId(r.getUserId())
                .userName(userName)
                .taskId(r.getTaskId())
                .taskTitle(taskTitle)
                .reportDate(r.getReportDate())
                .result(r.getResult())
                .weight(r.getWeight())
                .submittedAt(r.getSubmittedAt())
                .attachmentPath(r.getAttachmentPath())
                .build();
    }
}
