package com.smartcampus.smarttools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationEntry {
    private String degree;
    private String institution;
    private String fieldOfStudy;
    private String startYear;
    private String endYear;
    private String cgpa;
}
