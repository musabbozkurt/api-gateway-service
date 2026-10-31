package com.mb.modulith.email.batch;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.service.FileIOService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.AfterRead;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.NonTransientResourceException;
import org.springframework.batch.infrastructure.item.ParseException;
import org.springframework.batch.infrastructure.item.UnexpectedInputException;

@Slf4j
@RequiredArgsConstructor
public class DataSetResponseItemReader implements ItemReader<DataSetResponseDto> {

    private final FileIOService fileIOService;
    private DataSetResponseDto dataSetResponseDto;

    @BeforeStep
    public void beforeStep() {
        dataSetResponseDto = fileIOService.readDataSetResponseDtoFromFile();
    }

    @AfterRead
    public void afterRead() {
        dataSetResponseDto = null;
    }

    @Override
    public DataSetResponseDto read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        log.info("dataSetResponseDto : {}", dataSetResponseDto);
        return dataSetResponseDto;
    }
}
