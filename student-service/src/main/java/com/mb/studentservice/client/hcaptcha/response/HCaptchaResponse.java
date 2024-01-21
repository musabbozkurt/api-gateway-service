package com.mb.studentservice.client.hcaptcha.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class HCaptchaResponse {

    private Boolean success;

    @JsonProperty("challenge_ts")
    private OffsetDateTime challengeTs;

    private String hostname;
    private float score;

    @JsonProperty("score_reason")
    private List<String> scoreReason;

    @JsonProperty("sitekey")
    private String siteKey;

    @JsonProperty("behavior_counts")
    private String behaviorCounts;

    @JsonProperty("scoped_uid_0")
    private String scopedUid0;

    @JsonProperty("scoped_uid_1")
    private String scopedUid1;

    @JsonProperty("scoped_uid_2")
    private String scopedUid2;

    @JsonProperty("error-codes")
    private List<String> errorCodes;
}
