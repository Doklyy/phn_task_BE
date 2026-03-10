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
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DailyReportRepository reportRepository;

    /**
     * Tính điểm chuyên cần và chất lượng (W_Q_T) cho một user theo từng tháng.
     * - Nếu month == null → dùng tháng hiện tại, kỳ tính = từ mùng 1 tới hôm nay.
     * - Nếu month != null (YearMonth cụ thể, ví dụ 2026-02) → kỳ tính = full tháng đó (từ ngày 1 đến ngày cuối tháng).
     * - Điểm chuyên cần: tỷ lệ thời gian làm việc/báo cáo trong kỳ (0..1). Với các tháng bình thường lấy theo báo cáo ngày; riêng tháng 2026-02 dùng bảng “Thưởng thời gian làm việc”.
     * - Điểm chất lượng W_Q_T (0..1): W×Q×T / tổng W; Q = chất lượng, T = 1 đúng hạn / 0.7 trễ / 0 không hoàn thành.
     * - Tổng điểm = (chuyên cần * 0.4) + (chất lượng * 0.6).
     */
    public ScoringDto calculateScore(Long userId, YearMonth month) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDate today = LocalDate.now();
        YearMonth currentMonth = YearMonth.from(today);
        YearMonth targetMonth = (month != null) ? month : currentMonth;

        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.equals(currentMonth) ? today : targetMonth.atEndOfMonth();

        // Điểm CHUYÊN CẦN
        long reportedDays;
        long workingDays;
        double attendanceScore;
        // Tách riêng: thưởng thời gian làm việc & báo cáo cuối ngày (thang 5đ)
        double timeWorkScore5 = 0d;
        double dailyReportScore5 = 0d;

        // Tháng 2/2026: dùng bảng “Thưởng thời gian làm việc” (01/02–18/02) thay vì đọc từ daily_reports.
        if (targetMonth.equals(YearMonth.of(2026, 2))) {
            workingDays = 20; // số ngày làm việc chuẩn trong tháng 2/2026 (T2–T6)
            attendanceScore = getManualTimeWorkScoreForFeb2026(user);
            reportedDays = (long) Math.round(attendanceScore * workingDays);
            // Thưởng thời gian làm việc: scale về thang 5
            timeWorkScore5 = attendanceScore * 5.0;
            // Báo cáo hàng ngày: mỗi ngày 1 điểm, max 5 điểm
            dailyReportScore5 = Math.min(5.0, (double) reportedDays);
        } else {
            // Đếm số ngày đã báo cáo trong kỳ (tháng được chọn)
            reportedDays = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                    .filter(r -> !r.getReportDate().isBefore(startDate) && !r.getReportDate().isAfter(endDate))
                    .map(r -> r.getReportDate())
                    .distinct()
                    .count();

            // Chuyên cần = số ngày báo cáo / số ngày làm việc (T2–T6), không tính T7–CN
            workingDays = countWorkingDays(startDate, endDate);
            attendanceScore = workingDays > 0 ? Math.min(1.0, (double) reportedDays / workingDays) : 0;
            // Thưởng thời gian làm việc (5đ) = tỷ lệ chuyên cần * 5
            timeWorkScore5 = attendanceScore * 5.0;
            // Báo cáo hàng ngày: mỗi ngày 1 điểm, max 5 điểm
            dailyReportScore5 = Math.min(5.0, (double) reportedDays);
        }

        // Tính điểm chất lượng W_Q_T từ các task trong kỳ
        List<Task> tasksInPeriod = taskRepository.findByAssigneeIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getDeadline() != null)
                .filter(t -> {
                    LocalDate d = t.getDeadline().toLocalDate();
                    return !d.isBefore(startDate) && !d.isAfter(endDate);
                })
                .toList();

        double totalAssignedW = 0d;
        double totalAchieved = 0d;
        for (Task t : tasksInPeriod) {
            Double rawWeight = t.getWeight();
            if (rawWeight == null || rawWeight <= 0) continue;
            int w = weightToW(rawWeight);
            double q = t.getQuality() != null ? t.getQuality() : 0.0;
            double T = taskTFactor(t);
            totalAssignedW += w;
            totalAchieved += (w * q * T);
        }

        double qualityScore = 0d;
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
                .timeWorkScore5(timeWorkScore5)
                .dailyReportScore5(dailyReportScore5)
                .totalScore(totalScore)
                .reportedDays((int) reportedDays)
                .completedTasks((int) tasksInPeriod.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count())
                .build();
    }

    /**
     * Tiện ích: tính điểm cho tháng hiện tại (giữ API cũ cho các chỗ khác đang gọi).
     */
    public ScoringDto calculateScore(Long userId) {
        return calculateScore(userId, null);
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

    /** Mapping trọng số (0–1) → W (1, 2, 3, 5, 8) giống file Excel. */
    private static int weightToW(double weight) {
        if (weight <= 0.25) return 1;
        if (weight <= 0.50) return 2;
        if (weight <= 0.70) return 3;
        if (weight <= 0.90) return 5;
        return 8;
    }

    /** Hệ số T: 1 = hoàn thành đúng hạn, 0.7 = hoàn thành sau hạn, 0 = còn lại. */
    private static double taskTFactor(Task task) {
        if (task.getStatus() != TaskStatus.COMPLETED) return 0.0;
        LocalDateTime deadline = task.getDeadline();
        LocalDateTime completedAt = task.getCompletedAt();
        if (deadline == null || completedAt == null) return 0.0;
        return completedAt.isAfter(deadline) ? 0.7 : 1.0;
    }

    /**
     * Điểm chuyên cần đặc biệt cho tháng 02/2026 dựa trên bảng \"Thưởng thời gian làm việc\" (1/2–18/2).
     * Trả về giá trị 0..1 (điểm 10 chia cho 10). Các user khác ngoài bảng dùng 0.
     */
    private static double getManualTimeWorkScoreForFeb2026(User user) {
        if (user == null || user.getUsername() == null) return 0.0;
        String username = user.getUsername().toLowerCase();
        return switch (username) {
            case "an" -> 9.23 / 10.0;
            case "doan" -> 9.23 / 10.0;
            case "duong" -> 8.94 / 10.0;
            case "khai" -> 8.46 / 10.0;
            case "ly" -> 8.08 / 10.0;
            case "nam" -> 8.65 / 10.0;
            case "nhat" -> 9.23 / 10.0;
            case "trang" -> 7.69 / 10.0;
            default -> 0.0;
        };
    }

    /**
     * Lấy bảng xếp hạng điểm cho tất cả user theo tháng.
     * - Nếu month == null → dùng tháng hiện tại.
     */
    public List<ScoringDto> getRanking(YearMonth month) {
        return userRepository.findAll().stream()
                .map(u -> calculateScore(u.getId(), month))
                .filter(s -> s != null)
                .sorted((s1, s2) -> Double.compare(s2.getTotalScore(), s1.getTotalScore())) // Giảm dần
                .toList();
    }

    public List<ScoringDto> getRanking() {
        return getRanking(null);
    }
}
