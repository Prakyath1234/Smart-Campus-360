package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusHistoryDto {
    private Long id;
    private String entityName;
    private Long entityId;
    private String oldStatus;
    private String newStatus;
    private String changedByEmail;
    private String comment;
    private LocalDateTime createdAt;
}
