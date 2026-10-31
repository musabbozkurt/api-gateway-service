package com.mb.modulith.email.service;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.data.model.Email;

import java.util.List;

public interface EmailResourceService {

    List<Email> checkEmails(DataSetResponseDto emailRequest);

    DataSetResponseDto generateDummyDataSetResponseDto();

    DataSetResponseDto generateDummyDataSetResponseDto1();

    DataSetResponseDto generateDummyDataSetResponseDto2();
}
