package com.smartcampus.repository;

import com.smartcampus.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByFacultyId(Long facultyId);
}
