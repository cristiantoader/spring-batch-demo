package com.ing.fmjavaguild.worker;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.ModelMapper;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.oxm.xstream.XStreamMarshaller;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class JobWorkerConfig {
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 100;
    private static final String[] FIELD_EXTRACTOR_NAMES = {"id", "ticker", "security", "year", "eps", "yearGrossProfit", "yearTotalAssets", "yearTotalLiabilities", "yearMaxHigh", "yearMinLow"};

    /*
    select
       fundamentals.id                  as id,
       security.ticker_symbol           as ticker,
       security.security                as security,
       fundamentals.year                as year,
       fundamentals.earnings_per_share  as eps,
       fundamentals.gross_profit        as year_gross_profit,
       fundamentals.total_assets        as year_total_assets,
       fundamentals.total_liabilities   as year_total_liabilities,
       prices.avg_high                  as year_max_high,
       prices.avg_low                   as year_min_low

    from javafm_sb_securities security

    join (select *, year(f.period_ending) as year
          from javafm_sb_fundamentals f) fundamentals
        on fundamentals.ticker_symbol = security.ticker_symbol

    left join (select p.symbol,
                      year(p.date) as year,
                      max(p.high) as avg_high,
                      min(p.low) as avg_low
               from javafm_sb_prices p
               group by p.symbol, year(p.date)) prices
        on prices.symbol = security.ticker_symbol
        and prices.year = fundamentals.year

    order by fundamentals.id
     */

    @Autowired
    public JobWorkerConfig(StepBuilderFactory stepBuilderFactory, DataSource dataSource) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.dataSource = dataSource;
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Report> reportJdbcPagingItemReader(@Value("#{stepExecutionContext['minValue']}") Integer minValue,
                                                                   @Value("#{stepExecutionContext['maxValue']}") Integer maxValue) {

        JdbcPagingItemReader<Report> itemReader = new JdbcPagingItemReader<>();
        itemReader.setDataSource(dataSource);
        itemReader.setFetchSize(CHUNK_SIZE);
        itemReader.setRowMapper(new ReportRowMapper());

        MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();

        // could also have used a (materialized)view
        queryProvider.setSelectClause("" +
                "       fundamentals.id                  as id,\n" +
                "       security.ticker_symbol           as ticker,\n" +
                "       security.security                as security,\n" +
                "       fundamentals.year                as year,\n" +
                "       fundamentals.earnings_per_share  as eps,\n" +
                "       fundamentals.gross_profit        as year_gross_profit,\n" +
                "       fundamentals.total_assets        as year_total_assets,\n" +
                "       fundamentals.total_liabilities   as year_total_liabilities,\n" +
                "       prices.avg_high                  as year_max_high,\n" +
                "       prices.avg_low                   as year_min_low");


        queryProvider.setFromClause("" +
                "from javafm_sb_securities security\n" +
                "\n" +
                "join (select *, year(f.period_ending) as year\n" +
                "      from javafm_sb_fundamentals f) fundamentals\n" +
                "    on fundamentals.ticker_symbol = security.ticker_symbol\n" +
                "\n" +
                "left join (select p.symbol,\n" +
                "                  year(p.date) as year,\n" +
                "                  max(p.high) as avg_high,\n" +
                "                  min(p.low) as avg_low\n" +
                "           from javafm_sb_prices p\n" +
                "           group by p.symbol, year(p.date)) prices\n" +
                "    on prices.symbol = security.ticker_symbol\n" +
                "    and prices.year = fundamentals.year");

        queryProvider.setWhereClause("where fundamentals.id >= " + minValue + " and fundamentals.id <= " + maxValue);
        queryProvider.setSortKeys(ImmutableMap.of("fundamentals.id", Order.ASCENDING));

        itemReader.setQueryProvider(queryProvider);

        return itemReader;
    }

    @Bean
    public FlatFileItemWriter<Report> workerReportCsvItemWriter() throws IOException {
        String reportPath = File.createTempFile("report", ".csv").getAbsolutePath();
        log.info("Storing csv report to {}.", reportPath);

        BeanWrapperFieldExtractor<Report> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(FIELD_EXTRACTOR_NAMES);
        fieldExtractor.afterPropertiesSet();

        DelimitedLineAggregator<Report> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FlatFileItemWriter<Report> csvItemWriter = new FlatFileItemWriter<>();
        csvItemWriter.setResource(new FileSystemResource(reportPath));
        csvItemWriter.setLineAggregator(lineAggregator);

        csvItemWriter.setHeaderCallback(writer -> {
            String header = Arrays.stream(FIELD_EXTRACTOR_NAMES)
                                  .collect(Collectors.joining(","));
            writer.write(header);
        });

        return csvItemWriter;
    }

    @Bean
    public ItemProcessor<Report, Report> workerDataSanityReport(ModelMapper modelMapper) {
        return item -> {
            log.info("Processing report {}.", item);
            Report reportCopy = modelMapper.map(item, Report.class);
            reportCopy.setSecurity(item.getSecurity().replaceAll("[^a-zA-Z0-9]", ""));
            return reportCopy;
        };
    }

    @Bean
    public StaxEventItemWriter<Report> workerReportStaxEventItemWriter() throws Exception {
        String reportPath = File.createTempFile("report", ".xml").getAbsolutePath();
        log.info("Storing xml report to {}.", reportPath);

        XStreamMarshaller marshaller = new XStreamMarshaller();
        marshaller.setAliases(ImmutableMap.of("reportEntry", Report.class));

        StaxEventItemWriter<Report> itemWriter = new StaxEventItemWriter<>();
        itemWriter.setRootTagName("report");
        itemWriter.setMarshaller(marshaller);
        itemWriter.setResource(new FileSystemResource(reportPath));
        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    @Bean
    public CompositeItemWriter<Report> workerCompositeItemWriter() throws Exception {
        List<ItemWriter<? super Report>> writers = new ArrayList<>(2);
		writers.add(workerReportStaxEventItemWriter());
		writers.add(workerReportCsvItemWriter());

		CompositeItemWriter<Report> itemWriter = new CompositeItemWriter<>();
		itemWriter.setDelegates(writers);
		itemWriter.afterPropertiesSet();

		return itemWriter;
    }

    @Bean
    public Step exportGenerationWorkerStep() throws Exception {
        return stepBuilderFactory.get("exportGenerationWorkerStep")
                .<Report, Report>chunk(CHUNK_SIZE)
                .reader(reportJdbcPagingItemReader(null, null))
                .processor(workerDataSanityReport(null))
                .writer(workerCompositeItemWriter())
                .build();
    }
}
