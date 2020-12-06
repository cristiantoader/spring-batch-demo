package com.ing.fmjavaguild.batch.domain.securities;

import com.google.common.collect.ImmutableMap;
import com.ing.fmjavaguild.batch.config.DemoJobListener;
import org.modelmapper.ModelMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.String.format;

@Configuration
public class SecuritiesEtlConfig {

    private static final String[] DATASET_COLUMNS = {"tickerSymbol", "security", "secFilings", "GICS Sector",
            "gicsSubIndustry", "headquartersAddress", "dateFirstAdded", "cik"};

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;

    @Autowired
    public SecuritiesEtlConfig(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Security> securitiesFlatFileItemReader(@Value("#{jobParameters['eod']}") String eod) {
        FlatFileItemReader<Security> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(format("sb-etl-csv-importer-demo/data/%s-securities.csv", eod)));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // name of the 'columns' in the fieldset
        tokenizer.setNames(DATASET_COLUMNS);

        // breaks down parsing & mapping into 2 peaces
        DefaultLineMapper<Security> fundamentalLineMapper = new DefaultLineMapper<>();
        // makes String token from 1 line making the field set
        fundamentalLineMapper.setLineTokenizer(tokenizer);
        // maps tokenized line into an object
        fundamentalLineMapper.setFieldSetMapper(makeFieldSetMapper());
        fundamentalLineMapper.afterPropertiesSet();

        reader.setLineMapper(fundamentalLineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<Security, SecurityEntity> securityItemProcessor(ModelMapper modelMapper) {
        return item -> modelMapper.map(item, SecurityEntity.class);
    }

    @Bean
    public JpaItemWriter<SecurityEntity> securitiesJdbcBatchItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<SecurityEntity> fundamentalJpaItemWriter = new JpaItemWriter<>();
        fundamentalJpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return fundamentalJpaItemWriter;
    }

    @Bean
    public Step securitiesCsvEtlStep() {
        return stepBuilderFactory.get("securitiesCsvEtlStep")
                .<Security, SecurityEntity>chunk(100)
                .reader(securitiesFlatFileItemReader(null))
                .processor(securityItemProcessor(null))
                .writer(securitiesJdbcBatchItemWriter(null))
                .faultTolerant()
                .build();
    }

    @Bean
    public Job securitiesCsvEtlJob(DemoJobListener demoJobListener) {
        return jobBuilderFactory.get("securitiesCsvEtlJob")
                .start(securitiesCsvEtlStep())
                .listener(demoJobListener)
                .build();
    }

    private static BeanWrapperFieldSetMapper<Security> makeFieldSetMapper() {
        BeanWrapperFieldSetMapper<Security> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Security.class);
        fieldSetMapper.setCustomEditors(ImmutableMap.of(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true)));
        return fieldSetMapper;
    }
}
