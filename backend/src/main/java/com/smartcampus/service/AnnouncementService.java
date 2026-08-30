package com.smartcampus.service;

import com.smartcampus.dto.AnnouncementDto;
import com.smartcampus.entity.Announcement;
import com.smartcampus.entity.Department;
import com.smartcampus.entity.User;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.AnnouncementRepository;
import com.smartcampus.repository.DepartmentRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository, DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.announcementRepository = announcementRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AnnouncementDto createAnnouncement(AnnouncementDto dto, String authorEmail) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Announcement title is required");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BadRequestException("Announcement body content is required");
        }

        User author = userRepository.findByEmail(authorEmail).orElse(null);
        String authorName = author != null ? author.getName() : "Campus Administration";

        Department dept = null;
        if (dto.getTargetDepartmentId() != null) {
            dept = departmentRepository.findById(dto.getTargetDepartmentId()).orElse(null);
        }

        Announcement announcement = Announcement.builder()
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .targetAudience(dto.getTargetAudience() != null ? dto.getTargetAudience().toUpperCase() : "ALL_STUDENTS")
                .targetDepartment(dept)
                .targetSemester(dto.getTargetSemester())
                .authorName(authorName)
                .published(dto.getPublished() != null ? dto.getPublished() : true)
                .build();

        return mapToDto(announcementRepository.save(announcement));
    }

    public List<AnnouncementDto> getPublishedAnnouncements() {
        return announcementRepository.findByPublishedTrueOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<AnnouncementDto> getAllAnnouncements() {
        return announcementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteAnnouncement(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new ResourceNotFoundException("Announcement not found with ID: " + id);
        }
        announcementRepository.deleteById(id);
    }

    private AnnouncementDto mapToDto(Announcement a) {
        return AnnouncementDto.builder()
                .id(a.getId())
                .title(a.getTitle())
                .content(a.getContent())
                .targetAudience(a.getTargetAudience())
                .targetDepartmentId(a.getTargetDepartment() != null ? a.getTargetDepartment().getId() : null)
                .targetDepartmentName(a.getTargetDepartment() != null ? a.getTargetDepartment().getName() : null)
                .targetSemester(a.getTargetSemester())
                .authorName(a.getAuthorName())
                .published(a.isPublished())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
