package com.ing.fmjavaguild.web.config;

import com.google.common.collect.ImmutableMap;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Slf4j
public class JobMasterConfig {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final JobExplorer jobExplorer;
    private final ApplicationContext applicationContext;
    private final JdbcTemplate jdbcTemplate;

    private static final int GRID_SIZE = 2;
    private static final String MASTER_STEP_NAME = "exportGenerationMasterStep";
    private static final String MASTER_JOB_NAME  = "exportGenerationMasterJob";
    private static final String REMOTE_STEP_NAME = "exportGenerationWorkerStep";

    public static final String SQL_COUNT_DUMMY_DATA = "" +
            "select count(1) " +
            "from javafm_sb_securities s " +
            "join javafm_sb_fundamentals f " +
            "on s.ticker_symbol = f.ticker_symbol";

    public JobMasterConfig(StepBuilderFactory stepBuilderFactory,
                           JobBuilderFactory jobBuilderFactory,
                           JobExplorer jobExplorer,
                           ApplicationContext applicationContext,
                           JdbcTemplate jdbcTemplate) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.jobExplorer = jobExplorer;
        this.applicationContext = applicationContext;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public PartitionHandler partitionHandler(MessagingTemplate messagingTemplate) throws Exception {
        MessageChannelPartitionHandler partitionHandler = new MessageChannelPartitionHandler();

        partitionHandler.setStepName(REMOTE_STEP_NAME);                      // bean id in the slave jvm
        partitionHandler.setGridSize(GRID_SIZE);                             // data size divided by cluster size approximation
        partitionHandler.setMessagingOperations(messagingTemplate);
//        partitionHandler.setPollInterval(5000l);                             // how to know slave is done
        partitionHandler.setJobExplorer(jobExplorer);

        partitionHandler.afterPropertiesSet();

        return partitionHandler;
    }

    @Bean
    public Partitioner remoteArrayPartitioner() {
        return gridParitionsCount -> {
            Long securitiesCount = this.jdbcTemplate.queryForObject(SQL_COUNT_DUMMY_DATA, Long.class);
            Long partitionSize = securitiesCount / gridParitionsCount;

            return IntStream.range(0, gridParitionsCount)
                    .mapToObj(partitionIndex -> Tuple.of(partitionIndex,
                                                         partitionIndex * partitionSize,
                                                         Math.min(partitionIndex * partitionSize + partitionSize, securitiesCount)))
                    .collect(Collectors.toMap(
                                tuple -> "partition" + tuple._1.toString(),
                                tuple -> new ExecutionContext(ImmutableMap.of("minValue", tuple._2, "maxValue", tuple._3))));
        };
    }

    @Bean
    public Step exportGenerationMasterStep() throws Exception {
        return stepBuilderFactory.get(MASTER_STEP_NAME)
                .partitioner(REMOTE_STEP_NAME, remoteArrayPartitioner())
                .partitionHandler(partitionHandler(null))
                .gridSize(GRID_SIZE)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Job remoteMasterParitioningJob() throws Exception {
        return jobBuilderFactory.get(MASTER_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(exportGenerationMasterStep())
                .build();
    }
}
