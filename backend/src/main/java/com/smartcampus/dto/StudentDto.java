package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String rollNumber;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private Integer semester;
    private LocalDate enrollmentDate;
}
