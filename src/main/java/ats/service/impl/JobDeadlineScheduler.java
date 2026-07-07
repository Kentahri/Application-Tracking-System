package ats.service.impl;

import ats.constant.JobStatus;
import ats.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobDeadlineScheduler {

    private final JobRepository jobRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void closeExpiredPublishedJobs() {
        LocalDate today = LocalDate.now();
        int closedJobs = jobRepository.closeExpiredPublishedJobs(
                JobStatus.PUBLISHED,
                JobStatus.CLOSED,
                today,
                LocalDateTime.now()
        );

        if (closedJobs > 0) {
            log.info("closed {} expired published jobs before {}", closedJobs, today);
        }
    }
}
