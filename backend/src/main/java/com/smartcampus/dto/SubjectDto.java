package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubjectDto {
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private String departmentName;
    private Long facultyId;
    private String facultyName;
    private Integer credits;
}
