package com.mb.notificationservice.service.impl;

import com.mb.notificationservice.api.request.NotificationTemplateRequest;
import com.mb.notificationservice.api.response.NotificationTemplateResponse;
import com.mb.notificationservice.data.entity.NotificationTemplate;
import com.mb.notificationservice.data.repository.NotificationTemplateRepository;
import com.mb.notificationservice.enums.NotificationChannel;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.mapper.NotificationTemplateMapper;
import com.mb.notificationservice.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl implements NotificationTemplateService {

    private final NotificationTemplateRepository notificationTemplateRepository;
    private final NotificationTemplateMapper notificationTemplateMapper;

    @Override
    @Transactional
    public NotificationTemplateResponse create(NotificationTemplateRequest request) {
        if (notificationTemplateRepository.existsByCodeAndChannel(request.getCode(), request.getChannel())) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_CODE_EXISTS);
        }

        return notificationTemplateMapper.convert(notificationTemplateRepository.save(notificationTemplateMapper.convert(request)));
    }

    @Override
    @Transactional
    public NotificationTemplateResponse update(Long id, NotificationTemplateRequest request) {
        NotificationTemplate template = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));

        boolean codeChanged = !template.getCode().equals(request.getCode());
        boolean channelChanged = template.getChannel() != request.getChannel();

        if ((codeChanged || channelChanged) && notificationTemplateRepository.existsByCodeAndChannel(request.getCode(), request.getChannel())) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_CODE_EXISTS);
        }

        notificationTemplateMapper.update(template, request);
        NotificationTemplate updatedTemplate = notificationTemplateRepository.save(template);
        log.info("Notification template updated with id: {}", updatedTemplate.getId());
        return notificationTemplateMapper.convert(updatedTemplate);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getById(Long id) {
        NotificationTemplate template = notificationTemplateRepository.findById(id)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
        return notificationTemplateMapper.convert(template);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationTemplateResponse getByCodeAndChannel(String code, NotificationChannel channel) {
        NotificationTemplate template = notificationTemplateRepository.findByCodeAndChannel(code, channel)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND));
        return notificationTemplateMapper.convert(template);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationTemplateResponse> getAll(Pageable pageable) {
        return notificationTemplateRepository.findAll(pageable)
                .map(notificationTemplateMapper::convert);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!notificationTemplateRepository.existsById(id)) {
            throw new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND);
        }
        notificationTemplateRepository.deleteById(id);
        log.info("Notification template deleted with id: {}", id);
    }

    @Override
    public NotificationTemplate findActiveByCode(String code, NotificationChannel channel) {
        return notificationTemplateRepository.findByCodeAndChannelAndActiveTrue(code, channel)
                .orElseThrow(() -> new BaseException(NotificationErrorCode.NOTIFICATION_TEMPLATE_NOT_FOUND_OR_INACTIVE));
    }
}
