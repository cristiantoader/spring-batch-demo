package com.ing.fmjavaguild.batch.domain.prices;

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
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.sql.DataSource;
import java.text.SimpleDateFormat;
import java.util.Arrays;

@Configuration
@Slf4j
public class PricesEtlConfig {

    private static final String PRICES_CSV_ETL_STEP = "pricesCsvEtlStep";
    private static final String[] DATASET_NAMES = {"date", "symbol", "open", "close", "low", "high", "volume"};

    private final StepBuilderFactory stepBuilderFactory;
    private final JobBuilderFactory jobBuilderFactory;
    private final DataSource dataSource;
    private final DemoJobListener demoJobListener;

    @Autowired
    public PricesEtlConfig(StepBuilderFactory stepBuilderFactory,
                           JobBuilderFactory jobBuilderFactory,
                           DataSource dataSource,
                           DemoJobListener demoJobListener) {

        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
        this.dataSource = dataSource;
        this.demoJobListener = demoJobListener;
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Price> pricesFlatFileItemReader() {
        FlatFileItemReader<Price> reader = new FlatFileItemReader<>();
        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource("sb-etl-csv-importer-demo/data/20201206-prices.csv"));
        reader.setSaveState(false);

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // name of the 'columns' in the fieldset
        tokenizer.setNames(DATASET_NAMES);

        // breaks down parsing & mapping into 2 peaces
        DefaultLineMapper<Price> fundamentalLineMapper = new DefaultLineMapper<>();
        // makes String token from 1 line making the field set
        fundamentalLineMapper.setLineTokenizer(tokenizer);
        // maps tokenized line into an object
        fundamentalLineMapper.setFieldSetMapper(new PriceFieldSetMapper());
        fundamentalLineMapper.afterPropertiesSet();

        reader.setLineMapper(fundamentalLineMapper);
        return reader;
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<PriceDto> jdbcBatchItemWriter() {
        JdbcBatchItemWriter<PriceDto> itemWriter = new JdbcBatchItemWriter<>();
        itemWriter.setDataSource(dataSource);
        itemWriter.setSql("insert into javafm_sb_prices(date, symbol, open, close, low, high, volume) values (:date, :symbol, :open, :close, :low, :high, :volume)");
        itemWriter.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>()); // matches getters
        itemWriter.afterPropertiesSet();
        return itemWriter;
    }

    @Bean
    @StepScope
    public ItemProcessor<Price, Price> priceDataNormalisationProcessor(ModelMapper modelMapper) {
        return item -> {
//            log.info("Thread {} pre-processing on item {}.", Thread.currentThread().getName(), item);
            Price price = modelMapper.map(item, Price.class);
            price.setDate(item.getDate().substring(0, 10));
            return price;
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Price, PriceDto> priceDtoConversionProcessor() {
        return item -> {
            log.info("Thread {} processing conversions on item {}.", Thread.currentThread().getName(), item);
            PriceDto priceDto = new PriceDto();
            priceDto.setSymbol(item.getSymbol());
            priceDto.setDate(new SimpleDateFormat("yyyy-MM-dd").parse(item.getDate()));
            priceDto.setClose(item.getClose());
            priceDto.setOpen(item.getOpen());
            priceDto.setLow(item.getLow());
            priceDto.setHigh(item.getHigh());
            return priceDto;
        };
    }

    @Bean
    public CompositeItemProcessor<Price, PriceDto> priceDtoCompositeItemProcessor() {
        CompositeItemProcessor<Price, PriceDto> compositeItemProcessor = new CompositeItemProcessor<>();
        compositeItemProcessor.setDelegates(Arrays.asList(priceDataNormalisationProcessor(null),
                                                          priceDtoConversionProcessor()));
        return compositeItemProcessor;
    }

    @Bean
    public Step pricesCsvEtlStep() {
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

        return this.stepBuilderFactory.get(PRICES_CSV_ETL_STEP)
                .<Price, PriceDto>chunk(10_000)
                .reader(pricesFlatFileItemReader())
                .processor(priceDtoCompositeItemProcessor())
                .writer(jdbcBatchItemWriter())
                .faultTolerant()
                .taskExecutor(taskExecutor)
                .listener(new DemoChunkListener())
                .build();
    }

    @Bean
    public Job pricesCsvEtlJob() {
        return this.jobBuilderFactory.get("pricesCsvEtlJob")
//                .incrementer(new RunIdIncrementer())
                .start(pricesCsvEtlStep())
                .listener(demoJobListener)
                .build();
    }
}
