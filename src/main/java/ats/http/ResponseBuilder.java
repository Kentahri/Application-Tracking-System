package ats.http;

import ats.helper.MessageHelper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public final class ResponseBuilder {

    private ResponseBuilder() {}

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(data, MessageHelper.getMessage("response.success"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return build(data, HttpStatus.OK, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok() {
        return ok(null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
            return ok(data, MessageHelper.getMessage("response.created"));
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(T data, String message) {
        return build(data, HttpStatus.BAD_REQUEST, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> badRequest(String message) {
        return badRequest(null, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return build(null, HttpStatus.NOT_FOUND, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound() {
        return notFound(MessageHelper.getMessage("response.notFound"));
    }

    public static ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static ResponseEntity<Object> error(
            HttpStatus status, String message, Object errors) {
        ApiResponse<Object> response = ApiResponse.<Object>builder()
                .success(status.is2xxSuccessful())
                .status(status.value())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(null)
                .errors(errors)
                .build();
        return ResponseEntity.status(status).body(response);
    }

    private static <T> ResponseEntity<ApiResponse<T>> build(
            T data, HttpStatus status, String message, Object errors) {

        ApiResponse<T> response = ApiResponse.<T>builder()
                .success(status.is2xxSuccessful())
                .status(status.value())
                .requestId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .errors(errors)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}