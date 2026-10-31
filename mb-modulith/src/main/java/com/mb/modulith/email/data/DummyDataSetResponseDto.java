package com.mb.modulith.email.data;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.client.emailresource.dto.EmailDto;
import com.mb.modulith.email.client.emailresource.dto.ResourceDto;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class DummyDataSetResponseDto {

    private static final String USERL_MB_COM = "userl@mb. com";
    private static final String USER_2_MB_COM = "user2@mb. com";
    private static final String USER_3_NOT_SO_COOL_COM = "user3@not-so-cool. com";

    public static DataSetResponseDto generateDummyDataSetResponseDto() {
        DataSetResponseDto dataSetResponseDto = new DataSetResponseDto();
        List<ResourceDto> resources = new ArrayList<>();
        ResourceDto resource = new ResourceDto();
        List<String> url = new ArrayList<>();
        url.add("http://localhost:28081/client/emails/data-set-1");
        url.add("http://localhost:28081/client/emails/data-set-2");
        resource.setResourceUrls(url);
        resources.add(resource);
        dataSetResponseDto.setResources(resources);
        List<EmailDto> emails = new ArrayList<>();
        EmailDto emailDto = new EmailDto();
        List<String> email = new ArrayList<>();
        email.add(USERL_MB_COM);
        email.add(USER_2_MB_COM);
        email.add(USER_2_MB_COM);
        email.add(USER_3_NOT_SO_COOL_COM);
        email.add(USER_3_NOT_SO_COOL_COM);
        emailDto.setEmails(email);
        emails.add(emailDto);
        dataSetResponseDto.setEmails(emails);
        return dataSetResponseDto;
    }

    public static DataSetResponseDto generateDummyDataSetResponseDto1() {
        DataSetResponseDto dataSetResponseDto = new DataSetResponseDto();
        List<ResourceDto> resources = new ArrayList<>();
        ResourceDto resource = new ResourceDto();
        List<String> url = new ArrayList<>();
        url.add("http://localhost:28081/client/emails/data-set-2");
        resource.setResourceUrls(url);
        resources.add(resource);
        dataSetResponseDto.setResources(resources);
        List<EmailDto> emails = new ArrayList<>();
        EmailDto emailDto = new EmailDto();
        List<String> email = new ArrayList<>();
        email.add(USERL_MB_COM);
        email.add(USER_3_NOT_SO_COOL_COM);
        email.add(USER_3_NOT_SO_COOL_COM);
        emailDto.setEmails(email);
        emails.add(emailDto);
        dataSetResponseDto.setEmails(emails);
        return dataSetResponseDto;
    }

    public static DataSetResponseDto generateDummyDataSetResponseDto2() {
        DataSetResponseDto dataSetResponseDto = new DataSetResponseDto();
        List<EmailDto> emails = new ArrayList<>();
        EmailDto emailDto = new EmailDto();
        List<String> email = new ArrayList<>();
        email.add(USERL_MB_COM);
        email.add(USER_2_MB_COM);
        emailDto.setEmails(email);
        emails.add(emailDto);
        dataSetResponseDto.setEmails(emails);
        return dataSetResponseDto;
    }

}
