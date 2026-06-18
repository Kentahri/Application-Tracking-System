package ats.exception;

import lombok.Getter;

@Getter
public class InputValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String fieldName;
    private final String errorMessage;

    public InputValidationException(String fieldName, String errorMessage) {
        super(errorMessage);
        this.fieldName = fieldName;
        this.errorMessage = errorMessage;
    }
}
