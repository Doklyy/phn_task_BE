package vn.phn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.phn.entity.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByUserIdAndRecordDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
}
