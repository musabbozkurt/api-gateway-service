package com.mb.modulith.email.service.impl;

import com.mb.modulith.email.client.emailresource.dto.DataSetResponseDto;
import com.mb.modulith.email.client.emailresource.dto.EmailDto;
import com.mb.modulith.email.client.emailresource.dto.ResourceDto;
import com.mb.modulith.email.data.DummyDataSetResponseDto;
import com.mb.modulith.email.data.model.Email;
import com.mb.modulith.email.data.repository.EmailRepository;
import com.mb.modulith.email.exception.EmailErrorCode;
import com.mb.modulith.email.service.EmailResourceService;
import com.mb.modulith.shared.exception.BaseException;
import com.mb.modulith.shared.util.EmailUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailResourceServiceImpl implements EmailResourceService {

    private static final List<String> emailDomains = Arrays.asList("mb.com", "mb.se");
    private final EmailRepository emailRepository;
    private final RestTemplate restTemplate;

    @Override
    public List<Email> checkEmails(DataSetResponseDto dataSetResponseDto) {
        try {
            List<Email> emails = emailRepository.findAll();

            List<EmailDto> dataSetResponseDtoEmails = dataSetResponseDto.getEmails();
            List<ResourceDto> dataSetResponseDtoResources = dataSetResponseDto.getResources();

            modifyDataSetResponseDtoResources(dataSetResponseDto, dataSetResponseDtoResources);

            retrieveAndPrepareDataSetResponseWithRecursion(dataSetResponseDtoEmails, dataSetResponseDtoResources);

            modifyDataSetResponseDtoEmails(dataSetResponseDto, dataSetResponseDtoEmails);

            combineDataSetResponseDtoEmails(dataSetResponseDtoEmails);

            return getFilteredEmailList(emails, dataSetResponseDtoEmails);
        } catch (Exception e) {
            throw new BaseException(EmailErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    // generate dummy data for external services
    public DataSetResponseDto generateDummyDataSetResponseDto() {
        return DummyDataSetResponseDto.generateDummyDataSetResponseDto();
    }

    @Override
    public DataSetResponseDto generateDummyDataSetResponseDto1() {
        return DummyDataSetResponseDto.generateDummyDataSetResponseDto1();
    }

    @Override
    public DataSetResponseDto generateDummyDataSetResponseDto2() {
        return DummyDataSetResponseDto.generateDummyDataSetResponseDto2();
    }

    // fetch data from external services recursively
    private void retrieveAndPrepareDataSetResponseWithRecursion(List<EmailDto> dataSetResponseDtoEmails, List<ResourceDto> dataSetResponseDtoResources) {
        List<ResourceDto> dataSetResponseDtoResources1 = new ArrayList<>();
        dataSetResponseDtoResources.forEach(resourceDto -> {
            resourceDto.getResourceUrls().forEach(url -> {
                DataSetResponseDto responseDto = getDataSetResponseDto(url);
                if (!responseDto.getResources().isEmpty()) {
                    dataSetResponseDtoEmails.addAll(responseDto.getEmails());
                    dataSetResponseDtoResources1.addAll(responseDto.getResources());
                    retrieveAndPrepareDataSetResponseWithRecursion(dataSetResponseDtoEmails, dataSetResponseDtoResources1);
                }
            });
        });
    }

    // map emails with count and add to new list
    private List<Email> getFilteredEmailList(List<Email> emails, List<EmailDto> dataSetResponseDtoEmails) {
        List<Email> emailList = new ArrayList<>();
        for (EmailDto emailDto : dataSetResponseDtoEmails) {
            emailDto.getEmails()
                    .stream()
                    .collect(Collectors.groupingBy(s -> s))
                    .forEach((emailAddress, v) -> {
                        if (!emailDomains.contains(EmailUtils.getEmailDomain(emailAddress)) || !EmailUtils.isValid(emailAddress)) {
                            emailList.add(getCheckedEmail(v, emails, emailAddress));
                        }
                        log.info(emailAddress + " " + v.size());
                    });
        }
        return emailList;
    }

    // combining resources lists into 1 list with distinct elements so it will be easy to fetch data
    private void modifyDataSetResponseDtoResources(DataSetResponseDto dataSetResponseDto, List<ResourceDto> dataSetResponseDtoResources) {
        if (!dataSetResponseDtoResources.isEmpty()) {
            for (int i = 1; i < dataSetResponseDtoResources.size(); i++) {
                ResourceDto resourceDto = dataSetResponseDtoResources.get(i);
                dataSetResponseDto.getResources().getFirst().getResourceUrls().addAll(resourceDto.getResourceUrls());
                dataSetResponseDtoResources.remove(resourceDto);
            }
            dataSetResponseDto.getResources().getFirst().setResourceUrls(dataSetResponseDto.getResources().getFirst().getResourceUrls().stream().distinct().toList());
        }
    }

    // combining emailDtos lists into 1 list so it will be easy to calculate count
    private void combineDataSetResponseDtoEmails(List<EmailDto> dataSetResponseDtoEmails) {
        if (dataSetResponseDtoEmails.size() > 1) {
            for (int i = 1; i < dataSetResponseDtoEmails.size(); i++) {
                EmailDto emailDto = dataSetResponseDtoEmails.get(i);
                dataSetResponseDtoEmails.getFirst().getEmails().addAll(emailDto.getEmails());
                dataSetResponseDtoEmails.remove(emailDto);
            }
        }
    }

    // combining emails lists into 1 list so it will be easy to calculate count
    private void modifyDataSetResponseDtoEmails(DataSetResponseDto dataSetResponseDto, List<EmailDto> dataSetResponseDtoEmails) {
        if (!dataSetResponseDtoEmails.isEmpty()) {
            for (int i = 1; i < dataSetResponseDtoEmails.size(); i++) {
                EmailDto emailDto = dataSetResponseDtoEmails.get(i);
                dataSetResponseDto.getEmails().getFirst().getEmails().addAll(emailDto.getEmails());
                dataSetResponseDtoEmails.remove(emailDto);
            }
        }
    }

    // checking is email already saved into the database or not
    private Email getCheckedEmail(List<String> occurrences, List<Email> emails, String emailAddress) {
        Optional<Email> optionalEmail = emails.stream().filter(email -> email.getEmailAddress().equals(emailAddress)).findAny();
        if (optionalEmail.isPresent()) {
            Email email = optionalEmail.get();
            return getEmail(occurrences, emailAddress, email);
        }
        return getEmail(occurrences, emailAddress, new Email());
    }

    // prepare email entity
    private Email getEmail(List<String> occurrences, String emailAddress, Email email) {
        email.setEmailAddress(emailAddress);
        email.setEmailCount(email.getEmailCount() + occurrences.size());
        email.setDeleted(true);
        email.setBlockedDomain(true);
        return email;
    }

    // Fetch data from external urls
    private DataSetResponseDto getDataSetResponseDto(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(url, HttpMethod.GET, entity, DataSetResponseDto.class).getBody();
        } catch (Exception e) {
            log.error("Error occured while getting DataSetResponseDto exception : {}", ExceptionUtils.getStackTrace(e));
        }
        return new DataSetResponseDto();
    }
}
