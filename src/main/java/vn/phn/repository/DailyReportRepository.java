package vn.phn.repository;

import vn.phn.entity.DailyReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyReportRepository extends JpaRepository<DailyReport, Long> {

    List<DailyReport> findByUserIdOrderByReportDateDesc(Long userId);

    List<DailyReport> findByTaskIdOrderByReportDateDesc(Long taskId);

    Optional<DailyReport> findByUserIdAndTaskIdAndReportDate(Long userId, Long taskId, LocalDate reportDate);
}
