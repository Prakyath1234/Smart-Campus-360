package com.smartcampus.repository;

import com.smartcampus.entity.Timetable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimetableRepository extends JpaRepository<Timetable, Long> {
    List<Timetable> findBySemester(Integer semester);
    List<Timetable> findBySubjectDepartmentId(Long departmentId);
    List<Timetable> findBySubjectFacultyId(Long facultyId);
    
    // Check conflicts:
    // 1. Same faculty at same time on same day
    List<Timetable> findByDayOfWeekAndSubjectFacultyId(String dayOfWeek, Long facultyId);
    
    // 2. Same classroom at same time on same day
    List<Timetable> findByDayOfWeekAndClassroom(String dayOfWeek, String classroom);
    
    // 3. Same semester/department class at same time on same day
    List<Timetable> findByDayOfWeekAndSemesterAndSubjectDepartmentId(String dayOfWeek, Integer semester, Long departmentId);
}
