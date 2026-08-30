package com.smartcampus.smarttools.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceEntry {
    private String company;
    private String title;
    private String startDate;
    private String endDate;
    private boolean current;
    private List<String> description;
}
