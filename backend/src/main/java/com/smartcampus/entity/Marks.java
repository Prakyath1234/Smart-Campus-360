package com.smartcampus.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "marks",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "subject_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "internal_marks")
    @Builder.Default
    private Double internalMarks = 0.0;

    @Column(name = "assignment_marks")
    @Builder.Default
    private Double assignmentMarks = 0.0;

    @Column(name = "exam_marks")
    @Builder.Default
    private Double examMarks = 0.0;

    @Column(name = "total_marks")
    @Builder.Default
    private Double totalMarks = 0.0;

    @Column(length = 2)
    private String grade;
}
