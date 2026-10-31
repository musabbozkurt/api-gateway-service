package com.mb.modulith.commerce.mapping;

import com.mb.modulith.commerce.api.model.request.ApiAddressRequest;
import com.mb.modulith.commerce.api.model.request.ApiUpdateAddressRequest;
import com.mb.modulith.commerce.api.model.response.ApiClientAddressResponse;
import com.mb.modulith.commerce.data.model.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomAddressMapper {

    Address map(ApiAddressRequest apiAddressRequest);

    Address map(ApiUpdateAddressRequest apiUpdateAddressRequest);

    List<ApiClientAddressResponse> map(List<Address> address);

    @Mapping(source = "client.id", target = "clientId")
    ApiClientAddressResponse map(Address address);
}
