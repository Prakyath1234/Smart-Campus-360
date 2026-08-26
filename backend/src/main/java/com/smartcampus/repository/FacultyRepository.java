package com.smartcampus.repository;

import com.smartcampus.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    Optional<Faculty> findByEmployeeId(String employeeId);
    Optional<Faculty> findByUserId(Long userId);
    List<Faculty> findByDepartmentId(Long departmentId);
}
