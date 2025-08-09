package com.mb.studentservice.service.impl.client.hcaptcha;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.mb.studentservice.client.hcaptcha.HCaptchaClient;
import com.mb.studentservice.client.hcaptcha.request.HCaptchaRequest;
import com.mb.studentservice.client.hcaptcha.response.HCaptchaResponse;
import com.mb.studentservice.config.hcaptcha.HCaptchaProperties;
import com.mb.studentservice.service.client.hcaptcha.HCaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Service
@RequiredArgsConstructor
public class HCaptchaServiceImpl implements HCaptchaService {

    private static final RequestBody REQUEST_BODY = RequestBody.create("", MediaType.parse("text/plain"));

    private final HCaptchaClient hCaptchaClient;
    private final HCaptchaProperties hCaptchaProperties;
    private final OkHttpClient okHttpClient;
    private final Gson gson;

    @Override
    public HCaptchaResponse validateRequestWithHttpClient(String captchaResponse) {
        try {
            HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
            String hCaptchaRequest = "response=" + captchaResponse + "&secret=" + hCaptchaProperties.getSecret() + "&sitekey=" + hCaptchaProperties.getSiteKey();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hCaptchaProperties.getUrl() + "/siteverify"))
                    .header("Content-Type", APPLICATION_FORM_URLENCODED_VALUE)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(hCaptchaRequest)).build();

            Gson customGson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                    .setPrettyPrinting()
                    .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, type, context) -> OffsetDateTime.parse(json.getAsString()))
                    .create();

            HCaptchaResponse hCaptchaResponse = customGson.fromJson(httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body(), HCaptchaResponse.class);
            log.info("hCaptchaResponse from validateRequestWithHttpClient : {} ", hCaptchaResponse);
            return hCaptchaResponse;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread was interrupted while validating request with HttpClient. Exception: {}", ExceptionUtils.getStackTrace(e));
        } catch (Exception e) {
            log.error("Exception occurred while validating request with HttpClient. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return new HCaptchaResponse();
    }

    @Override
    public HCaptchaResponse validateRequestWithMultiValueMap(String captchaResponse) {
        try {
            MultiValueMap<String, Object> hCaptchaRequest = new LinkedMultiValueMap<>();
            hCaptchaRequest.add("response", captchaResponse);
            hCaptchaRequest.add("secret", hCaptchaProperties.getSecret());
            hCaptchaRequest.add("sitekey", hCaptchaProperties.getSiteKey());
            HCaptchaResponse hCaptchaResponse = hCaptchaClient.validateRequest(hCaptchaRequest);
            log.info("hCaptchaResponse from validateRequestWithMultiValueMap : {} ", hCaptchaResponse);
            return hCaptchaResponse;
        } catch (Exception e) {
            log.error("Exception occurred while validating request with MultiValueMap. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return new HCaptchaResponse();
    }

    @Override
    public HCaptchaResponse validateRequest(String captchaResponse) {
        try {
            HCaptchaRequest captchaRequest = new HCaptchaRequest();
            captchaRequest.setResponse(captchaResponse);
            captchaRequest.setSecret(hCaptchaProperties.getSecret());
            captchaRequest.setSiteKey(hCaptchaProperties.getSiteKey());
            HCaptchaResponse hCaptchaResponse = hCaptchaClient.validateRequest(captchaRequest);
            log.info("hCaptchaResponse from validateRequest : {} ", hCaptchaResponse);
            return hCaptchaResponse;
        } catch (Exception e) {
            log.error("Exception occurred while validating request. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return new HCaptchaResponse();
    }

    @Override
    public boolean isHCaptchaResponseValid(String userResponseToken) {
        // https://github.com/hCaptcha/hcaptcha-android-sdk#usage
        if (StringUtils.isEmpty(userResponseToken))
            return true;

        HCaptchaResponse hCaptchaResponse = validateUserResponseToken(userResponseToken);
        return Boolean.TRUE.equals(hCaptchaResponse.getSuccess()) && (Objects.nonNull(hCaptchaResponse.getScore()) && hCaptchaResponse.getScore() < hCaptchaProperties.getRiskScoreThreshold());
    }

    private HCaptchaResponse validateUserResponseToken(String userResponseToken) {
        try {
            String captchaRequest = String.format("%s/siteverify?response=%s&secret=%s&sitekey=%s&remoteip=%s",
                    hCaptchaProperties.getUrl(), userResponseToken, hCaptchaProperties.getSecret(), hCaptchaProperties.getSiteKey(), "127.0.0.1");

            log.info("Received a request to validate user response token. HCaptchaRequest: {}", captchaRequest);

            Request request = new Request.Builder()
                    .url(captchaRequest)
                    .method("POST", REQUEST_BODY)
                    .build();

            Response response = okHttpClient.newCall(request).execute();

            HCaptchaResponse hCaptchaResponse = gson.fromJson(Objects.requireNonNull(response.body()).string(), HCaptchaResponse.class);

            log.info("Received a response from hCaptcha validate request. HCaptchaResponse: {}", hCaptchaResponse);

            return hCaptchaResponse;
        } catch (Exception e) {
            log.error("Exception occurred while validating HCaptcha request. Exception: {}", ExceptionUtils.getStackTrace(e));
        }
        return new HCaptchaResponse();
    }

}