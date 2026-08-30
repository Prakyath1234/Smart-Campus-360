package com.smartcampus.smarttools.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SmartToolsResponse<T> {
    private boolean success;
    private String message;
    private int status;
    private T data;
}
