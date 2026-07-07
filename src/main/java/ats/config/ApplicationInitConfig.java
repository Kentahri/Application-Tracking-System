package ats.config;

import ats.constant.InterviewResult;
import ats.constant.InterviewStatus;
import ats.constant.JobStatus;
import ats.constant.UserRole;
import ats.constant.UserStatus;
import ats.entity.*;
import ats.entity.PipelineStage;
import ats.repository.ApplicationRepository;
import ats.repository.CandidateRepository;
import ats.repository.CvRepository;
import ats.repository.DepartmentRepository;
import ats.repository.InterviewRepository;
import ats.repository.JobRepository;
import ats.repository.PipelineStageRepository;
import ats.repository.StageTransitionRepository;
import ats.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @Bean
    ApplicationRunner init(DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           PipelineStageRepository pipelineStageRepository,
                           JobRepository jobRepository,
                           CandidateRepository candidateRepository,
                           CvRepository cvRepository,
                           ApplicationRepository applicationRepository,
                           StageTransitionRepository stageTransitionRepository,
                           InterviewRepository interviewRepository) {
        return args -> {
            Department engineering = getOrCreateDepartment(
                    departmentRepository,
                    "Engineering",
                    null,
                    "Software product development"
            );
            Department humanResources = getOrCreateDepartment(
                    departmentRepository,
                    "Human Resources",
                    null,
                    "People operations and recruiting"
            );
            Department marketing = getOrCreateDepartment(
                    departmentRepository,
                    "Marketing",
                    null,
                    "Brand and growth"
            );
            Department finance = getOrCreateDepartment(
                    departmentRepository,
                    "Finance",
                    null,
                    "Treasury, risk, and financial operations"
            );
            Department operations = getOrCreateDepartment(
                    departmentRepository,
                    "Operations",
                    null,
                    "Business operations and support"
            );
            Department dataAnalytics = getOrCreateDepartment(
                    departmentRepository,
                    "Data & Analytics",
                    null,
                    "Data platform and business intelligence"
            );
            Department security = getOrCreateDepartment(
                    departmentRepository,
                    "Information Security",
                    null,
                    "Cybersecurity and compliance"
            );

            User admin = getOrCreateUser(
                    userRepository,
                    "Admin@admin.com",
                    "System Admin",
                    UserRole.ADMIN,
                    humanResources
            );
            User recruiterA = getOrCreateUser(
                    userRepository,
                    "recruiter@ats.local",
                    "Recruiter One",
                    UserRole.RECRUITER,
                    humanResources
            );
            User recruiterB = getOrCreateUser(
                    userRepository,
                    "recruiter2@ats.local",
                    "Recruiter Two",
                    UserRole.RECRUITER,
                    humanResources
            );
            User interviewer = getOrCreateUser(
                    userRepository,
                    "interviewer@ats.local",
                    "Interview One",
                    UserRole.INTERVIEWER,
                    engineering
            );
            User candidate = getOrCreateUser(
                    userRepository,
                    "candidate@ats.local",
                    "Candidate User",
                    UserRole.CANDIDATE,
                    null
            );

            PipelineStage applied = getOrCreateStage(pipelineStageRepository, "Applied", 1);
            PipelineStage screening = getOrCreateStage(pipelineStageRepository, "Screening", 2);
            PipelineStage interviewStage = getOrCreateStage(pipelineStageRepository, "Interview", 3);
            PipelineStage offer = getOrCreateStage(pipelineStageRepository, "Offer", 4);
            PipelineStage rejected = getOrCreateStage(pipelineStageRepository, "Rejected", 5);

            Job backendJob = getOrCreateJob(
                    jobRepository,
                    "Backend Developer",
                    "Build REST APIs for the ATS platform",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(1200),
                    BigDecimal.valueOf(2500),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterA
            );
            Job frontendJob = getOrCreateJob(
                    jobRepository,
                    "Frontend Developer",
                    "Build recruiter and candidate web experiences",
                    "Da Nang",
                    BigDecimal.valueOf(1000),
                    BigDecimal.valueOf(2200),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterA
            );
            Job hrInternJob = getOrCreateJob(
                    jobRepository,
                    "HR Intern",
                    "Support screening and interview scheduling",
                    "Remote",
                    BigDecimal.valueOf(300),
                    BigDecimal.valueOf(600),
                    JobStatus.DRAFT,
                    humanResources,
                    recruiterB
            );
            Job marketingJob = getOrCreateJob(
                    jobRepository,
                    "Marketing Executive",
                    "Plan recruitment marketing campaigns",
                    "Ha Noi",
                    BigDecimal.valueOf(800),
                    BigDecimal.valueOf(1500),
                    JobStatus.CLOSED,
                    marketing,
                    recruiterB
            );
            Job mobileJob = getOrCreateJob(
                    jobRepository,
                    "Mobile Developer (iOS/Android)",
                    "Build cross-platform mobile apps for the ATS suite",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(1500),
                    BigDecimal.valueOf(2800),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterA
            );
            Job devopsJob = getOrCreateJob(
                    jobRepository,
                    "DevOps Engineer",
                    "Operate Kubernetes clusters and CI/CD pipelines",
                    "Remote",
                    BigDecimal.valueOf(1800),
                    BigDecimal.valueOf(3200),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterA
            );
            Job qaJob = getOrCreateJob(
                    jobRepository,
                    "QA Engineer (Automation)",
                    "Design end-to-end test suites with Playwright and Cypress",
                    "Da Nang",
                    BigDecimal.valueOf(1100),
                    BigDecimal.valueOf(2200),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterB
            );
            Job dataEngineerJob = getOrCreateJob(
                    jobRepository,
                    "Data Engineer",
                    "Build ETL pipelines and data lake on AWS",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(2000),
                    BigDecimal.valueOf(3500),
                    JobStatus.PUBLISHED,
                    dataAnalytics,
                    recruiterA
            );
            Job dataAnalystJob = getOrCreateJob(
                    jobRepository,
                    "Data Analyst",
                    "Turn business questions into dashboards and insights",
                    "Ha Noi",
                    BigDecimal.valueOf(1300),
                    BigDecimal.valueOf(2400),
                    JobStatus.PUBLISHED,
                    dataAnalytics,
                    recruiterB
            );
            Job securityEngineerJob = getOrCreateJob(
                    jobRepository,
                    "Security Engineer",
                    "Threat modeling, SAST/DAST, and incident response",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(2200),
                    BigDecimal.valueOf(3800),
                    JobStatus.PUBLISHED,
                    security,
                    recruiterA
            );
            Job complianceAnalystJob = getOrCreateJob(
                    jobRepository,
                    "Compliance Analyst",
                    "Maintain ISO 27001 and internal control documentation",
                    "Ha Noi",
                    BigDecimal.valueOf(1400),
                    BigDecimal.valueOf(2500),
                    JobStatus.PUBLISHED,
                    security,
                    recruiterB
            );
            Job treasuryJob = getOrCreateJob(
                    jobRepository,
                    "Treasury Officer",
                    "Manage daily cash position and FX exposure",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(1600),
                    BigDecimal.valueOf(2600),
                    JobStatus.PUBLISHED,
                    finance,
                    recruiterA
            );
            Job riskAnalystJob = getOrCreateJob(
                    jobRepository,
                    "Credit Risk Analyst",
                    "Score SME loan applications and monitor portfolio risk",
                    "Ha Noi",
                    BigDecimal.valueOf(1500),
                    BigDecimal.valueOf(2800),
                    JobStatus.DRAFT,
                    finance,
                    recruiterB
            );
            Job contentWriterJob = getOrCreateJob(
                    jobRepository,
                    "Content Writer",
                    "Write recruiting blog posts and social content",
                    "Remote",
                    BigDecimal.valueOf(700),
                    BigDecimal.valueOf(1200),
                    JobStatus.PUBLISHED,
                    marketing,
                    recruiterB
            );
            Job productDesignerJob = getOrCreateJob(
                    jobRepository,
                    "Product Designer (UX/UI)",
                    "Design recruiter and candidate journeys in Figma",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(1700),
                    BigDecimal.valueOf(3000),
                    JobStatus.PUBLISHED,
                    engineering,
                    recruiterA
            );
            Job operationsAnalystJob = getOrCreateJob(
                    jobRepository,
                    "Operations Analyst",
                    "Improve recruiting workflows with automation scripts",
                    "Da Nang",
                    BigDecimal.valueOf(900),
                    BigDecimal.valueOf(1600),
                    JobStatus.CLOSED,
                    operations,
                    recruiterB
            );
            Job recruiterCoordinatorJob = getOrCreateJob(
                    jobRepository,
                    "Recruiting Coordinator",
                    "Schedule interviews and own candidate communication",
                    "Ho Chi Minh City",
                    BigDecimal.valueOf(800),
                    BigDecimal.valueOf(1400),
                    JobStatus.PUBLISHED,
                    humanResources,
                    recruiterA
            );

            Candidate candidateA = getOrCreateCandidate(
                    candidateRepository,
                    "candidate.one@example.com",
                    "Candidate One",
                    "0901000001"
            );
            Candidate candidateB = getOrCreateCandidate(
                    candidateRepository,
                    "candidate.two@example.com",
                    "Candidate Two",
                    "0901000002"
            );
            Candidate candidateC = getOrCreateCandidate(
                    candidateRepository,
                    "candidate.three@example.com",
                    "Candidate Three",
                    "0901000003"
            );

            Cv cvA = getOrCreateCv(cvRepository, candidateA, "/sample/cvs/candidate-one.pdf", "PDF");
            Cv cvB = getOrCreateCv(cvRepository, candidateB, "/sample/cvs/candidate-two.pdf", "PDF");
            Cv cvC = getOrCreateCv(cvRepository, candidateC, "/sample/cvs/candidate-three.pdf", "PDF");

            Application appA = getOrCreateApplication(
                    applicationRepository,
                    backendJob,
                    candidateA,
                    cvA,
                    screening
            );
            Application appB = getOrCreateApplication(
                    applicationRepository,
                    backendJob,
                    candidateB,
                    cvB,
                    applied
            );
            Application appC = getOrCreateApplication(
                    applicationRepository,
                    frontendJob,
                    candidateC,
                    cvC,
                    interviewStage
            );

            getOrCreateTransition(stageTransitionRepository, appA, applied, screening, "Passed resume screening");
            getOrCreateTransition(stageTransitionRepository, appC, applied, screening, "Resume matched frontend role");
            getOrCreateTransition(stageTransitionRepository, appC, screening, interviewStage, "Scheduled technical interview");

            getOrCreateInterview(
                    interviewRepository,
                    appC,
                    interviewer,
                    LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                    "https://meet.example.com/frontend-candidate-three",
                    InterviewStatus.SCHEDULED,
                    60,
                    null,
                    InterviewResult.PENDING
            );
        };
    }

    private Department getOrCreateDepartment(DepartmentRepository repository,
                                             String name,
                                             Long parentId,
                                             String description) {
        Department department = repository.findByDepartmentName(name);
        if (department != null) {
            return department;
        }

        department = new Department();
        initBase(department);
        department.setParentId(parentId);
        department.setDepartmentName(name);
        department.setDescription(description);
        return repository.save(department);
    }

    private User getOrCreateUser(UserRepository repository,
                                 String email,
                                 String name,
                                 UserRole role,
                                 Department department) {
        User user = repository.findByEmail(email);
        if (user != null) {
            return user;
        }

        user = new User();
        initBase(user);
        user.setDepartmentId(department);
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode("123456"));
        user.setPhone("0900000000");
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        return repository.save(user);
    }

    private PipelineStage getOrCreateStage(PipelineStageRepository repository,
                                           String name,
                                           Integer order) {
        PipelineStage stage = repository.findByStageName(name);
        if (stage != null) {
            return stage;
        }

        stage = new PipelineStage();
        initBase(stage);
        stage.setStageName(name);
        stage.setStageOrder(order);
        return repository.save(stage);
    }

    private Job getOrCreateJob(JobRepository repository,
                               String title,
                               String description,
                               String location,
                               BigDecimal salaryMin,
                               BigDecimal salaryMax,
                               JobStatus status,
                               Department department,
                               User recruiter) {
        Job job = repository.findByTitle(title);
        if (job != null) {
            return job;
        }

        job = new Job();
        initBase(job);
        job.setDepartmentId(department);
        job.setRecruiterId(recruiter);
        job.setTitle(title);
        job.setDescription(description);
        job.setLocation(location);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setStatus(status);
        return repository.save(job);
    }

    private Candidate getOrCreateCandidate(CandidateRepository repository,
                                           String email,
                                           String name,
                                           String phone) {
        Candidate candidate = repository.findByEmail(email);
        if (candidate != null) {
            return candidate;
        }

        candidate = new Candidate();
        initBase(candidate);
        candidate.setEmail(email);
        candidate.setName(name);
        candidate.setPhone(phone);
        return repository.save(candidate);
    }

    private Cv getOrCreateCv(CvRepository repository,
                             Candidate candidate,
                             String filePath,
                             String fileType) {
        Cv cv = repository.findByFilePath(filePath);
        if (cv != null) {
            return cv;
        }

        cv = new Cv();
        initBase(cv);
        cv.setCandidateId(candidate);
        cv.setFilePath(filePath);
        cv.setFileType(fileType);
        return repository.save(cv);
    }

    private Application getOrCreateApplication(ApplicationRepository repository,
                                               Job job,
                                               Candidate candidate,
                                               Cv cv,
                                               PipelineStage stage) {
        Application application = repository.findByCandidateId_IdAndJobId_Id(candidate.getId(), job.getId());
        if (application != null) {
            return application;
        }

        application = new Application();
        initBase(application);
        application.setJobId(job);
        application.setCandidateId(candidate);
        application.setCvId(cv);
        application.setPipelineStageId(stage);
        return repository.save(application);
    }

    private StageTransition getOrCreateTransition(StageTransitionRepository repository,
                                                  Application application,
                                                  PipelineStage fromStage,
                                                  PipelineStage toStage,
                                                  String notes) {
        if (repository.existsByApplicationId_IdAndToStageId_Id(application.getId(), toStage.getId())) {
            return null;
        }

        StageTransition transition = new StageTransition();
        initBase(transition);
        transition.setApplicationId(application);
        transition.setFromStageId(fromStage);
        transition.setToStageId(toStage);
        transition.setMovedAt(LocalDateTime.now());
        transition.setNotes(notes);
        return repository.save(transition);
    }

    private Interview getOrCreateInterview(InterviewRepository repository,
                                           Application application,
                                           User interviewer,
                                           LocalDateTime scheduledAt,
                                           String meetingLink,
                                           InterviewStatus status,
                                           Integer durationMinutes,
                                           String feedback,
                                           InterviewResult result) {
        Interview existingInterview = repository.findByApplicationId_Id(application.getId()).orElse(null);
        if (existingInterview != null) {
            if (existingInterview.getInterviewerId() == null) {
                existingInterview.setInterviewerId(interviewer);
                existingInterview.setUpdatedAt(LocalDateTime.now());
                return repository.save(existingInterview);
            }
            return existingInterview;
        }

        Interview interview = new Interview();
        initBase(interview);
        interview.setApplicationId(application);
        interview.setInterviewerId(interviewer);
        interview.setScheduledAt(scheduledAt);
        interview.setMeetingLink(meetingLink);
        interview.setStatus(status);
        interview.setDurationMinutes(durationMinutes);
        interview.setFeedBack(feedback);
        interview.setResult(result);
        return repository.save(interview);
    }

    private void initBase(BaseEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setIsDeleted(false);
    }
}
