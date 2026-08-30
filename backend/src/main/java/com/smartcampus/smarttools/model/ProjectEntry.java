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
public class ProjectEntry {
    private String projectName;
    private List<String> technologies;
    private String description;
}
