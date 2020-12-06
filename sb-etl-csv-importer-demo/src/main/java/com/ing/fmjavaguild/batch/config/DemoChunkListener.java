package com.ing.fmjavaguild.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.AfterChunk;
import org.springframework.batch.core.annotation.AfterChunkError;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

@Slf4j
public class DemoChunkListener {

    @BeforeChunk
    public void beforeChunk(ChunkContext chunkContext) {
        log.info("Before chunk {}.", chunkContext.toString());

    }

    @AfterChunk
    public void afterChunk(ChunkContext chunkContext) {
        log.info("After chunk {}.", chunkContext.toString());
    }

    @AfterChunkError
    public void afterChunkError(ChunkContext chunkContext) {
        StepContext stepContext = chunkContext.getStepContext();
        log.error("Chunk {} from job {} and step {} had an error.", chunkContext.toString(), stepContext.getJobName(), stepContext.getStepName());
    }
}
