package com.smartcampus.service;

import com.smartcampus.dto.StatusHistoryDto;
import com.smartcampus.entity.StatusHistory;
import com.smartcampus.repository.StatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatusHistoryService {

    private final StatusHistoryRepository statusHistoryRepository;

    public StatusHistoryService(StatusHistoryRepository statusHistoryRepository) {
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public void recordStatusChange(String entityName, Long entityId, String oldStatus, String newStatus, String changedByEmail, String comment) {
        StatusHistory history = StatusHistory.builder()
                .entityName(entityName)
                .entityId(entityId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedByEmail(changedByEmail != null ? changedByEmail : "SYSTEM")
                .comment(comment)
                .build();
        statusHistoryRepository.save(history);
    }

    public List<StatusHistoryDto> getStatusHistory(String entityName, Long entityId) {
        return statusHistoryRepository.findByEntityNameAndEntityIdOrderByCreatedAtDesc(entityName, entityId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private StatusHistoryDto mapToDto(StatusHistory h) {
        return StatusHistoryDto.builder()
                .id(h.getId())
                .entityName(h.getEntityName())
                .entityId(h.getEntityId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .changedByEmail(h.getChangedByEmail())
                .comment(h.getComment())
                .createdAt(h.getCreatedAt())
                .build();
    }
}
