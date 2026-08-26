package com.smartcampus.repository;

import com.smartcampus.entity.Attendance;
import com.smartcampus.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByStudentId(Long studentId);
    List<Attendance> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
    Optional<Attendance> findByStudentIdAndSubjectIdAndDate(Long studentId, Long subjectId, LocalDate date);
    long countByStudentIdAndSubjectIdAndStatus(Long studentId, Long subjectId, AttendanceStatus status);
    long countByStudentIdAndSubjectId(Long studentId, Long subjectId);
}
