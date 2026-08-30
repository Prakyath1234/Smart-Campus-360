package com.smartcampus.repository;

import com.smartcampus.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {
    Optional<Subject> findByCode(String code);
    boolean existsByCode(String code);
    List<Subject> findByDepartmentId(Long departmentId);
    List<Subject> findByFacultyId(Long facultyId);
}
