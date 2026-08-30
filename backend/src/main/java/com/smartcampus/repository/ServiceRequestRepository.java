package com.smartcampus.repository;

import com.smartcampus.entity.ServiceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.List;

public interface ServiceRequestRepository extends JpaRepository<ServiceRequest, Long>, JpaSpecificationExecutor<ServiceRequest> {
    List<ServiceRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
