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
     * Kiểm tra nhắc báo cáo: nếu ngày hôm trước (theo giờ VN) user có công việc mà chưa báo cáo thì yêu cầu báo cáo bù.
     * Quy định: đến 24:00 chưa báo cáo thì coi là chưa hoàn thành; sáng hôm sau hệ thống nhắc báo cáo bù.
     */
    public ReportReminderDto getReportReminder(Long userId) {
        LocalDate today = LocalDate.now(VIETNAM);
        LocalDate yesterday = today.minusDays(1);

        List<Task> assigneeTasks = taskRepository.findByAssigneeIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getStatus() == TaskStatus.ACCEPTED || t.getStatus() == TaskStatus.NEW)
                .collect(Collectors.toList());

        // Có công việc hôm qua = có ít nhất một task tồn tại trong ngày hôm qua (kể cả đã hoàn thành trong ngày hôm qua).
        boolean hadWorkYesterday = assigneeTasks.stream().anyMatch(t -> {
            LocalDate taskCreated = t.getCreatedAt() != null ? t.getCreatedAt().atZone(VIETNAM).toLocalDate() : null;
            if (taskCreated == null || taskCreated.isAfter(yesterday)) return false;
            if (t.getCompletedAt() == null) return true;
            // Hoàn thành trong hoặc sau ngày hôm qua => vẫn coi là có việc hôm qua (phải báo cáo).
            return !t.getCompletedAt().toLocalDate().isBefore(yesterday);
        });

        if (!hadWorkYesterday) {
            return ReportReminderDto.builder()
                    .missingYesterday(false)
                    .yesterday(yesterday)
                    .message(null)
                    .build();
        }

        boolean reportedYesterday = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                .anyMatch(r -> r.getReportDate().equals(yesterday));

        if (reportedYesterday) {
            return ReportReminderDto.builder()
                    .missingYesterday(false)
                    .yesterday(yesterday)
                    .message(null)
                    .build();
        }

        return ReportReminderDto.builder()
                .missingYesterday(true)
                .yesterday(yesterday)
                .message("Bạn chưa báo cáo công việc ngày " + yesterday + ". Vui lòng báo cáo bù ngay.")
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

            List<Task> assigneeTasks = taskRepository.findByAssigneeIdOrderByDeadlineAsc(userId).stream()
                    .filter(t -> t.getStatus() == TaskStatus.ACCEPTED)
                    .collect(Collectors.toList());

            int requiredDays = 0;
            int missedDays = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                final LocalDate day = d;
                boolean hadBacklog = assigneeTasks.stream().anyMatch(t -> {
                    LocalDate taskCreated = t.getCreatedAt() != null ? t.getCreatedAt().atZone(VIETNAM).toLocalDate() : null;
                    if (taskCreated == null || taskCreated.isAfter(day)) return false;
                    if (t.getCompletedAt() == null) return true;
                    return !t.getCompletedAt().toLocalDate().isBefore(day);
                });
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
