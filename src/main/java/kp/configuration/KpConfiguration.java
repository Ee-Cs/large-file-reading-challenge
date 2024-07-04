package kp.configuration;

import kp.models.Data;
import kp.services.KpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static kp.Constants.DATA_FILE;
import static kp.Constants.INSERT_SQL;
/**
 * The configuration.
 */
@Slf4j
@Configuration
public class KpConfiguration {
    /**
     * Creates the writer bean.
     *
     * @return the writer bean
     */
    @Bean
    public JdbcBatchItemWriter<Data> jdbcBatchItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Data>().sql(INSERT_SQL)
                .dataSource(dataSource).beanMapped().build();
    }

//    /**
//     * The {@link CommandLineRunner} bean.
//     *
//     * @return the {@link CommandLineRunner} bean
//     */
//    @Bean
//    public CommandLineRunner commandLineRunner(KpService kpService) {
//        return _ -> kpService.process();
//    }
}
