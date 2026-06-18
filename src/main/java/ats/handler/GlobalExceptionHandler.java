package ats.handler;

import ats.constant.ResponseMessage;
import ats.exception.BadRequestException;
import ats.exception.InputValidationException;
import ats.exception.NotFoundException;
import ats.exception.UnauthorizedException;
import ats.exception.ValidationException;
import ats.http.ResponseBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.FileSystemException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errors = extractFieldErrors(ex.getBindingResult());
        return badRequest(ResponseMessage.VALIDATION_FAILED, errors);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        String message = (ex.getCause() != null) ? ex.getCause().getMessage() : ex.getMessage();
        return badRequest(ResponseMessage.INVALID, message);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(@NonNull HttpMessageNotWritableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(@NonNull NoResourceFoundException ex,
                                                                    @NonNull HttpHeaders headers,
                                                                    @NonNull HttpStatusCode status,
                                                                    @NonNull WebRequest request) {
        log.debug("NoResourceFoundException: {}", ex.getMessage());
        return ResponseBuilder.error(HttpStatus.NOT_FOUND, ResponseMessage.NOT_FOUND, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex,
                                                                   @NonNull HttpHeaders headers,
                                                                   @NonNull HttpStatusCode status,
                                                                   @NonNull WebRequest request) {
        log.debug("NoHandlerFoundException: {}", ex.getMessage());
        return ResponseBuilder.error(HttpStatus.NOT_FOUND, ResponseMessage.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<Object> handleBindException(BindException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errors = extractFieldErrors(ex.getBindingResult());
        return badRequest(ResponseMessage.VALIDATION_FAILED, errors);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        log.error(ex.getMessage(), ex);
        return badRequest(ResponseMessage.VALIDATION_FAILED, ex.getMessage());
    }

    @ExceptionHandler(InputValidationException.class)
    public ResponseEntity<Object> handleInputValidationException(InputValidationException ex) {
        log.error(ex.getMessage(), ex);
        return badRequest(
                ResponseMessage.VALIDATION_FAILED,
                Map.of(ex.getFieldName(), ex.getErrorMessage())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException ex) {
        log.error(ex.getMessage(), ex);
        return badRequest(ResponseMessage.INVALID, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) {
        log.error(ex.getMessage(), ex);
        return badRequest(ResponseMessage.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.UNAUTHORIZED, ResponseMessage.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        log.error(ex.getMessage(), ex);
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            errors.put(path, message);
        }
        return badRequest(ResponseMessage.VALIDATION_FAILED, errors);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(NotFoundException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.NOT_FOUND, ResponseMessage.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(MultipartException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.ERROR, "File upload failed");
    }

    @ExceptionHandler(FileSystemException.class)
    public ResponseEntity<Object> handleFileSystemException(FileSystemException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.ERROR, "File upload failed");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseBuilder.error(HttpStatus.INTERNAL_SERVER_ERROR, ResponseMessage.ERROR, ex.getMessage());
    }

    // region Private Methods

    private Map<String, String> extractFieldErrors(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            errors.put(error.getField(), Optional.ofNullable(error.getDefaultMessage()).orElse("Invalid"));
        }
        return errors;
    }

    private ResponseEntity<Object> badRequest(String message, Object error) {
        return ResponseBuilder.error(HttpStatus.BAD_REQUEST, message, error);
    }

    // endregion
}
