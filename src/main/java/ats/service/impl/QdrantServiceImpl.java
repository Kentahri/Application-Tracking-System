package ats.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import ats.entity.Job;
import ats.service.QdrantService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QdrantServiceImpl implements QdrantService {

    @Value("${qdrant.url}")
    private String qdrantUrl;

    @Value("${qdrant.collection}")
    private String collection;

    private final RestClient restClient = RestClient.create();

    @Override
    public void upsertJob(Job job, List<Float> vector) {
        String url = qdrantUrl + "/collections/" + collection + "/points";

        Map<String, Object> payload = Map.of(
                "jobId", job.getId(),
                "title", valueOrEmpty(job.getTitle()),
                "location", valueOrEmpty(job.getLocation()),
                "note", buildNote(job),
                "status", job.getStatus() != null ? job.getStatus().name() : "",
                "department", job.getDepartmentId() != null
                        ? valueOrEmpty(job.getDepartmentId().getDepartmentName())
                        : ""
        );

        Map<String, Object> point = Map.of(
                "id", job.getId(),
                "vector", vector,
                "payload", payload
        );

        Map<String, Object> body = Map.of(
                "points", List.of(point)
        );

        restClient.put()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public void deleteJob(Long jobId) {
        String url = qdrantUrl + "/collections/" + collection + "/points/delete";

        Map<String, Object> body = Map.of(
                "points", List.of(jobId)
        );

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Override
    public List<Long> searchJobIds(List<Float> vector, int limit) {
        String url = qdrantUrl + "/collections/" + collection + "/points/search";

        Map<String, Object> body = Map.of(
                "vector", vector,
                "limit", limit,
                "with_payload", true
        );

        Map<?, ?> response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        List<?> result = (List<?>) response.get("result");

        return result.stream()
                .map(item -> (Map<?, ?>) item)
                .map(point -> ((Number) point.get("id")).longValue())
                .toList();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private String buildNote(Job job) {
        return """
                %s
                Salary: %s - %s
                Deadline: %s
                """.formatted(
                valueOrEmpty(job.getDescription()),
                job.getSalaryMin() != null ? job.getSalaryMin() : "",
                job.getSalaryMax() != null ? job.getSalaryMax() : "",
                job.getDeadline() != null ? job.getDeadline() : ""
        ).trim();
    }
}
