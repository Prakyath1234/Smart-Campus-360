package com.smartcampus.repository;

import com.smartcampus.entity.EmergencyAlert;
import com.smartcampus.entity.EmergencyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmergencyAlertRepository extends JpaRepository<EmergencyAlert, Long> {
    List<EmergencyAlert> findByStatus(EmergencyStatus status);
    List<EmergencyAlert> findByStatusNot(EmergencyStatus status);
    long countByStatus(EmergencyStatus status);
}
