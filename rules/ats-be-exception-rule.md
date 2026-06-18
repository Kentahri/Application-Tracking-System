# Project Rules — ATS Backend (Spring Boot)

## 1. Tổng quan

Dự án backend cho hệ thống ATS (Application Tracking System). Sử dụng **Spring Boot + Java**.

```
ATS_WEB/
├── FE_ATS/    ← Frontend (React) — cổng 3000
└── BE_ATS/    ← Backend (Spring Boot) — cổng 8080
```

## 2. Tech Stack

| Lớp | Công nghệ |
|------|-----------|
| Framework | Spring Boot 3 |
| Database | JPA / Hibernate |
| Validation | Jakarta Validation (`@Valid`, `@NotNull`, `@NotBlank`, `@Size`…) |
| Auth | JWT (`JwtFilter` trong `config/`) |
| Logging | SLF4J + Lombok `@Slf4j` |
| Build | Maven |

## 3. Cấu trúc Package

```
src/main/java/ats/
├── config/              # Cấu hình (JwtFilter, SecurityConfig…)
├── constant/            # Hằng số (ResponseMessage, UserRole…)
├── controller/          # REST Controller
├── dto/                 # DTO (tổ chức theo feature: auth/, job/, department/…)
│   └── <feature>/
│       ├── XxxRequest.java
│       ├── XxxResponse.java
│       ├── XxxUpdateRequest.java
│       └── XxxDeleteRequest.java
├── entity/              # JPA Entity
├── exception/           # Custom Exception
├── handler/             # GlobalExceptionHandler
├── helper/              # Helper / Utility
├── http/                # ApiResponse, ResponseBuilder
├── mapper/              # MapStruct / Manual Mapper
├── repository/           # JPA Repository
└── service/             # Service Interface + Impl
    └── impl/
```

## 4. Exception Architecture (QUAN TRỌNG)

### 4.1. Exception Classes

| Exception | Gói | Dùng khi |
|-----------|-----|---------|
| `NotFoundException` | `ats.exception` | Entity không tồn tại trong DB |
| `BadRequestException` | `ats.exception` | Request không hợp lệ về mặt nghiệp vụ |
| `InputValidationException` | `ats.exception` | Validation lỗi theo field cụ thể |
| `UnauthorizedException` | `ats.exception` | Lỗi xác thực (401) |
| `ValidationException` (jakarta) | `jakarta.validation` | Lỗi validation tổng quát trong service |

### 4.2. Exception Definitions

```java
// NotFoundException — khi không tìm thấy entity
throw new NotFoundException("Job", "id", id);
// → "Job not found with id : '123'"

// BadRequestException — lỗi nghiệp vụ (duplicate, invalid state…)
throw new BadRequestException("Tiêu đề công việc đã tồn tại");

// InputValidationException — validation theo field
throw new InputValidationException("email", "Email không hợp lệ");

// UnauthorizedException — lỗi auth
throw new UnauthorizedException("Token không hợp lệ");
```

**QUAN TRỌNG:** Không throw `RuntimeException` thuần. Luôn dùng custom exception có sẵn trong `ats.exception`.

### 4.3. GlobalExceptionHandler

`ats.handler.GlobalExceptionHandler` xử lý tất cả exception và trả về **actual message** từ exception, không hardcode.

**Quy tắc Handler:**
- Extend `ResponseEntityExceptionHandler`
- Override các method của base class cho Spring validation errors
- Mỗi `@ExceptionHandler` trả về `ex.getMessage()` trong field `errors`, **KHÔNG hardcode message**
- Handler cho `Exception` cuối cùng luôn trả về actual error message

### 4.4. Service Layer — Exception Patterns

#### Pattern 1: Entity not found

```java
// ✅ Tốt
@Override
public JobResponse getJobById(Long id) {
    Job job = jobRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Job", "id", id));
    return jobMapper.toDto(job);
}

// ❌ Tránh
.orElseThrow(() -> new RuntimeException("Không tìm thấy công việc với id: " + id));
```

#### Pattern 2: Foreign key validation

```java
// ✅ Tốt — check trước khi tạo
@Override
public JobResponse create(JobRequest request) {
    if (!departmentRepository.existsById(request.getDepartmentId())) {
        throw new NotFoundException("Department", "id", request.getDepartmentId());
    }
    if (!userRepository.existsById(request.getRecruiterId())) {
        throw new NotFoundException("User", "id", request.getRecruiterId());
    }
    // ... tiếp tục
}

// ❌ Tránh: không check, để DB throw constraint violation
```

#### Pattern 3: Duplicate check (tạo mới)

```java
// ✅ Tốt
@Override
public JobResponse create(JobRequest request) {
    if (jobRepository.existsByTitle(request.getTitle())) {
        throw new BadRequestException("Tiêu đề công việc đã tồn tại: " + request.getTitle());
    }
    // ... tiếp tục
}

// ❌ Tránh
if (jobRepository.existsByTitle(...)) {
    throw new RuntimeException("...");
}
```

#### Pattern 4: Duplicate check (cập nhật)

```java
// ✅ Tốt — exclude chính nó khỏi check
@Override
public JobResponse update(Long id, JobUpdateRequest request) {
    Job job = jobRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Job", "id", id));

    if (request.getTitle() != null
            && !request.getTitle().equals(job.getTitle())
            && jobRepository.existsByTitleAndIdNot(request.getTitle(), id)) {
        throw new BadRequestException("Tiêu đề công việc đã tồn tại: " + request.getTitle());
    }
    // ...
}
```

### 4.5. DTO Validation

```java
// ✅ Tốt — dùng Jakarta Validation annotations
public class JobRequest {
    @NotNull(message = "Department ID không được để trống")
    private Long departmentId;

    @NotNull(message = "Recruiter ID không được để trống")
    private Long recruiterId;

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 500, message = "Tiêu đề không được vượt quá 500 ký tự")
    private String title;

    @Size(max = 500, message = "Địa điểm không được vượt quá 500 ký tự")
    private String location;
}

// ❌ Tránh: không validation ở DTO, check thủ công trong service
// (ngoại trừ các validation phức tạp cần business logic)
```

### 4.6. Controller Layer

```java
// ✅ Tốt — dùng @Valid để trigger validation
@PostMapping
public JobResponse create(@Valid @RequestBody JobRequest request) {
    return jobService.create(request);
}

// ❌ Tránh: thiếu @Valid
public JobResponse create(@RequestBody JobRequest request) {
    // validation sẽ không được trigger
}
```

## 5. Repository Conventions

```java
// ✅ Tốt — đặt tên theo Spring Data JPA convention
public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByTitle(String title);
    boolean existsByTitleAndIdNot(String title, Long id);
    Optional<Job> findByTitle(String title);
}

// ❌ Tránh
boolean existsTitle(String title);  // sai convention
```

## 6. Response Format

### 6.1. ApiResponse Structure

```json
{
  "success": false,
  "status": 400,
  "requestId": "uuid",
  "timestamp": "2026-06-18 14:00:00",
  "message": "Validation failed",
  "path": "/api/jobs",
  "data": null,
  "errors": {
    "title": "Tiêu đề không được để trống"
  },
  "duration": null
}
```

### 6.2. ResponseBuilder Usage

```java
// Thành công
return ResponseBuilder.ok(data);
return ResponseBuilder.created(data);

// Lỗi (GlobalExceptionHandler tự xử lý, không cần gọi trực tiếp trong controller/service)
return ResponseBuilder.error(HttpStatus.BAD_REQUEST, message, errors);
```

## 7. Naming Conventions

### 7.1. Package

- Lowercase, không dùng underscore: `ats.exception`, `ats.http`
- Feature-based trong dto: `ats.dto.auth`, `ats.dto.job`

### 7.2. Class

| Loại | Quy tắc | Ví dụ |
|------|---------|-------|
| Entity | PascalCase | `Job`, `Department`, `User` |
| DTO Request | PascalCase, suffix Request | `JobRequest`, `JobUpdateRequest` |
| DTO Response | PascalCase, suffix Response | `JobResponse`, `DepartmentResponse` |
| DTO Delete | PascalCase, suffix DeleteRequest | `JobDeleteRequest` |
| Repository | PascalCase, suffix Repository | `JobRepository`, `UserRepository` |
| Service | PascalCase, suffix Service/ServiceImpl | `JobService`, `JobServiceImpl` |
| Controller | PascalCase, suffix Controller | `JobController`, `AuthController` |
| Exception | PascalCase | `NotFoundException`, `BadRequestException` |

### 7.3. Field / Variable

- camelCase: `jobTitle`, `departmentId`, `recruiterId`
- Boolean prefix: `isActive`, `hasPermission`
- Collection plural: `List<Job> jobs`, `Set<Role> roles`

## 8. Logging

### 8.1. @Slf4j

```java
@Slf4j
@Service
public class JobServiceImpl {
    log.debug("...");  // Chi tiết, thường bật trong dev
    log.info("...");   // Thao tác nghiệp vụ chính (create, update, delete)
    log.warn("...");   // Cảnh báo (validation fail, duplicate check)
    log.error("...", ex); // Lỗi — luôn kèm exception
}
```

### 8.2. Quy tắc

```java
// ✅ Tốt
log.info("Creating job with title: {}", request.getTitle());
log.warn("Job not found with id: {}", id);
log.error("Unexpected error: {}", ex.getMessage(), ex);

// ❌ Tránh
log.info("Creating job");                // thiếu context
log.info("error: " + ex.getMessage());   // không dùng {} placeholder
log.error(ex);                           // không kèm message
```

## 9. Common Mistakes to Avoid

1. **Không throw RuntimeException thuần** — luôn dùng custom exception trong `ats.exception`
2. **Không hardcode message trong GlobalExceptionHandler** — trả về `ex.getMessage()`
3. **Không thiếu `@Valid`** ở Controller cho Request body
4. **Không validate foreign key** trước khi tạo entity
5. **Không check duplicate** trước khi create/update entity
6. **Không để `findById(id).orElseThrow()` trả về null** — luôn throw exception
7. **Không dùng `System.out.println`** — dùng log
8. **Không ignore exception trong catch block** — luôn log hoặc rethrow
