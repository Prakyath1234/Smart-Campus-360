package com.smartcampus.repository;

import com.smartcampus.entity.LeaveRequest;
import com.smartcampus.entity.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    List<LeaveRequest> findByStudentId(Long studentId);
    List<LeaveRequest> findByStudentDepartmentId(Long departmentId);
    List<LeaveRequest> findByStatus(LeaveStatus status);
    long countByStatus(LeaveStatus status);
}
