package com.mb.modulith.email.service;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;

public interface FileIOService {

    DataSetResponseDto readDataSetResponseDtoFromFile();

    void writeDataSetResponseDtoToFile(String emailRequestJson);
}
