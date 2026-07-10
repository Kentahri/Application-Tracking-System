package ats.service;

public interface JobVectorService {

    void upsert(Long jobId);

    void delete(Long jobId);
}
