package vn.phn.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.phn.dto.ScoringDto;
import vn.phn.entity.Task;
import vn.phn.entity.TaskStatus;
import vn.phn.entity.User;
import vn.phn.repository.DailyReportRepository;
import vn.phn.repository.TaskRepository;
import vn.phn.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DailyReportRepository reportRepository;

    /**
     * Tính điểm chuyên cần và chất lượng (W_Q_T) cho một user.
     * Kỳ tính: từ mùng 1 tháng hiện tại đến hôm nay — mỗi đầu tháng (mùng 1) điểm reset.
     * - Điểm chuyên cần: tỷ lệ số ngày báo cáo / số ngày làm việc (T2–T6) trong tháng hiện tại.
     * - Điểm chất lượng W_Q_T (0..1): W×Q×T / tổng W; Q = chất lượng (hoặc 1 nếu hoàn thành chưa chấm), T = 1 đúng hạn / 0.5 trễ hoặc chưa đến hạn / 0 quá hạn.
     * - Tổng điểm = (chuyên cần * 0.4) + (chất lượng * 0.6).
     */
    public ScoringDto calculateScore(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1); // Mùng 1 đầu tháng → reset điểm
        LocalDate endDate = now;

        // Đếm số ngày đã báo cáo trong tháng hiện tại (từ mùng 1 đến hôm nay)
        long reportedDays = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                .filter(r -> !r.getReportDate().isBefore(startDate) && !r.getReportDate().isAfter(endDate))
                .map(r -> r.getReportDate())
                .distinct()
                .count();

        // Chuyên cần = số ngày báo cáo / số ngày làm việc (T2–T6), không tính T7–CN
        long workingDays = countWorkingDays(startDate, endDate);
        double attendanceScore = workingDays > 0 ? Math.min(1.0, (double) reportedDays / workingDays) : 0;

        // Tính điểm chất lượng W_Q_T từ các task trong kỳ
        List<Task> tasksInPeriod = taskRepository.findByAssigneeIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getDeadline() != null)
                .filter(t -> {
                    LocalDate d = t.getDeadline().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .toList();

        double totalAssignedW = tasksInPeriod.stream()
                .mapToDouble(t -> t.getWeight() != null ? t.getWeight() : 0d)
                .sum();

        double totalAchieved = tasksInPeriod.stream()
                .mapToDouble(t -> {
                    Double w = t.getWeight();
                    if (w == null || w <= 0) return 0d;

                    // Q: nếu có quality thì dùng, nếu task đã hoàn thành mà chưa có quality thì mặc định 1, ngược lại 0.
                    double q;
                    if (t.getQuality() != null) {
                        q = t.getQuality();
                    } else if (t.getStatus() == TaskStatus.COMPLETED) {
                        q = 1.0;
                    } else {
                        q = 0.0;
                    }

                    // T: tiến độ theo deadline & completedAt
                    double T;
                    LocalDateTime deadlineDateTime = t.getDeadline();
                    LocalDateTime completedAt = t.getCompletedAt();
                    LocalDateTime nowDateTime = LocalDateTime.now();

                    if (completedAt != null) {
                        // Hoàn thành đúng hạn: 1, muộn: 0.5
                        T = completedAt.isAfter(deadlineDateTime) ? 0.5 : 1.0;
                    } else {
                        // Chưa hoàn thành: nếu chưa tới hạn thì 0.5, quá hạn thì 0
                        T = nowDateTime.isAfter(deadlineDateTime) ? 0.0 : 0.5;
                    }

                    return w * q * T;
                })
                .sum();

        double qualityScore = 0;
        if (totalAssignedW > 0) {
            qualityScore = Math.min(1.0, totalAchieved / totalAssignedW);
        }

        // Tổng điểm = chuyên cần 40% + chất lượng 60%
        double qualityScoreMax = qualityScore * 15.0; // 15 là điểm chuẩn tối đa cho phần Nhiệm vụ (W_Q_T)
        double totalScore = (attendanceScore * 0.4) + (qualityScore * 0.6);

        return ScoringDto.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .attendanceScore(attendanceScore)
                .qualityScore(qualityScore)
                .qualityScoreMax(qualityScoreMax)
                .totalScore(totalScore)
                .reportedDays((int) reportedDays)
                .completedTasks((int) tasksInPeriod.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count())
                .build();
    }

    /** Đếm số ngày làm việc (T2–T6) trong khoảng [start, end] (bao gồm cả hai đầu). */
    private static long countWorkingDays(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) return 0;
        long count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) count++;
        }
        return count;
    }

    /**
     * Lấy bảng xếp hạng điểm cho tất cả user (hoặc theo role).
     */
    public List<ScoringDto> getRanking() {
        return userRepository.findAll().stream()
                .map(u -> calculateScore(u.getId()))
                .filter(s -> s != null)
                .sorted((s1, s2) -> Double.compare(s2.getTotalScore(), s1.getTotalScore())) // Giảm dần
                .toList();
    }
}
