package com.smartcampus.repository;

import com.smartcampus.entity.Mentorship;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MentorshipRepository extends JpaRepository<Mentorship, Long> {
    List<Mentorship> findByMentorId(Long mentorId);
    Optional<Mentorship> findByMenteeId(Long menteeId);
    boolean existsByMenteeId(Long menteeId);
}
