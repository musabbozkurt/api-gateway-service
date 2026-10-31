package com.mb.modulith.email.batch;

import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.data.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DataSetResponseDtoItemWriter implements ItemWriter<List<Email>> {

    private final EmailRepository emailRepository;

    @Override
    public void write(Chunk<? extends List<Email>> chunk) {
        chunk.forEach(emailRepository::saveAll);
        log.info("Writing email batch chunk size: {}", chunk.size());
    }
}
