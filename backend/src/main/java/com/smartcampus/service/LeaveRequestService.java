package com.smartcampus.service;

import com.smartcampus.dto.LeaveRequestDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.FacultyRepository;
import com.smartcampus.repository.LeaveRequestRepository;
import com.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final NotificationService notificationService;

    public LeaveRequestService(LeaveRequestRepository leaveRequestRepository, StudentRepository studentRepository,
                               FacultyRepository facultyRepository, NotificationService notificationService) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public LeaveRequestDto applyLeave(LeaveRequestDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new BadRequestException("Start date must be before or equal to end date");
        }

        LeaveRequest leave = LeaveRequest.builder()
                .student(student)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .build();

        leave = leaveRequestRepository.save(leave);

        // Notify faculty in student's department if available
        if (student.getDepartment() != null) {
            List<Faculty> deptFaculties = facultyRepository.findByDepartmentId(student.getDepartment().getId());
            for (Faculty fac : deptFaculties) {
                notificationService.createNotification(fac.getUser(), "New Leave Request Filed",
                        "Student " + student.getUser().getName() + " has requested leave from " + dto.getStartDate() + " to " + dto.getEndDate(),
                        NotificationType.LEAVE);
            }
        }

        return mapToDto(leave);
    }

    public List<LeaveRequestDto> getStudentLeaves(Long studentId) {
        return leaveRequestRepository.findByStudentId(studentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDto> getFacultyLeaveRequests(Long facultyId) {
        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));
        // Show leaves for faculty's department
        if (faculty.getDepartment() == null) {
            return leaveRequestRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
        }
        return leaveRequestRepository.findByStudentDepartmentId(faculty.getDepartment().getId()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestDto> getAllLeaveRequests() {
        return leaveRequestRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestDto reviewLeave(Long leaveId, String statusString, String comments, Long facultyId) {
        LeaveRequest leave = leaveRequestRepository.findById(leaveId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));

        LeaveStatus status;
        try {
            status = LeaveStatus.valueOf(statusString.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status. Must be APPROVED or REJECTED");
        }

        leave.setStatus(status);
        leave.setReviewedBy(faculty);
        leave.setReviewComments(comments);

        leave = leaveRequestRepository.save(leave);

        // Notify student
        notificationService.createNotification(leave.getStudent().getUser(), "Leave Request Review",
                "Your leave request has been " + status.name() + " by " + faculty.getUser().getName() + ". Comments: " + comments,
                NotificationType.LEAVE);

        return mapToDto(leave);
    }

    private LeaveRequestDto mapToDto(LeaveRequest leave) {
        return LeaveRequestDto.builder()
                .id(leave.getId())
                .studentId(leave.getStudent().getId())
                .studentName(leave.getStudent().getUser().getName())
                .rollNumber(leave.getStudent().getRollNumber())
                .departmentName(leave.getStudent().getDepartment() != null ? leave.getStudent().getDepartment().getName() : "N/A")
                .startDate(leave.getStartDate())
                .endDate(leave.getEndDate())
                .reason(leave.getReason())
                .status(leave.getStatus().name())
                .reviewedByFacultyId(leave.getReviewedBy() != null ? leave.getReviewedBy().getId() : null)
                .reviewedByFacultyName(leave.getReviewedBy() != null ? leave.getReviewedBy().getUser().getName() : null)
                .reviewComments(leave.getReviewComments())
                .createdAt(leave.getCreatedAt())
                .build();
    }
}
