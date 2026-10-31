package com.mb.modulith.email.batch;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.data.repository.EmailRepository;
import com.mb.modulith.email.service.EmailResourceService;
import com.mb.modulith.email.service.FileIOService;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@SuppressWarnings("removal")
public class SpringBatchExampleJobConfig {

    @Bean
    public ItemProcessor<DataSetResponseDto, List<Email>> itemProcessor(EmailResourceService emailResourceService) {
        return new DataSetResponseItemProcessor(emailResourceService);
    }

    @Bean
    public ItemWriter<List<Email>> itemWriter(EmailRepository emailRepository) {
        return new DataSetResponseDtoItemWriter(emailRepository);
    }

    @Bean
    public ItemReader<DataSetResponseDto> itemReader(FileIOService fileIOService) {
        return new DataSetResponseItemReader(fileIOService);
    }

    @Bean
    public Step exampleJobStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemProcessor<DataSetResponseDto, List<Email>> processor,
                               ItemWriter<List<Email>> writer,
                               ItemReader<DataSetResponseDto> reader) {
        return new StepBuilder("exampleJobStep", jobRepository)
                .<DataSetResponseDto, List<Email>>chunk(1, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job exampleJob(JobRepository jobRepository, Step exampleJobStep) {
        return new JobBuilder("exampleJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(exampleJobStep)
                .build();
    }
}
