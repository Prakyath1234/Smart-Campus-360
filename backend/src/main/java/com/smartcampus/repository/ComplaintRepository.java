package com.smartcampus.repository;

import com.smartcampus.entity.Complaint;
import com.smartcampus.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findByStudentId(Long studentId);
    List<Complaint> findByStatus(ComplaintStatus status);
    long countByStatus(ComplaintStatus status);
}
