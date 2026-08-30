package com.smartcampus.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatIfRequest {
    private List<WhatIfSubject> subjects;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WhatIfSubject {
        private String subjectName;
        private Integer credits;
        private String currentGrade;
        private String projectedGrade;
    }
}
