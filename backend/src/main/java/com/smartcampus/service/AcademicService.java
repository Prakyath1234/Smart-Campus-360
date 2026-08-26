package com.smartcampus.service;

import com.smartcampus.dto.*;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AcademicService {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final MarksRepository marksRepository;
    private final TimetableRepository timetableRepository;
    private final NotificationService notificationService;

    public AcademicService(DepartmentRepository departmentRepository, StudentRepository studentRepository,
                           FacultyRepository facultyRepository, SubjectRepository subjectRepository,
                           AttendanceRepository attendanceRepository, MarksRepository marksRepository,
                           TimetableRepository timetableRepository, NotificationService notificationService) {
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.marksRepository = marksRepository;
        this.timetableRepository = timetableRepository;
        this.notificationService = notificationService;
    }

    // --- Department Methods ---
    @Transactional
    public Department createDepartment(Department dept) {
        if (departmentRepository.findByCode(dept.getCode()).isPresent()) {
            throw new BadRequestException("Department code already exists");
        }
        return departmentRepository.save(dept);
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // --- Student Profile Methods ---
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        return mapToStudentDto(student);
    }

    public StudentDto getStudentByUserId(Long userId) {
        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
        return mapToStudentDto(student);
    }

    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream().map(this::mapToStudentDto).collect(Collectors.toList());
    }

    // --- Faculty Profile Methods ---
    public FacultyDto getFacultyById(Long id) {
        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        return mapToFacultyDto(faculty);
    }

    public FacultyDto getFacultyByUserId(Long userId) {
        Faculty faculty = facultyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty profile not found"));
        return mapToFacultyDto(faculty);
    }

    public List<FacultyDto> getAllFaculty() {
        return facultyRepository.findAll().stream().map(this::mapToFacultyDto).collect(Collectors.toList());
    }

    // --- Subject Methods ---
    @Transactional
    public SubjectDto createSubject(SubjectDto dto) {
        if (subjectRepository.findByCode(dto.getCode()).isPresent()) {
            throw new BadRequestException("Subject code already exists");
        }
        Department dept = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
        Faculty faculty = null;
        if (dto.getFacultyId() != null) {
            faculty = facultyRepository.findById(dto.getFacultyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Faculty not found"));
        }

        Subject subject = Subject.builder()
                .name(dto.getName())
                .code(dto.getCode())
                .department(dept)
                .faculty(faculty)
                .credits(dto.getCredits())
                .build();
        return mapToSubjectDto(subjectRepository.save(subject));
    }

    public List<SubjectDto> getAllSubjects() {
        return subjectRepository.findAll().stream().map(this::mapToSubjectDto).collect(Collectors.toList());
    }

    // --- Attendance Methods ---
    @Transactional
    public AttendanceDto recordAttendance(AttendanceDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (attendanceRepository.findByStudentIdAndSubjectIdAndDate(dto.getStudentId(), dto.getSubjectId(), dto.getDate()).isPresent()) {
            throw new BadRequestException("Attendance already recorded for this student, subject, and date");
        }

        AttendanceStatus status;
        try {
            status = AttendanceStatus.valueOf(dto.getStatus().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Invalid status. Must be PRESENT or ABSENT");
        }

        Attendance attendance = Attendance.builder()
                .student(student)
                .subject(subject)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .status(status)
                .build();

        return mapToAttendanceDto(attendanceRepository.save(attendance));
    }

    public List<AttendanceDto> getStudentAttendance(Long studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::mapToAttendanceDto)
                .collect(Collectors.toList());
    }

    public double getAttendancePercentage(Long studentId, Long subjectId) {
        long total = attendanceRepository.countByStudentIdAndSubjectId(studentId, subjectId);
        if (total == 0) return 0.0;
        long present = attendanceRepository.countByStudentIdAndSubjectIdAndStatus(studentId, subjectId, AttendanceStatus.PRESENT);
        return ((double) present / total) * 100.0;
    }

    // --- Marks Methods ---
    @Transactional
    public MarksDto enterMarks(MarksDto dto) {
        Student student = studentRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        if (dto.getInternalMarks() < 0 || dto.getInternalMarks() > 20) {
            throw new BadRequestException("Internal marks must be between 0 and 20");
        }
        if (dto.getAssignmentMarks() < 0 || dto.getAssignmentMarks() > 10) {
            throw new BadRequestException("Assignment marks must be between 0 and 10");
        }
        if (dto.getExamMarks() < 0 || dto.getExamMarks() > 70) {
            throw new BadRequestException("Exam marks must be between 0 and 70");
        }

        Marks marks = marksRepository.findByStudentIdAndSubjectId(dto.getStudentId(), dto.getSubjectId())
                .orElse(Marks.builder().student(student).subject(subject).build());

        marks.setInternalMarks(dto.getInternalMarks());
        marks.setAssignmentMarks(dto.getAssignmentMarks());
        marks.setExamMarks(dto.getExamMarks());
        
        double total = marks.getInternalMarks() + marks.getAssignmentMarks() + marks.getExamMarks();
        marks.setTotalMarks(total);
        marks.setGrade(calculateGrade(total));

        marks = marksRepository.save(marks);

        // Notify student of marks release
        notificationService.createNotification(student.getUser(), "Marks updated for " + subject.getName(),
                "Your marks have been updated. Total: " + total + "/100. Grade: " + marks.getGrade(), NotificationType.ACADEMIC);

        return mapToMarksDto(marks);
    }

    public List<MarksDto> getStudentMarks(Long studentId) {
        return marksRepository.findByStudentId(studentId).stream()
                .map(this::mapToMarksDto)
                .collect(Collectors.toList());
    }

    private String calculateGrade(double score) {
        if (score >= 90) return "A+";
        if (score >= 80) return "A";
        if (score >= 70) return "B";
        if (score >= 60) return "C";
        if (score >= 50) return "D";
        if (score >= 40) return "E";
        return "F";
    }

    // --- Timetable Methods ---
    @Transactional
    public TimetableDto createTimetableSlot(TimetableDto dto) {
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));

        validateTimetableConflict(dto, subject);

        Timetable timetable = Timetable.builder()
                .dayOfWeek(dto.getDayOfWeek().toUpperCase())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .subject(subject)
                .classroom(dto.getClassroom())
                .semester(dto.getSemester())
                .build();

        return mapToTimetableDto(timetableRepository.save(timetable));
    }

    public List<TimetableDto> getTimetableForSemester(Integer semester) {
        return timetableRepository.findBySemester(semester).stream()
                .map(this::mapToTimetableDto)
                .collect(Collectors.toList());
    }

    private void validateTimetableConflict(TimetableDto dto, Subject subject) {
        LocalTime start = dto.getStartTime();
        LocalTime end = dto.getEndTime();

        if (start.isAfter(end) || start.equals(end)) {
            throw new BadRequestException("Start time must be before end time");
        }

        String day = dto.getDayOfWeek().toUpperCase();

        // 1. Faculty conflict
        if (subject.getFaculty() != null) {
            List<Timetable> facultyConflicts = timetableRepository.findByDayOfWeekAndSubjectFacultyId(day, subject.getFaculty().getId());
            for (Timetable c : facultyConflicts) {
                if (!c.getId().equals(dto.getId()) && timeOverlaps(c.getStartTime(), c.getEndTime(), start, end)) {
                    throw new BadRequestException("Scheduling Conflict: Faculty " + subject.getFaculty().getUser().getName() + 
                            " is already teaching in " + c.getClassroom() + " during this slot (" + c.getStartTime() + " - " + c.getEndTime() + ").");
                }
            }
        }

        // 2. Classroom conflict
        List<Timetable> roomConflicts = timetableRepository.findByDayOfWeekAndClassroom(day, dto.getClassroom());
        for (Timetable c : roomConflicts) {
            if (!c.getId().equals(dto.getId()) && timeOverlaps(c.getStartTime(), c.getEndTime(), start, end)) {
                throw new BadRequestException("Scheduling Conflict: Classroom " + dto.getClassroom() + 
                        " is already occupied by " + c.getSubject().getName() + " (" + c.getStartTime() + " - " + c.getEndTime() + ").");
            }
        }

        // 3. Class/semester conflict
        if (subject.getDepartment() != null) {
            List<Timetable> classConflicts = timetableRepository.findByDayOfWeekAndSemesterAndSubjectDepartmentId(day, dto.getSemester(), subject.getDepartment().getId());
            for (Timetable c : classConflicts) {
                if (!c.getId().equals(dto.getId()) && timeOverlaps(c.getStartTime(), c.getEndTime(), start, end)) {
                    throw new BadRequestException("Scheduling Conflict: Semester " + dto.getSemester() + " (" + subject.getDepartment().getCode() + 
                            ") already has a scheduled class for " + c.getSubject().getName() + " during this slot.");
                }
            }
        }
    }

    private boolean timeOverlaps(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    // --- Mappings ---
    private StudentDto mapToStudentDto(Student student) {
        return StudentDto.builder()
                .id(student.getId())
                .userId(student.getUser().getId())
                .name(student.getUser().getName())
                .email(student.getUser().getEmail())
                .phone(student.getUser().getPhone())
                .rollNumber(student.getRollNumber())
                .departmentId(student.getDepartment() != null ? student.getDepartment().getId() : null)
                .departmentName(student.getDepartment() != null ? student.getDepartment().getName() : null)
                .departmentCode(student.getDepartment() != null ? student.getDepartment().getCode() : null)
                .semester(student.getSemester())
                .enrollmentDate(student.getEnrollmentDate())
                .build();
    }

    private FacultyDto mapToFacultyDto(Faculty faculty) {
        return FacultyDto.builder()
                .id(faculty.getId())
                .userId(faculty.getUser().getId())
                .name(faculty.getUser().getName())
                .email(faculty.getUser().getEmail())
                .phone(faculty.getUser().getPhone())
                .employeeId(faculty.getEmployeeId())
                .departmentId(faculty.getDepartment() != null ? faculty.getDepartment().getId() : null)
                .departmentName(faculty.getDepartment() != null ? faculty.getDepartment().getName() : null)
                .designation(faculty.getDesignation())
                .build();
    }

    private SubjectDto mapToSubjectDto(Subject subject) {
        return SubjectDto.builder()
                .id(subject.getId())
                .name(subject.getName())
                .code(subject.getCode())
                .departmentId(subject.getDepartment() != null ? subject.getDepartment().getId() : null)
                .departmentName(subject.getDepartment() != null ? subject.getDepartment().getName() : null)
                .facultyId(subject.getFaculty() != null ? subject.getFaculty().getId() : null)
                .facultyName(subject.getFaculty() != null ? subject.getFaculty().getUser().getName() : null)
                .credits(subject.getCredits())
                .build();
    }

    private AttendanceDto mapToAttendanceDto(Attendance attendance) {
        return AttendanceDto.builder()
                .id(attendance.getId())
                .studentId(attendance.getStudent().getId())
                .studentName(attendance.getStudent().getUser().getName())
                .rollNumber(attendance.getStudent().getRollNumber())
                .subjectId(attendance.getSubject().getId())
                .subjectName(attendance.getSubject().getName())
                .subjectCode(attendance.getSubject().getCode())
                .date(attendance.getDate())
                .status(attendance.getStatus().name())
                .build();
    }

    private MarksDto mapToMarksDto(Marks marks) {
        return MarksDto.builder()
                .id(marks.getId())
                .studentId(marks.getStudent().getId())
                .studentName(marks.getStudent().getUser().getName())
                .rollNumber(marks.getStudent().getRollNumber())
                .subjectId(marks.getSubject().getId())
                .subjectName(marks.getSubject().getName())
                .internalMarks(marks.getInternalMarks())
                .assignmentMarks(marks.getAssignmentMarks())
                .examMarks(marks.getExamMarks())
                .totalMarks(marks.getTotalMarks())
                .grade(marks.getGrade())
                .build();
    }

    private TimetableDto mapToTimetableDto(Timetable t) {
        return TimetableDto.builder()
                .id(t.getId())
                .dayOfWeek(t.getDayOfWeek())
                .startTime(t.getStartTime())
                .endTime(t.getEndTime())
                .subjectId(t.getSubject().getId())
                .subjectName(t.getSubject().getName())
                .subjectCode(t.getSubject().getCode())
                .facultyName(t.getSubject().getFaculty() != null ? t.getSubject().getFaculty().getUser().getName() : "N/A")
                .classroom(t.getClassroom())
                .semester(t.getSemester())
                .build();
    }
}
