package com.mb.notificationservice.client.dummysms.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mb.notificationservice.client.dummysms.request.DummySmsOtpRequest;
import com.mb.notificationservice.exception.BaseException;
import com.mb.notificationservice.exception.NotificationErrorCode;
import com.mb.notificationservice.util.CryptoHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class DummySmsUtilsTest {

    @Test
    void createAuthorization_ShouldReturnAuthorization_WhenValidInputs() {
        DummySmsOtpRequest request = new DummySmsOtpRequest("5554443322", "client", "testmessage");
        String apikey = "testApiKey";
        String secretKey = "testSecretKey";
        String url = "http://test.url";
        String dateSign = LocalDate.now().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.convertValue(request, JsonNode.class);

        String md5Hash = CryptoHelper.toMD5(node.toString());
        String hmac = CryptoHelper.toHashMac256(secretKey, dateSign + url + md5Hash);

        String authorization = DummySmsUtils.createAuthorization(request, apikey, secretKey, url, dateSign);

        String expectedAuthorization = apikey + ":" + hmac;
        assertEquals(expectedAuthorization, authorization);
    }

    @Test
    void createAuthorization_ShouldThrowBaseException_WhenCryptoHelperFails() {
        DummySmsOtpRequest request = new DummySmsOtpRequest("invalidData", null, null);

        BaseException exception = assertThrows(BaseException.class, () -> DummySmsUtils.createAuthorization(request, "", "", "", ""));

        assertEquals(NotificationErrorCode.ALGORITHM_ERROR, exception.getErrorCode());
    }

    @Test
    void getDateAsSignature_ShouldReturnFormattedDate() {
        LocalDateTime fixedDateTime = LocalDateTime.of(2023, 9, 26, 12, 0, 0);
        try (MockedStatic<LocalDateTime> mocked = mockStatic(LocalDateTime.class)) {
            mocked.when(LocalDateTime::now).thenReturn(fixedDateTime);

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy HH:mm:ss 'GMT'");
            String expectedSignature = dtf.format(fixedDateTime);

            String signature = DummySmsUtils.getDateAsSignature();

            assertEquals(expectedSignature, signature);
        }
    }
}
