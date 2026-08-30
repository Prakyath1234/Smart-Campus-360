package com.smartcampus.repository;

import com.smartcampus.entity.MentorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MentorNoteRepository extends JpaRepository<MentorNote, Long> {
    List<MentorNote> findByStudentIdOrderByCreatedAtDesc(Long studentId);
    List<MentorNote> findByMentorIdAndStudentIdOrderByCreatedAtDesc(Long mentorId, Long studentId);
}
