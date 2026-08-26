package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacultyDto {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String employeeId;
    private Long departmentId;
    private String departmentName;
    private String designation;
}
