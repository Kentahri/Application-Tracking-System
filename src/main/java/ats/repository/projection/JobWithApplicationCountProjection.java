package ats.repository.projection;

import ats.entity.Job;

public interface JobWithApplicationCountProjection {

    Job getJob();

    Long getApplicationCount();
}
