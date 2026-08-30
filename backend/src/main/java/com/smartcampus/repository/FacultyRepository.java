package com.smartcampus.repository;

import com.smartcampus.entity.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.List;

public interface FacultyRepository extends JpaRepository<Faculty, Long>, JpaSpecificationExecutor<Faculty> {
    Optional<Faculty> findByEmployeeId(String employeeId);
    Optional<Faculty> findByUserId(Long userId);
    List<Faculty> findByDepartmentId(Long departmentId);
    boolean existsByEmployeeId(String employeeId);
    boolean existsByUserId(Long userId);
}
