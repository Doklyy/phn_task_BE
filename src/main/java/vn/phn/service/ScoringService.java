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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScoringService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final DailyReportRepository reportRepository;

    /**
     * Tính điểm chuyên cần và chất lượng (WQT) cho một user.
     * - Điểm chuyên cần: tỷ lệ số ngày báo cáo / số ngày làm việc (30 ngày gần nhất hoặc từ khi user tạo)
     * - Điểm chất lượng: WQT trung bình từ các task đã hoàn thành có quality
     * - Tổng điểm = (chuyên cần * 0.4) + (chất lượng * 0.6)
     */
    public ScoringDto calculateScore(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;

        LocalDate startDate = LocalDate.now().minusDays(30); // 30 ngày gần nhất
        LocalDate endDate = LocalDate.now();

        // Đếm số ngày đã báo cáo trong 30 ngày gần nhất
        long reportedDays = reportRepository.findByUserIdOrderByReportDateDesc(userId).stream()
                .filter(r -> !r.getReportDate().isBefore(startDate) && !r.getReportDate().isAfter(endDate))
                .map(r -> r.getReportDate())
                .distinct()
                .count();

        long workingDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        double attendanceScore = workingDays > 0 ? Math.min(1.0, (double) reportedDays / workingDays) : 0;

        // Tính điểm chất lượng WQT từ các task đã hoàn thành
        List<Task> completedTasks = taskRepository.findByAssigneeIdOrderByDeadlineAsc(userId).stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getQuality() != null && t.getQuality() > 0)
                .toList();

        double qualityScore = 0;
        if (!completedTasks.isEmpty()) {
            double totalWqt = completedTasks.stream()
                    .mapToDouble(t -> TaskService.calculateWqt(t.getWeight(), t.getQuality()))
                    .sum();
            qualityScore = totalWqt / completedTasks.size();
        }

        // Tổng điểm = chuyên cần 40% + chất lượng 60%
        double totalScore = (attendanceScore * 0.4) + (qualityScore * 0.6);

        return ScoringDto.builder()
                .userId(user.getId())
                .userName(user.getUsername())
                .name(user.getName())
                .attendanceScore(attendanceScore)
                .qualityScore(qualityScore)
                .totalScore(totalScore)
                .reportedDays((int) reportedDays)
                .completedTasks(completedTasks.size())
                .build();
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
