package com.smartcampus.repository;

import com.smartcampus.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByUserId(Long userId);
    List<Student> findByDepartmentId(Long departmentId);
}
