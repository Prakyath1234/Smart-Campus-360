package com.smartcampus.smarttools.exception;

import com.smartcampus.exception.BadRequestException;

public class UnsupportedFileTypeException extends BadRequestException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
