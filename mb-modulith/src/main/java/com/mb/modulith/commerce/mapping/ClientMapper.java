package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.request.ApiClientRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientResponse;
import com.mb.modulith.commerce.data.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientMapper {

    Client map(ApiClientRequest apiClientRequest);

    List<ApiClientResponse> map(List<Client> clients);

    ApiClientResponse map(Client client);

}
