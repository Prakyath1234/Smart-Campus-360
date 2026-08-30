package com.smartcampus.smarttools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactInformation {
    private String email;
    private String phone;
    private String location;
    private String linkedin;
    private String github;
    private String portfolio;
}
