package com.smartcampus.service;

import com.smartcampus.dto.StudentPerformanceDto;
import com.smartcampus.entity.*;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PerformanceAnalyticsService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;

    public PerformanceAnalyticsService(StudentRepository studentRepository, UserRepository userRepository,
                                       AttendanceRepository attendanceRepository, MarksRepository marksRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
    }

    public StudentPerformanceDto getPerformanceForStudentEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user: " + email));

        return computePerformance(student);
    }

    public StudentPerformanceDto getPerformanceForStudentId(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        return computePerformance(student);
    }

    public List<StudentPerformanceDto> getAtRiskStudents() {
        return studentRepository.findAll().stream()
                .map(this::computePerformance)
                .filter(p -> "AT_RISK".equals(p.getRiskStatus()))
                .collect(Collectors.toList());
    }

    private StudentPerformanceDto computePerformance(Student student) {
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(student.getId());
        long totalClasses = attendanceList.size();
        long presentClasses = attendanceList.stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count();
        double attendancePct = totalClasses > 0 ? ((double) presentClasses / totalClasses) * 100.0 : 100.0;

        List<Marks> marksList = marksRepository.findByStudentId(student.getId());
        double avgMarks = marksList.isEmpty() ? 0.0 :
                marksList.stream().mapToDouble(m -> m.getTotalMarks() != null ? m.getTotalMarks() : 0.0).average().orElse(0.0);

        List<StudentPerformanceDto.SubjectScore> subjectScores = new ArrayList<>();
        for (Marks m : marksList) {
            long subjTotal = attendanceRepository.countByStudentIdAndSubjectId(student.getId(), m.getSubject().getId());
            long subjPresent = attendanceRepository.countByStudentIdAndSubjectIdAndStatus(student.getId(), m.getSubject().getId(), AttendanceStatus.PRESENT);
            double subjAtt = subjTotal > 0 ? ((double) subjPresent / subjTotal) * 100.0 : 100.0;

            subjectScores.add(StudentPerformanceDto.SubjectScore.builder()
                    .subjectName(m.getSubject().getName())
                    .subjectCode(m.getSubject().getCode())
                    .totalMarks(m.getTotalMarks())
                    .grade(m.getGrade())
                    .attendancePercentage(Math.round(subjAtt * 10.0) / 10.0)
                    .build());
        }

        String bestSubject = marksList.stream()
                .max(Comparator.comparingDouble(m -> m.getTotalMarks() != null ? m.getTotalMarks() : 0.0))
                .map(m -> m.getSubject().getName())
                .orElse("N/A");

        String weakestSubject = marksList.stream()
                .min(Comparator.comparingDouble(m -> m.getTotalMarks() != null ? m.getTotalMarks() : 0.0))
                .map(m -> m.getSubject().getName())
                .orElse("N/A");

        String riskStatus = "GOOD";
        List<String> riskReasons = new ArrayList<>();
        if (attendancePct < 75.0) {
            riskStatus = "AT_RISK";
            riskReasons.add(String.format("Attendance rate (%.1f%%) below mandatory 75%% threshold", attendancePct));
        }
        if (!marksList.isEmpty() && avgMarks < 50.0) {
            riskStatus = "AT_RISK";
            riskReasons.add(String.format("Average academic score (%.1f/100) below passing 50 mark threshold", avgMarks));
        }

        return StudentPerformanceDto.builder()
                .studentId(student.getId())
                .studentName(student.getUser().getName())
                .rollNumber(student.getRollNumber())
                .attendancePercentage(Math.round(attendancePct * 10.0) / 10.0)
                .totalClasses(totalClasses)
                .presentClasses(presentClasses)
                .averageMarks(Math.round(avgMarks * 10.0) / 10.0)
                .bestSubject(bestSubject)
                .weakestSubject(weakestSubject)
                .riskStatus(riskStatus)
                .riskReason(riskReasons.isEmpty() ? "Academic performance & attendance in good standing" : String.join(" | ", riskReasons))
                .subjectScores(subjectScores)
                .build();
    }
}
