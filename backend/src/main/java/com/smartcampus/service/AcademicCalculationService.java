package com.smartcampus.service;

import com.smartcampus.dto.*;
import com.smartcampus.entity.*;
import com.smartcampus.exception.BadRequestException;
import com.smartcampus.repository.MarksRepository;
import com.smartcampus.repository.TimetableRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AcademicCalculationService {

    private final MarksRepository marksRepository;
    private final TimetableRepository timetableRepository;

    public AcademicCalculationService(MarksRepository marksRepository,
                                      TimetableRepository timetableRepository) {
        this.marksRepository = marksRepository;
        this.timetableRepository = timetableRepository;
    }

    public static int getGradePoint(String grade) {
        if (grade == null) {
            throw new BadRequestException("Grade cannot be null");
        }
        switch (grade.toUpperCase().trim()) {
            case "A+": case "S": return 10;
            case "A": return 9;
            case "B+": return 8;
            case "B": return 7;
            case "C": return 6;
            case "D": return 5;
            case "E": return 4;
            case "F": return 0;
            default:
                throw new BadRequestException("Unknown grade: " + grade);
        }
    }

    public SGPAResult calculateSGPA(SGPACalculationRequest request) {
        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new BadRequestException("Subjects list cannot be empty");
        }

        int totalCredits = 0;
        int totalCreditPoints = 0;

        for (SGPACalculationRequest.SubjectScore subject : request.getSubjects()) {
            if (subject.getSubjectName() == null || subject.getSubjectName().trim().isEmpty()) {
                throw new BadRequestException("Subject name cannot be missing");
            }
            if (subject.getCredits() == null || subject.getCredits() <= 0) {
                throw new BadRequestException("Credits must be greater than zero");
            }
            int gradePoint = getGradePoint(subject.getGrade());
            totalCredits += subject.getCredits();
            totalCreditPoints += (subject.getCredits() * gradePoint);
        }

        BigDecimal sgpa = BigDecimal.valueOf(totalCreditPoints)
                .divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);

        return SGPAResult.builder()
                .sgpa(sgpa)
                .totalCredits(totalCredits)
                .totalCreditPoints(totalCreditPoints)
                .subjects(request.getSubjects())
                .build();
    }

    public CGPAResult calculateCGPA(CGPACalculationRequest request) {
        if (request.getSemesters() == null || request.getSemesters().isEmpty()) {
            throw new BadRequestException("Semesters list cannot be empty");
        }

        BigDecimal weightedSum = BigDecimal.ZERO;
        int totalCredits = 0;
        Set<Integer> semestersSeen = new HashSet<>();

        for (CGPACalculationRequest.SemesterScore sem : request.getSemesters()) {
            if (sem.getSemester() == null || sem.getSemester() <= 0) {
                throw new BadRequestException("Semester number must be positive");
            }
            if (!semestersSeen.add(sem.getSemester())) {
                throw new BadRequestException("Duplicate semester number: " + sem.getSemester());
            }
            if (sem.getSgpa() == null || sem.getSgpa().compareTo(BigDecimal.ZERO) < 0 || sem.getSgpa().compareTo(BigDecimal.valueOf(10)) > 0) {
                throw new BadRequestException("SGPA must be between 0.0 and 10.0");
            }
            if (sem.getCredits() == null || sem.getCredits() <= 0) {
                throw new BadRequestException("Credits must be greater than zero");
            }

            weightedSum = weightedSum.add(sem.getSgpa().multiply(BigDecimal.valueOf(sem.getCredits())));
            totalCredits += sem.getCredits();
        }

        BigDecimal cgpa = weightedSum.divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);

        return CGPAResult.builder()
                .cgpa(cgpa)
                .totalCredits(totalCredits)
                .semesters(request.getSemesters())
                .build();
    }

    public WhatIfResult calculateWhatIfSGPA(WhatIfRequest request) {
        if (request.getSubjects() == null || request.getSubjects().isEmpty()) {
            throw new BadRequestException("Subjects list cannot be empty");
        }

        int totalCredits = 0;
        int currentPoints = 0;
        int projectedPoints = 0;

        for (WhatIfRequest.WhatIfSubject sub : request.getSubjects()) {
            if (sub.getCredits() == null || sub.getCredits() <= 0) {
                throw new BadRequestException("Credits must be greater than zero");
            }
            int currentGradePoint = getGradePoint(sub.getCurrentGrade());
            int projectedGradePoint = getGradePoint(sub.getProjectedGrade());

            totalCredits += sub.getCredits();
            currentPoints += (sub.getCredits() * currentGradePoint);
            projectedPoints += (sub.getCredits() * projectedGradePoint);
        }

        BigDecimal currentSGPA = BigDecimal.valueOf(currentPoints)
                .divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
        BigDecimal projectedSGPA = BigDecimal.valueOf(projectedPoints)
                .divide(BigDecimal.valueOf(totalCredits), 2, RoundingMode.HALF_UP);
        BigDecimal difference = projectedSGPA.subtract(currentSGPA);

        return WhatIfResult.builder()
                .currentSGPA(currentSGPA)
                .projectedSGPA(projectedSGPA)
                .difference(difference)
                .build();
    }

    public TargetCGPAResult calculateRequiredSGPA(TargetCGPARequest request) {
        if (request.getCurrentCGPA() == null || request.getCurrentCGPA().compareTo(BigDecimal.ZERO) < 0 || request.getCurrentCGPA().compareTo(BigDecimal.valueOf(10)) > 0) {
            throw new BadRequestException("Current CGPA must be between 0.0 and 10.0");
        }
        if (request.getTargetCGPA() == null || request.getTargetCGPA().compareTo(BigDecimal.ZERO) < 0 || request.getTargetCGPA().compareTo(BigDecimal.valueOf(10)) > 0) {
            throw new BadRequestException("Target CGPA must be between 0.0 and 10.0");
        }
        if (request.getCompletedCredits() == null || request.getCompletedCredits() < 0) {
            throw new BadRequestException("Completed credits cannot be negative");
        }
        if (request.getFutureCredits() == null || request.getFutureCredits() <= 0) {
            throw new BadRequestException("Future credits must be greater than zero");
        }

        // targetCGPA * (completed + future) - currentCGPA * completed
        BigDecimal targetPoints = request.getTargetCGPA().multiply(BigDecimal.valueOf(request.getCompletedCredits() + request.getFutureCredits()));
        BigDecimal currentPoints = request.getCurrentCGPA().multiply(BigDecimal.valueOf(request.getCompletedCredits()));
        BigDecimal neededPoints = targetPoints.subtract(currentPoints);

        BigDecimal requiredSGPA = neededPoints.divide(BigDecimal.valueOf(request.getFutureCredits()), 2, RoundingMode.HALF_UP);

        if (requiredSGPA.compareTo(BigDecimal.ZERO) < 0 || requiredSGPA.compareTo(BigDecimal.valueOf(10)) > 0) {
            return TargetCGPAResult.builder()
                    .possible(false)
                    .requiredFutureSGPA(requiredSGPA)
                    .explanation("The target CGPA of " + request.getTargetCGPA() + " is mathematically impossible to achieve with the given credit configurations.")
                    .build();
        }

        return TargetCGPAResult.builder()
                .possible(true)
                .requiredFutureSGPA(requiredSGPA)
                .explanation("The target CGPA is mathematically achievable. You need a future SGPA of " + requiredSGPA + ".")
                .build();
    }

    public AcademicSummary getStudentAcademicSummary(Student student) {
        List<Marks> marksList = marksRepository.findByStudentId(student.getId());

        // Build a mapping of subject id -> semester using Timetable slot allocations
        Map<Long, Integer> subjectSemesters = new HashMap<>();
        for (Timetable slot : timetableRepository.findAll()) {
            if (slot.getSubject() != null) {
                subjectSemesters.put(slot.getSubject().getId(), slot.getSemester());
            }
        }

        // Group subject scores by semester
        Map<Integer, List<SGPACalculationRequest.SubjectScore>> semesterScores = new TreeMap<>();
        for (Marks marks : marksList) {
            if (marks.getGrade() == null) continue;

            Long subjectId = marks.getSubject().getId();
            Integer semester = subjectSemesters.get(subjectId);

            // Fallback heuristics: check digits in course code
            if (semester == null) {
                semester = extractSemesterFromCode(marks.getSubject().getCode());
            }

            semesterScores.computeIfAbsent(semester, k -> new ArrayList<>()).add(
                    SGPACalculationRequest.SubjectScore.builder()
                            .subjectName(marks.getSubject().getName())
                            .credits(marks.getSubject().getCredits())
                            .grade(marks.getGrade())
                            .build()
            );
        }

        List<AcademicSummary.SemesterHistoryEntry> history = new ArrayList<>();
        BigDecimal cgpaWeightedSum = BigDecimal.ZERO;
        int totalCgpaCredits = 0;
        BigDecimal lastSgpa = BigDecimal.ZERO;

        for (Map.Entry<Integer, List<SGPACalculationRequest.SubjectScore>> entry : semesterScores.entrySet()) {
            SGPACalculationRequest sgpaRequest = SGPACalculationRequest.builder()
                    .subjects(entry.getValue())
                    .build();
            SGPAResult sgpaResult = calculateSGPA(sgpaRequest);

            history.add(AcademicSummary.SemesterHistoryEntry.builder()
                    .semester(entry.getKey())
                    .sgpa(sgpaResult.getSgpa())
                    .credits(sgpaResult.getTotalCredits())
                    .build());

            cgpaWeightedSum = cgpaWeightedSum.add(sgpaResult.getSgpa().multiply(BigDecimal.valueOf(sgpaResult.getTotalCredits())));
            totalCgpaCredits += sgpaResult.getTotalCredits();
            lastSgpa = sgpaResult.getSgpa();
        }

        BigDecimal currentCGPA = totalCgpaCredits > 0
                ? cgpaWeightedSum.divide(BigDecimal.valueOf(totalCgpaCredits), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return AcademicSummary.builder()
                .currentSGPA(lastSgpa)
                .currentCGPA(currentCGPA)
                .totalCredits(totalCgpaCredits)
                .semesterHistory(history)
                .build();
    }

    private int extractSemesterFromCode(String code) {
        if (code == null) return 1;
        Pattern pattern = Pattern.compile("\\d");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            try {
                int sem = Integer.parseInt(matcher.group());
                if (sem >= 1 && sem <= 8) {
                    return sem;
                }
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }
}
