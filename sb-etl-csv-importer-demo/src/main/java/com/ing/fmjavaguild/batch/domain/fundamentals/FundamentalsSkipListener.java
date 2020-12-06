package com.ing.fmjavaguild.batch.domain.fundamentals;

import com.ing.fmjavaguild.batch.infrastructure.emails.SimpleEmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;

@Slf4j
@Component
public class FundamentalsSkipListener implements SkipListener<Fundamentals, FundamentalsEntity> {

    private final SimpleEmailService simpleEmailService;

    @Autowired
    public FundamentalsSkipListener(SimpleEmailService simpleEmailService) {
        this.simpleEmailService = simpleEmailService;
    }

    @Override
    public void onSkipInRead(Throwable throwable) {
        simpleEmailService.sendSimpleMessage("[skipped reading] FM Java - Spring Batch demo", throwable.getMessage());
    }

    @Override
    public void onSkipInWrite(FundamentalsEntity fundamentalsEntity, Throwable throwable) {
        log.error("Skipping write of {} due to {}", fundamentalsEntity, throwable);
        simpleEmailService.sendSimpleMessage("[skipped write] FM Java - Spring Batch demo",
                format("Having fun with %s while processing %s.", throwable.getMessage(), fundamentalsEntity.toString()));
    }

    @Override
    public void onSkipInProcess(Fundamentals fundamentals, Throwable throwable) {
        log.error("Skipp processing of {} due to {}.", fundamentals, throwable);
        simpleEmailService.sendSimpleMessage("[skipped processing] FM Java - Spring Batch demo",
                format("Having fun with %s while processing %s.", throwable.getMessage(), fundamentals.toString()));
    }
}
