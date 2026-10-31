package com.mb.modulith.email.batch;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.service.EmailResourceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DataSetResponseItemProcessor implements ItemProcessor<DataSetResponseDto, List<Email>> {

    private final EmailResourceService emailResourceService;

    @Override
    public List<Email> process(@NonNull DataSetResponseDto dataSetResponseDto) {
        return emailResourceService.checkEmails(dataSetResponseDto);
    }
}
