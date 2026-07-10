package ats.service;

import ats.entity.Job;

import java.util.List;

public interface QdrantService {

    void upsertJob(Job job, List<Float> vector);

    void deleteJob(Long jobId);

    List<Long> searchJobIds(List<Float> vector, int limit);
}