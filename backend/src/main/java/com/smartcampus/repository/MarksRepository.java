package com.smartcampus.repository;

import com.smartcampus.entity.Marks;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<Marks, Long> {
    List<Marks> findByStudentId(Long studentId);
    Optional<Marks> findByStudentIdAndSubjectId(Long studentId, Long subjectId);
}
