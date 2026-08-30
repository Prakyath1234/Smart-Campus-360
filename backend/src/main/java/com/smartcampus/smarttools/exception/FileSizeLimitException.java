package com.smartcampus.smarttools.exception;

import com.smartcampus.exception.BadRequestException;

public class FileSizeLimitException extends BadRequestException {
    public FileSizeLimitException(String message) {
        super(message);
    }
}
