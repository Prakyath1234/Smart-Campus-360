package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "emergency_type", nullable = false, length = 50)
    private String emergencyType;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double latitude;
    private Double longitude;

    @Column(name = "location_text", length = 255)
    private String locationText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EmergencyStatus status = EmergencyStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;
}
