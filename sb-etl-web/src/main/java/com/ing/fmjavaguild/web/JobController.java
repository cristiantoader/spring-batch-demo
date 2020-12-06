package com.ing.fmjavaguild.web;

import io.vavr.Tuple;
import io.vavr.control.Try;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/job")
public class JobController {

    private final JobOperator jobOperator;

    @Autowired
    public JobController(JobOperator jobOperator) {
        this.jobOperator = jobOperator;
    }

    @GetMapping
    public Set<String> findAllJobs() {
        return this.jobOperator.getJobNames();
    }

    @PostMapping
    public Long launchJob(@RequestBody JobCreateRequest jobCreateRequest) {
        Try<Long> createJobTry = Optional.ofNullable(jobCreateRequest)
                .map(it -> Tuple.of(it.getJobName(), makeSbParametersString(it.getProperties())))
                .map(it -> Try.of(() -> this.jobOperator.start(it._1, it._2)))
                .orElseThrow(() -> new IllegalArgumentException("Invalid job creare request"));

        return createJobTry.getOrElseThrow((ex) -> new IllegalStateException(ex));
    }

    @DeleteMapping("/{id}")
    public void deleteJob(@PathVariable("id") Long id) {
        // will change job status to stopping
        Try.of(() -> this.jobOperator.stop(id))
                         .getOrElseThrow(ex -> new IllegalStateException(ex));
    }

    private String makeSbParametersString(Map<String, ? super String> properties) {
        return properties.entrySet()
                         .stream()
                         .map(it -> String.format("%s=%s", it.getKey(), it.getValue()))
                         .collect(Collectors.joining(","));
    }
}
