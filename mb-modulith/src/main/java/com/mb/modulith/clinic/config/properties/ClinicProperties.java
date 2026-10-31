package com.mb.modulith.clinic.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Getter
@Setter
@Configuration
@ConfigurationProperties("clinic")
public class ClinicProperties {

    private BigDecimal penaltyPercentage;
}
