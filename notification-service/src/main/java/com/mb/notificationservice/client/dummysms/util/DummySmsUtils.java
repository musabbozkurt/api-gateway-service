package com.mb.notificationservice.client.dummysms.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.client.dummysms.request.DummySmsOtpRequest;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.util.CryptoHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DummySmsUtils {

    public static String createAuthorization(DummySmsOtpRequest request,
                                             String apikey,
                                             String secretKey,
                                             String url,
                                             String dateSign) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(request, JsonNode.class);
        String hashMac256;
        String md5Hash;
        try {
            md5Hash = CryptoHelper.toMD5(node.toString());
            hashMac256 = CryptoHelper.toHashMac256(secretKey, dateSign + url + md5Hash);
        } catch (Exception e) {
            log.error("Exception occurred while creating authorization. Exception: {}", ExceptionUtils.getStackTrace(e));
            throw new BaseException(NotificationErrorCode.ALGORITHM_ERROR);
        }
        return apikey + ":" + hashMac256;
    }

    public static String getDateAsSignature() {
        return DateTimeFormatter.ofPattern("EEE, MMM dd yyyy HH:mm:ss 'GMT'").format(LocalDateTime.now());
    }
}
