package vn.phn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.phn.entity.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    Optional<AttendanceRecord> findByUserIdAndRecordDate(Long userId, LocalDate recordDate);

    List<AttendanceRecord> findByUserIdAndRecordDateBetweenOrderByRecordDateAsc(Long userId, LocalDate start, LocalDate end);

    List<AttendanceRecord> findByUserIdOrderByRecordDateDesc(Long userId, org.springframework.data.domain.Pageable pageable);

    List<AttendanceRecord> findByRecordDateBetweenOrderByUserIdAscRecordDateAsc(LocalDate start, LocalDate end);
}
