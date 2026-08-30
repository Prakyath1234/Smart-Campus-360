package com.smartcampus.dto;

import com.smartcampus.smarttools.model.ParsedResume;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtsAnalysisRequest {
    private ParsedResume resume;
    private String jobDescription;
}
