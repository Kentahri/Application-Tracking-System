package ats.service;

import java.util.List;

import ats.entity.Job;

public interface QdrantService {

    void upsertJob(Job job, List<Float> vector);

    void deleteJob(Long jobId);

    List<Long> searchJobIds(List<Float> vector, int limit);
}