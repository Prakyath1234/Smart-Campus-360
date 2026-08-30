package com.smartcampus.service;

import com.smartcampus.entity.*;
import com.smartcampus.repository.*;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final ComplaintRepository complaintRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ReportService(StudentRepository studentRepository, AttendanceRepository attendanceRepository,
                         MarksRepository marksRepository, ComplaintRepository complaintRepository,
                         LeaveRequestRepository leaveRequestRepository, EmergencyAlertRepository emergencyAlertRepository,
                         ServiceRequestRepository serviceRequestRepository) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.complaintRepository = complaintRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    public String generateCsvReport(String type) {
        StringBuilder csv = new StringBuilder();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        switch (type.toLowerCase()) {
            case "students":
                csv.append("ID,Roll Number,Name,Email,Phone,Department,Semester\n");
                List<Student> students = studentRepository.findAll();
                for (Student s : students) {
                    csv.append(escape(s.getId())).append(",")
                       .append(escape(s.getRollNumber())).append(",")
                       .append(escape(s.getUser().getName())).append(",")
                       .append(escape(s.getUser().getEmail())).append(",")
                       .append(escape(s.getUser().getPhone())).append(",")
                       .append(escape(s.getDepartment() != null ? s.getDepartment().getName() : "N/A")).append(",")
                       .append(escape(s.getSemester())).append("\n");
                }
                break;

            case "attendance":
                csv.append("ID,Date,Roll Number,Student Name,Subject,Status\n");
                List<Attendance> attendanceList = attendanceRepository.findAll();
                for (Attendance a : attendanceList) {
                    csv.append(escape(a.getId())).append(",")
                       .append(escape(a.getDate())).append(",")
                       .append(escape(a.getStudent().getRollNumber())).append(",")
                       .append(escape(a.getStudent().getUser().getName())).append(",")
                       .append(escape(a.getSubject().getName())).append(",")
                       .append(escape(a.getStatus())).append("\n");
                }
                break;

            case "marks":
                csv.append("ID,Roll Number,Student Name,Subject,Internal Marks,Assignment Marks,Exam Marks,Total Marks,Grade\n");
                List<Marks> marksList = marksRepository.findAll();
                for (Marks m : marksList) {
                    csv.append(escape(m.getId())).append(",")
                       .append(escape(m.getStudent().getRollNumber())).append(",")
                       .append(escape(m.getStudent().getUser().getName())).append(",")
                       .append(escape(m.getSubject().getName())).append(",")
                       .append(escape(m.getInternalMarks())).append(",")
                       .append(escape(m.getAssignmentMarks())).append(",")
                       .append(escape(m.getExamMarks())).append(",")
                       .append(escape(m.getTotalMarks())).append(",")
                       .append(escape(m.getGrade())).append("\n");
                }
                break;

            case "complaints":
                csv.append("ID,Title,Category,Priority,Status,Student Name,Logged Date\n");
                List<Complaint> complaints = complaintRepository.findAll();
                for (Complaint c : complaints) {
                    csv.append(escape(c.getId())).append(",")
                       .append(escape(c.getTitle())).append(",")
                       .append(escape(c.getCategory())).append(",")
                       .append(escape(c.getPriority())).append(",")
                       .append(escape(c.getStatus())).append(",")
                       .append(escape(c.getStudent().getUser().getName())).append(",")
                       .append(escape(c.getCreatedAt() != null ? c.getCreatedAt().format(fmt) : "")).append("\n");
                }
                break;

            case "leaves":
                csv.append("ID,Roll Number,Student Name,Start Date,End Date,Reason,Status\n");
                List<LeaveRequest> leaves = leaveRequestRepository.findAll();
                for (LeaveRequest l : leaves) {
                    csv.append(escape(l.getId())).append(",")
                       .append(escape(l.getStudent().getRollNumber())).append(",")
                       .append(escape(l.getStudent().getUser().getName())).append(",")
                       .append(escape(l.getStartDate())).append(",")
                       .append(escape(l.getEndDate())).append(",")
                       .append(escape(l.getReason())).append(",")
                       .append(escape(l.getStatus())).append("\n");
                }
                break;

            case "emergency":
                csv.append("ID,Student Name,Roll Number,Emergency Type,Location,Status,Triggered Time\n");
                List<EmergencyAlert> alerts = emergencyAlertRepository.findAll();
                for (EmergencyAlert e : alerts) {
                    csv.append(escape(e.getId())).append(",")
                       .append(escape(e.getStudent().getUser().getName())).append(",")
                       .append(escape(e.getStudent().getRollNumber())).append(",")
                       .append(escape(e.getEmergencyType())).append(",")
                       .append(escape(e.getLocationText())).append(",")
                       .append(escape(e.getStatus())).append(",")
                       .append(escape(e.getCreatedAt() != null ? e.getCreatedAt().format(fmt) : "")).append("\n");
                }
                break;

            case "service-requests":
            default:
                csv.append("ID,Roll Number,Student Name,Request Type,Title,Status,Created Date\n");
                List<ServiceRequest> requests = serviceRequestRepository.findAll();
                for (ServiceRequest r : requests) {
                    csv.append(escape(r.getId())).append(",")
                       .append(escape(r.getStudent().getRollNumber())).append(",")
                       .append(escape(r.getStudent().getUser().getName())).append(",")
                       .append(escape(r.getRequestType())).append(",")
                       .append(escape(r.getTitle())).append(",")
                       .append(escape(r.getStatus())).append(",")
                       .append(escape(r.getCreatedAt() != null ? r.getCreatedAt().format(fmt) : "")).append("\n");
                }
                break;
        }

        return csv.toString();
    }

    private String escape(Object obj) {
        if (obj == null) return "\"\"";
        String str = obj.toString().replace("\"", "\"\"");
        return "\"" + str + "\"";
    }
}
