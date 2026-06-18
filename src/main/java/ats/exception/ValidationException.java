package ats.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String fieldName;
    private final String errorCode;

    public ValidationException(String message) {
        super(message);
        this.fieldName = null;
        this.errorCode = null;
    }

    public ValidationException(String fieldName, String errorCode, String message) {
        super(message);
        this.fieldName = fieldName;
        this.errorCode = errorCode;
    }
}