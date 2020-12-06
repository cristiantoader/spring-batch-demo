package com.ing.fmjavaguild.batch.config;

import com.ing.fmjavaguild.batch.infrastructure.emails.SimpleEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DemoJobListener implements JobExecutionListener {

    private final SimpleEmailService simpleEmailService;

    @Autowired
    public DemoJobListener(SimpleEmailService simpleEmailService) {
        this.simpleEmailService = simpleEmailService;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        log.info("Before job " + jobName);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String jobStatus = jobExecution.getExitStatus().getExitCode();

        log.info("After job " + jobName);
        simpleEmailService.sendSimpleMessage(String.format("[%s] FM Java - Spring Batch %s", jobStatus, jobName),
                                             String.format("Job %s has finished with status %s.", jobName, jobStatus));

    }
}
