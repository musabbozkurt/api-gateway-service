package com.mb.modulith.email.mapping;

import com.mb.modulith.email.api.dto.request.ApiEmailRequest;
import com.mb.modulith.email.api.dto.response.ApiEmailResponse;
import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.data.model.Email;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmailMapper {
    Email map(ApiEmailRequest apiEmailRequest);

    List<ApiEmailResponse> map(List<Email> emails);

    ApiEmailResponse map(Email email);

    DataSetResponseDto map(DataSetResponseDto dataSetResponseDto);

}
