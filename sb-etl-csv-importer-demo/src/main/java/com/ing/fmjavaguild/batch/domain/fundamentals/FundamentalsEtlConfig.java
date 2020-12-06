package com.ing.fmjavaguild.batch.domain.fundamentals;

import com.google.common.collect.ImmutableMap;
import com.ing.fmjavaguild.batch.config.DemoChunkListener;
import com.ing.fmjavaguild.batch.config.DemoJobListener;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
@Slf4j
public class FundamentalsEtlConfig {

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final FundamentalsSkipListener fundamentalsSkipListener;
    private final DemoJobListener demoJobListener;

    private static final String[] DATASET_NAMES = {"id", "tickerSymbol", "periodEnding", "accountsPayable",
            "accountsReceivable", "addlIncomeExpenseItems", "afterTaxRoe", "capitalExpenditures", "capitalSurplus",
            "cashRatio", "cashAndCashEquivalents", "changeInInventories", "commonStocks", "costOfRevenue",
            "currentRatio", "deferredLiabilityCharges", "depreciation", "earningsBeforeInterestandTax",
            "earningsBeforeTax", "effectofExchangeRate", "equityEarningsLossUnconsolidatedSubsidiary", "fixedAssets",
            "goodwill", "grossMargin", "grossProfit", "incomeTax", "intangibleAssets", "interestExpense", "inventory",
            "investments", "liabilities", "longTermDebt", "longTermInvestments", "minorityInterest", "miscStocks",
            "netBorrowings", "netCashFlow", "netCashFlowOperating", "netCashFlowsFinancing", "netCashFlowsInvesting",
            "netIncome", "netIncomeAdjustments", "netIncomeApplicabletoCommonShareholders", "netIncomeContOperations",
            "netReceivables", "nonRecurringItems", "operatingIncome", "operatingMargin", "otherAssets",
            "otherCurrentAssets", "otherCurrentLiabilities", "otherEquity", "otherFinancingActivities",
            "otherInvestingActivities", "otherLiabilities", "otherOperatingActivities", "otherOperatingItems",
            "preTaxMargin", "preTaxROE", "profitMargin", "quickRatio", "researchandDevelopment", "retainedEarnings",
            "saleandPurchaseofStock", "sales", "generalandAdmin", "shortTermDebtCurrentPortionofLongTermDebt",
            "shortTermInvestments", "totalAssets", "totalCurrentAssets", "totalCurrentLiabilities", "totalEquity",
            "totalLiabilities", "totalLiabilitiesEquity", "totalRevenue", "treasuryStock", "forYear",
            "earningsPerShare", "estimatedSharesOutstanding"};

    private static final String FUNDAMENTALS_STEP_NAME = "fundamentalsCsvEtlStep";
    private static final String FUNDAMENTALS_JOB_NAME = "fundamentalCsvEtlJob";

    @Autowired
    public FundamentalsEtlConfig(StepBuilderFactory stepBuilderFactory,
                                 JobBuilderFactory jobBuilderFactory,
                                 FundamentalsSkipListener fundamentalsSkipListener,
                                 DemoJobListener demoJobListener) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.fundamentalsSkipListener = fundamentalsSkipListener;
        this.demoJobListener = demoJobListener;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Fundamentals> fundamentalFlatFileItemReader() {
        FlatFileItemReader<Fundamentals> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource("sb-etl-csv-importer-demo/data/20201206-fundamentals.csv"));

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // name of the 'columns' in the fieldset
        tokenizer.setNames(DATASET_NAMES);

        // breaks down parsing & mapping into 2 peaces
        DefaultLineMapper<Fundamentals> fundamentalLineMapper = new DefaultLineMapper<>();
        // makes String token from 1 line making the field set
        fundamentalLineMapper.setLineTokenizer(tokenizer);
        // maps tokenized line into an object
        fundamentalLineMapper.setFieldSetMapper(makeFieldSetMapper());
        fundamentalLineMapper.afterPropertiesSet();

        reader.setLineMapper(fundamentalLineMapper);
        return reader;
    }

    @Bean
    public ItemProcessor<Fundamentals, FundamentalsEntity> fundamentalItemProcessor(ModelMapper modelMapper) {
        return fundamentals -> {
            log.info("Processing fundamental {}.", fundamentals);
            FundamentalsEntity processedFundamental = modelMapper.map(fundamentals, FundamentalsEntity.class);
            processedFundamental.setId(null);
            return processedFundamental;
        };
    }

    @Bean
    public JpaItemWriter<FundamentalsEntity> fundamentalJdbcBatchItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<FundamentalsEntity> fundamentalJpaItemWriter = new JpaItemWriter<>();
        fundamentalJpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return fundamentalJpaItemWriter;
    }

    @Bean
    public Step fundamentalsCsvEtlStep() {
        return this.stepBuilderFactory.get(FUNDAMENTALS_STEP_NAME)
                .<Fundamentals, FundamentalsEntity>chunk(10)
                .reader(fundamentalFlatFileItemReader())
                .processor(fundamentalItemProcessor(null))
                .writer(fundamentalJdbcBatchItemWriter(null))
                .listener(new DemoChunkListener())
                .faultTolerant()
                .skip(ConstraintViolationException.class)
                .skipLimit(5)
                .listener(fundamentalsSkipListener)
                .build();
    }

    @Bean
    public Job fundamentalCsvEtlJob() {
        return this.jobBuilderFactory.get(FUNDAMENTALS_JOB_NAME)
//                .incrementer(new RunIdIncrementer())
                .start(fundamentalsCsvEtlStep())
                .listener(demoJobListener)
                .build();
    }

    private static BeanWrapperFieldSetMapper<Fundamentals> makeFieldSetMapper() {
        BeanWrapperFieldSetMapper<Fundamentals> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Fundamentals.class);
        fieldSetMapper.setCustomEditors(ImmutableMap.of(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true)));
        return fieldSetMapper;
    }

}
