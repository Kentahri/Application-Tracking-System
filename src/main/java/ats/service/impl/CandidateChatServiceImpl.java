package ats.service.impl;

import ats.constant.JobStatus;
import ats.dto.chat.CandidateChatRequest;
import ats.dto.chat.CandidateChatResponse;
import ats.dto.chat.JobSuggestionResponse;
import ats.entity.Candidate;
import ats.entity.Cv;
import ats.entity.Job;
import ats.exception.BadRequestException;
import ats.exception.NotFoundException;
import ats.repository.CandidateRepository;
import ats.repository.JobRepository;
import ats.service.CandidateChatService;
import ats.service.CandidateCvAccessService;
import ats.service.EmbeddingService;
import ats.service.GeminiChatService;
import ats.service.QdrantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CandidateChatServiceImpl implements CandidateChatService {

    private static final int CV_PROMPT_LIMIT = 12000;
    private static final int JOB_SEARCH_LIMIT = 5;
    private static final int QDRANT_SEARCH_LIMIT = 15;

    private final CandidateCvAccessService candidateCvAccessService;
    private final EmbeddingService embeddingService;
    private final QdrantService qdrantService;
    private final GeminiChatService geminiChatService;
    private final JobRepository jobRepository;
    private final CandidateRepository candidateRepository;

    @Override
    public CandidateChatResponse chat(CandidateChatRequest request, Principal principal) {
        Candidate candidate = candidateCvAccessService.getCurrentCandidate(principal);
        Long cvId = normalizeId(request.cvId());
        Long jobId = normalizeId(request.jobId());
        String message = request.message().trim();

        if (cvId != null && jobId != null) {
            Cv cv = candidateCvAccessService.getOwnedCv(cvId, principal);
            String cvText = requireParsedText(cv);
            Job job = getPublishedJobOrThrow(jobId);
            consumeQueryQuota(candidate);
            String answer = geminiChatService.generate(buildCvAdvicePrompt(message, cvText, job));
            return new CandidateChatResponse(answer, List.of(JobSuggestionResponse.from(job)));
        }

        if (cvId != null) {
            Cv cv = candidateCvAccessService.getOwnedCv(cvId, principal);
            String cvText = requireParsedText(cv);
            consumeQueryQuota(candidate);
            List<Job> jobs = findMatchingJobs(cvText);
            String answer = geminiChatService.generate(buildJobRecommendationPrompt(message, cvText, jobs));
            return new CandidateChatResponse(
                    answer,
                    jobs.stream().map(JobSuggestionResponse::from).toList()
            );
        }

        if (jobId != null) {
            Job job = getPublishedJobOrThrow(jobId);
            consumeQueryQuota(candidate);
            String answer = geminiChatService.generate(buildJobQuestionPrompt(message, job));
            return new CandidateChatResponse(answer, List.of(JobSuggestionResponse.from(job)));
        }

        consumeQueryQuota(candidate);
        List<Job> jobs = findMatchingJobs(message);
        if (!jobs.isEmpty()) {
            String answer = geminiChatService.generate(buildJobSearchPrompt(message, jobs));
            return new CandidateChatResponse(
                    answer,
                    jobs.stream().map(JobSuggestionResponse::from).toList()
            );
        }

        String answer = geminiChatService.generate(buildGeneralPrompt(message));
        return new CandidateChatResponse(answer, List.of());
    }

    private Long normalizeId(Long id) {
        return id != null && id > 0 ? id : null;
    }

    private void consumeQueryQuota(Candidate candidate) {
        int updatedRows = candidateRepository.consumeQueryQuota(candidate.getId(), LocalDateTime.now());
        if (updatedRows == 0) {
            log.warn("Candidate id: {} has no remaining chat quota", candidate.getId());
            throw new BadRequestException("You have no remaining chat quota.");
        }
    }

    private String requireParsedText(Cv cv) {
        String parsedText = cv.getParsedText();
        if (parsedText == null || parsedText.isBlank()) {
            throw new BadRequestException("CV has no parsed text. Please upload the CV again.");
        }
        return parsedText;
    }

    private Job getPublishedJobOrThrow(Long jobId) {
        return jobRepository.findById(jobId)
                .filter(job -> JobStatus.PUBLISHED.equals(job.getStatus()))
                .orElseThrow(() -> {
                    log.warn("Published job not found for candidate chat, job id: {}", jobId);
                    return new NotFoundException("Job not found");
                });
    }

    private List<Job> findMatchingJobs(String cvText) {
        List<Float> vector = embeddingService.embed(cvText);
        List<Long> jobIds = qdrantService.searchJobIds(vector, QDRANT_SEARCH_LIMIT);
        if (jobIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> rankByJobId = new java.util.HashMap<>();
        for (int index = 0; index < jobIds.size(); index++) {
            rankByJobId.put(jobIds.get(index), index);
        }

        Map<Long, Job> jobsById = jobRepository.findAllById(jobIds)
                .stream()
                .filter(job -> JobStatus.PUBLISHED.equals(job.getStatus()))
                .collect(Collectors.toMap(Job::getId, Function.identity()));

        return jobsById.values()
                .stream()
                .sorted(Comparator.comparingInt(job -> rankByJobId.getOrDefault(job.getId(), Integer.MAX_VALUE)))
                .limit(JOB_SEARCH_LIMIT)
                .toList();
    }

    private String buildJobRecommendationPrompt(String userMessage, String cvText, List<Job> jobs) {
        String jobContext = jobs.isEmpty()
                ? "No published matching jobs were found."
                : jobs.stream()
                .map(this::formatJob)
                .collect(Collectors.joining("\n\n"));

        return """
                You are an ATS career assistant for candidates.
                Answer in Vietnamese.
                Use only the CV and job context below. Do not invent jobs.
                If there are no matching jobs, say that clearly and suggest what profile details the candidate can improve.
                
                Candidate question:
                %s
                
                Candidate CV:
                %s
                
                Matching jobs:
                %s
                
                Response requirements:
                - Start with a direct answer.
                - Recommend up to 5 jobs.
                - For each job, explain briefly why it matches the CV.
                - Mention missing skills or experience gaps if useful.
                - Keep the answer practical and concise.
                """.formatted(
                userMessage,
                truncate(cvText, CV_PROMPT_LIMIT),
                jobContext
        );
    }

    private String buildCvAdvicePrompt(String userMessage, String cvText, Job job) {
        return """
                You are an ATS career assistant for candidates.
                Answer in Vietnamese.
                Use only the CV and job context below.
                
                Candidate question:
                %s
                
                Candidate CV:
                %s
                
                Target job:
                %s
                
                Response requirements:
                - Explain how well the CV fits the target job.
                - Suggest concrete CV edits: summary, skills, experience bullets, keywords.
                - Do not fabricate experience the candidate does not have.
                - Mark missing skills as learning or improvement suggestions.
                - Keep the answer practical and structured.
                """.formatted(
                userMessage,
                truncate(cvText, CV_PROMPT_LIMIT),
                formatJob(job)
        );
    }

    private String buildJobQuestionPrompt(String userMessage, Job job) {
        return """
                You are an ATS career assistant for candidates.
                Answer in Vietnamese.
                Use only the target job context below. Do not invent unavailable job details.
                
                Candidate question:
                %s
                
                Target job:
                %s
                
                Response requirements:
                - Answer the candidate's question directly.
                - Explain the job clearly using the provided context.
                - If the candidate asks about fit, tell them to provide a CV so the system can compare properly.
                - Keep the answer practical and concise.
                """.formatted(
                userMessage,
                formatJob(job)
        );
    }

    private String buildJobSearchPrompt(String userMessage, List<Job> jobs) {
        String jobContext = jobs.stream()
                .map(this::formatJob)
                .collect(Collectors.joining("\n\n"));

        return """
                You are an ATS career assistant for candidates.
                Answer in Vietnamese.
                The candidate did not provide a CV id or a job id, so these jobs were found by semantic search from the candidate's message.
                Use only the job context below. Do not invent unavailable jobs.
                
                Candidate question:
                %s
                
                Matching jobs:
                %s
                
                Response requirements:
                - Start with a direct answer.
                - List the matching jobs clearly.
                - Briefly explain why each job matches the question.
                - If the candidate asks about personal fit, say they should provide/select a CV for a more accurate match.
                - Keep the answer practical and concise.
                """.formatted(
                userMessage,
                jobContext
        );
    }

    private String buildGeneralPrompt(String userMessage) {
        return """
                You are an ATS career assistant for candidates.
                Answer in Vietnamese.
                The candidate has not provided a CV id or a job id in this request, and no matching published jobs were found by semantic search.
                
                Candidate question:
                %s
                
                Response requirements:
                - Answer general career, CV, and job-search questions helpfully.
                - If the candidate asks for matching jobs, explain that they should provide/select a CV.
                - If the candidate asks for CV advice for a specific job, explain that they should provide/select both a CV and a job.
                - Do not claim that you have inspected their CV or any job unless ids are provided.
                - Keep the answer concise.
                """.formatted(userMessage);
    }

    private String formatJob(Job job) {
        List<String> lines = new ArrayList<>();
        lines.add("Job ID: " + job.getId());
        lines.add("Title: " + valueOrEmpty(job.getTitle()));
        lines.add("Department: " + (job.getDepartmentId() != null
                ? valueOrEmpty(job.getDepartmentId().getDepartmentName())
                : ""));
        lines.add("Location: " + valueOrEmpty(job.getLocation()));
        lines.add("Salary: " + valueOrEmpty(job.getSalaryMin()) + " - " + valueOrEmpty(job.getSalaryMax()));
        lines.add("Deadline: " + valueOrEmpty(job.getDeadline()));
        lines.add("Description: " + valueOrEmpty(job.getDescription()));
        return String.join("\n", lines);
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "\n[Content truncated]";
    }

    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }
}
