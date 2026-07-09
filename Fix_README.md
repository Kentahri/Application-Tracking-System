# Fix README - Candidate, gói nâng cấp, CV, Application

Tài liệu này ghi lại hướng sửa đã chốt cho project sau khi đổi kịch bản thanh toán:

1. Candidate đăng nhập bằng bảng `candidates`, dùng `candidates.password_hash`, có endpoint auth riêng.
2. Hệ thống có bảng/entity gói nâng cấp riêng.
3. Candidate giữ FK tới gói nâng cấp hiện tại.
4. Candidate có `numberOfQueryQuota`, thể hiện số lượt query AI chatbot còn/được cấp.
5. Application có `priority`; priority phụ thuộc vào gói nâng cấp của Candidate.
6. Vẫn giữ `applications.candidate_id`; khi apply bằng `cvId` phải validate `application.candidate_id == application.cv_id.candidate_id`.

## 1. Hiện trạng project

Project đang dùng Spring Boot, Spring Data JPA và SQL Server.

Trong `src/main/resources/application.yaml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: none
```

Vì `ddl-auto: none`, Hibernate sẽ không tự thêm bảng/cột vào DB. Các thay đổi entity bên dưới cần đi kèm SQL migration.

Các entity liên quan:

- `Candidate.java`
  - Bảng: `candidates`
  - Đã cần thêm: `upgrade_package_id`, `password_hash`, `number_of_query_quota`.

- `UpgradePackage.java`
  - Bảng mới: `upgrade_packages`
  - Lưu thông tin gói nâng cấp, quota và priority.

- `Application.java`
  - Bảng: `applications`
  - Đã cần thêm: `priority`.

- `Cv.java`
  - Bảng: `cvs`
  - Hiện đã có `candidate_id`, nên 1 Candidate có thể có nhiều CV.

## 2. Entity đã thêm/sửa

### 2.1 Entity mới: UpgradePackage

File mới:

```text
src/main/java/ats/entity/UpgradePackage.java
```

Ý nghĩa: định nghĩa các gói nâng cấp mà Candidate có thể mua.

Các field chính:

- `packageName`: tên gói, ví dụ `FREE`, `BASIC`, `PREMIUM`.
- `description`: mô tả gói.
- `price`: giá gói.
- `numberOfQueryQuota`: số lượt AI chatbot mà gói cung cấp.
- `priority`: mức ưu tiên của Application khi Candidate thuộc gói này.

Gợi ý dữ liệu:

| Gói | numberOfQueryQuota | priority |
| --- | ---: | ---: |
| FREE | 0 | 0 |
| BASIC | 50 | 1 |
| PREMIUM | 200 | 2 |

### 2.2 Candidate

File:

```text
src/main/java/ats/entity/Candidate.java
```

Field mới:

```java
@ManyToOne
@JoinColumn(name = "upgrade_package_id")
private UpgradePackage upgradePackageId;

@Column(name = "password_hash", columnDefinition = "NVARCHAR(255)")
private String passwordHash;

@Builder.Default
@Column(name = "number_of_query_quota", nullable = false)
private Integer numberOfQueryQuota = 0;
```

Ý nghĩa:

- `upgradePackageId`: gói nâng cấp hiện tại của Candidate.
- `passwordHash`: mật khẩu hash để Candidate login bằng bảng `candidates`.
- `numberOfQueryQuota`: số lượt query AI chatbot hiện có của Candidate.

Lưu ý: theo style hiện tại của project, nhiều field quan hệ đang đặt tên dạng `candidateId`, `jobId`, `departmentId` dù kiểu dữ liệu là entity. Vì vậy README giữ tên `upgradePackageId` cho nhất quán với code hiện tại.

### 2.3 Application

File:

```text
src/main/java/ats/entity/Application.java
```

Field mới:

```java
@Builder.Default
@Column(name = "priority", nullable = false)
private Integer priority = 0;
```

Ý nghĩa:

- Application mới mặc định `priority = 0`.
- Khi Candidate apply, lấy priority từ gói nâng cấp hiện tại của Candidate.
- Nếu Candidate nâng cấp sau khi đã apply, có thể cập nhật lại priority cho các Application đang còn active của Candidate.

## 3. SQL migration đề xuất

Vì project đang `ddl-auto: none`, cần tạo/chạy script SQL.

```sql
CREATE TABLE upgrade_packages (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    package_name NVARCHAR(255) NOT NULL,
    description NVARCHAR(1000) NULL,
    price NUMERIC(15,2) NOT NULL,
    number_of_query_quota INT NOT NULL
        CONSTRAINT DF_upgrade_packages_number_of_query_quota DEFAULT 0,
    priority INT NOT NULL
        CONSTRAINT DF_upgrade_packages_priority DEFAULT 0,
    created_at DATETIME2 NULL,
    updated_at DATETIME2 NULL,
    is_deleted BIT NOT NULL
        CONSTRAINT DF_upgrade_packages_is_deleted DEFAULT 0,
    deleted_at DATETIME2 NULL
);

ALTER TABLE candidates
ADD upgrade_package_id BIGINT NULL;

ALTER TABLE candidates
ADD password_hash NVARCHAR(255) NULL;

ALTER TABLE candidates
ADD number_of_query_quota INT NOT NULL
    CONSTRAINT DF_candidates_number_of_query_quota DEFAULT 0;

ALTER TABLE applications
ADD priority INT NOT NULL
    CONSTRAINT DF_applications_priority DEFAULT 0;

ALTER TABLE candidates
ADD CONSTRAINT FK_candidates_upgrade_packages
FOREIGN KEY (upgrade_package_id) REFERENCES upgrade_packages(id);

CREATE INDEX IX_candidates_upgrade_package_id
ON candidates(upgrade_package_id)
WHERE is_deleted = 0;

CREATE INDEX IX_applications_job_stage_priority_created
ON applications(job_id, pipeline_stage_id, priority DESC, created_at DESC, id DESC)
WHERE is_deleted = 0;
```

Nếu DB đã có constraint/index trùng tên, cần kiểm tra trước khi chạy.

Seed gói mẫu:

```sql
INSERT INTO upgrade_packages
    (package_name, description, price, number_of_query_quota, priority, created_at, updated_at, is_deleted)
VALUES
    ('FREE', 'Default free package', 0, 0, 0, SYSDATETIME(), SYSDATETIME(), 0),
    ('BASIC', 'Basic upgrade package', 99000, 50, 1, SYSDATETIME(), SYSDATETIME(), 0),
    ('PREMIUM', 'Premium upgrade package', 199000, 200, 2, SYSDATETIME(), SYSDATETIME(), 0);
```

## 4. Kịch bản thanh toán mới

### 4.1 Luồng mua gói nâng cấp

Luồng đề xuất:

1. Candidate chọn một `UpgradePackage`.
2. Backend tạo payment transaction gắn với `candidateId` và `upgradePackageId`.
3. Candidate thanh toán qua cổng thanh toán.
4. Payment callback/webhook xác nhận giao dịch thành công.
5. Backend cập nhật Candidate:
   - `candidate.upgradePackageId = selectedPackage`
   - `candidate.numberOfQueryQuota = selectedPackage.numberOfQueryQuota`
6. Backend cập nhật priority cho Application nếu cần:
   - Application mới sau này lấy priority từ package.
   - Application đã tồn tại có thể được cập nhật lại theo package mới.

Pseudo code:

```java
UpgradePackage upgradePackage = upgradePackageRepository.findById(packageId)
        .orElseThrow(...);

candidate.setUpgradePackageId(upgradePackage);
candidate.setNumberOfQueryQuota(upgradePackage.getNumberOfQueryQuota());
```

Nếu muốn cộng dồn quota thay vì reset quota theo gói:

```java
candidate.setNumberOfQueryQuota(
        candidate.getNumberOfQueryQuota() + upgradePackage.getNumberOfQueryQuota()
);
```

Cần chốt rõ nghiệp vụ:

- Reset quota theo gói: dễ hiểu, phù hợp subscription.
- Cộng dồn quota: phù hợp mua credit.

### 4.2 Priority của Application

Khi tạo Application mới:

```java
Integer priority = 0;
if (candidate.getUpgradePackageId() != null
        && candidate.getUpgradePackageId().getPriority() != null) {
    priority = candidate.getUpgradePackageId().getPriority();
}

Application application = Application.builder()
        .jobId(job)
        .candidateId(candidate)
        .cvId(cv)
        .pipelineStageId(firstStage)
        .priority(priority)
        .build();
```

Khi Candidate nâng cấp gói sau khi đã apply, có 2 hướng:

- Chỉ áp dụng priority mới cho Application tạo sau thời điểm nâng cấp.
- Cập nhật lại priority cho toàn bộ Application active của Candidate.

Với yêu cầu "mức độ priority tuỳ thuộc vào gói nâng cấp của tài khoản", nên chọn hướng cập nhật lại Application active sau khi thanh toán thành công.

Repository có thể thêm:

```java
List<Application> findByCandidateId_Id(Long candidateId);
```

Sau payment thành công:

```java
for (Application application : applications) {
    application.setPriority(upgradePackage.getPriority());
}
```

### 4.3 Sort recruiter theo priority

Điểm sửa chính:

```text
src/main/java/ats/repository/ApplicationRepository.java
```

Query hiện tại:

```java
order by a.id desc
```

Đổi thành:

```java
order by coalesce(a.priority, 0) desc,
         a.createdAt desc,
         a.id desc
```

Full query đề xuất:

```java
@Query("""
        select a
        from Application a
        join fetch a.candidateId
        join fetch a.cvId
        join fetch a.pipelineStageId
        where a.jobId.id = :jobId
        order by coalesce(a.priority, 0) desc,
                 a.createdAt desc,
                 a.id desc
        """)
List<Application> findByJobIdWithDetails(@Param("jobId") Long jobId);
```

`JobServiceImpl.getKanbanBoard()` đang dùng query này để lấy Application rồi group theo stage, nên thứ tự trong từng stage sẽ đi theo sort trên.

### 4.4 DTO/mapper nên expose priority

Nên thêm `priority` vào:

- `src/main/java/ats/dto/kanban/KanbanApplicationResponse.java`
- `src/main/java/ats/dto/application/ApplicationDetailResponse.java`
- `src/main/java/ats/mapper/KanbanMapper.java`
- `src/main/java/ats/mapper/ApplicationMapper.java`

Ví dụ:

```java
private Integer priority;
```

Nếu entity và DTO cùng tên `priority`, MapStruct có thể tự map cho `KanbanMapper`.

## 5. Candidate đăng nhập bằng bảng candidates

Hướng đã chốt: Candidate login bằng bảng `candidates`, dùng `password_hash`, tạo endpoint riêng.

Endpoint đề xuất:

```http
POST /api/candidate/auth/login
```

Request:

```json
{
  "email": "candidate@example.com",
  "password": "123456"
}
```

Luồng xử lý:

1. Tìm Candidate bằng `CandidateRepository.findByEmail(email)`.
2. So sánh password bằng `PasswordEncoder.matches(rawPassword, candidate.getPasswordHash())`.
3. Nếu đúng, tạo JWT có authority `ROLE_CANDIDATE`.
4. Trả về access token và thông tin Candidate.

Nên tạo riêng:

- `CandidateUserDetails`
- `CandidateUserDetailsService`
- `CandidateAuthService`
- `CandidateAuthController`

`CandidateUserDetails.getAuthorities()` cần trả:

```java
List.of(new SimpleGrantedAuthority("ROLE_CANDIDATE"))
```

Vì `JwtService.generateToken(...)` hiện lấy role từ authorities:

```java
.claim("role", user.getAuthorities().iterator().next().getAuthority())
```

Token Candidate sẽ có role `ROLE_CANDIDATE`.

## 6. SecurityConfig và JwtFilter

Trong `SecurityConfig`, thêm:

```java
.requestMatchers("/api/candidate/auth/**").permitAll()
.requestMatchers("/api/candidate/**").hasRole("CANDIDATE")
```

Rule `permitAll` cho `/api/candidate/auth/**` phải đứng trước rule `/api/candidate/**`.

Trong `JwtFilter`, request vào `/api/candidate/**` cần load user từ bảng `candidates`.

Pseudo code:

```java
UserDetails user;

if (request.getServletPath().startsWith("/api/candidate/")) {
    user = candidateUserDetailsService.loadUserByUsername(username);
} else {
    user = userDetailsService.loadUserByUsername(username);
}
```

Như vậy:

- Admin/Recruiter/Interviewer vẫn login bằng bảng `users`.
- Candidate login bằng bảng `candidates`.

## 7. Candidate có nhiều CV và apply bằng cvId

DB hiện tại đã hỗ trợ 1 Candidate có nhiều CV vì `cvs.candidate_id` trỏ về `candidates.id`.

Nên thêm repository method:

```java
List<Cv> findByCandidateId_Id(Long candidateId);
```

Endpoint đề xuất:

```http
POST /api/candidate/cvs
GET  /api/candidate/cvs
DELETE /api/candidate/cvs/{cvId}
```

Khi upload/list/delete CV, Candidate phải lấy từ JWT, không lấy `candidateId` từ request body.

Apply bằng CV đã có:

```http
POST /api/candidate/jobs/{jobId}/apply
```

Request:

```json
{
  "cvId": 10,
  "message": "I want to apply for this job"
}
```

Validation bắt buộc:

```java
if (!Objects.equals(cv.getCandidateId().getId(), candidate.getId())) {
    throw new UnauthorizedException("CV does not belong to current candidate");
}
```

Khi tạo Application, vẫn giữ `candidateId`:

```java
Application application = Application.builder()
        .jobId(job)
        .candidateId(candidate)
        .cvId(cv)
        .pipelineStageId(firstStage)
        .priority(priority)
        .build();
```

Quy tắc dữ liệu phải luôn đúng:

```text
application.candidate_id == application.cv_id.candidate_id
```

## 8. Check trùng Application

Vì yêu cầu là 1 Candidate có nhiều CV, mỗi CV có thể apply nhiều Job, nên check trùng nên theo cặp `job_id + cv_id`.

Thêm method vào `ApplicationRepository`:

```java
Application findByCvId_IdAndJobId_Id(Long cvId, Long jobId);
```

Thêm unique index:

```sql
CREATE UNIQUE INDEX UX_applications_job_cv_active
ON applications(job_id, cv_id)
WHERE is_deleted = 0;
```

Ý nghĩa:

- Cùng một CV không apply cùng một Job nhiều lần.
- Candidate vẫn có thể dùng CV khác để apply cùng Job nếu nghiệp vụ cho phép.

Nếu muốn 1 Candidate chỉ được apply 1 lần cho 1 Job bất kể CV nào, dùng unique index theo `job_id + candidate_id` thay vì `job_id + cv_id`.

## 9. Thứ tự implement đề xuất

1. Chạy migration tạo `upgrade_packages`.
2. Thêm các cột mới:
   - `candidates.upgrade_package_id`
   - `candidates.password_hash`
   - `candidates.number_of_query_quota`
   - `applications.priority`
3. Seed các gói `FREE`, `BASIC`, `PREMIUM`.
4. Sửa Candidate auth:
   - `CandidateUserDetails`
   - `CandidateUserDetailsService`
   - `CandidateAuthService`
   - `CandidateAuthController`
   - `SecurityConfig`
   - `JwtFilter`
5. Sửa Candidate CV management.
6. Sửa apply bằng `cvId` và validate CV thuộc Candidate.
7. Khi tạo Application, set `priority` từ `candidate.upgradePackageId.priority`.
8. Sửa sort recruiter theo `Application.priority DESC`, rồi `createdAt DESC`.
9. Thêm DTO/mapper expose `priority`.
10. Làm payment:
    - tạo payment transaction
    - callback thành công set gói cho Candidate
    - cập nhật `numberOfQueryQuota`
    - cập nhật `priority` cho Application active nếu chọn áp dụng ngay.

Chạy test/build:

```powershell
.\mvnw.cmd test
```

## 10. Lưu ý nên sửa thêm

Các annotation `@SQLDelete` trong nhiều entity hiện đang dùng `update_at`, trong khi `BaseEntity` map cột là `updated_at`.

Ví dụ:

```java
@SQLDelete(sql = "UPDATE applications SET is_deleted = 1, update_at = CURRENT_TIMESTAMP, deleted_at = CURRENT_TIMESTAMP WHERE id = ? and is_deleted = 0")
```

Nên đổi `update_at` thành `updated_at` nếu DB thật sự dùng cột `updated_at`, nếu không soft delete có thể lỗi runtime.
