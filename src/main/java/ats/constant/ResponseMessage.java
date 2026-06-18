package ats.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ResponseMessage {

    public static final String SUCCESS = "Success";
    public static final String FAILURE = "Failed";
    public static final String INVALID = "Invalid input";
    public static final String BAD_REQUEST = "Bad request";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String ERROR = "Error occurred";
    public static final String INTERNAL_SERVER_ERROR = "Internal server error";
    public static final String NOT_FOUND = "Not found";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String FORBIDDEN = "Forbidden";
    public static final String NO_CONTENT = "No content";
    public static final String PROCESSING = "Processing";
    public static final String CREATED = "Created";
    public static final String UPDATED = "Updated";
    public static final String DELETED = "Deleted";
    public static final String CONFLICT = "Conflict";
}
