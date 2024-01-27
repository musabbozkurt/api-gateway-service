package com.mb.swaggerapplication.utils;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.format.AmountFormatParams;
import org.javamoney.moneta.format.CurrencyStyle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.money.format.AmountFormatQueryBuilder;
import javax.money.format.MonetaryAmountFormat;
import javax.money.format.MonetaryFormats;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UtilsTest {

    @Test
    public void testEntityIdUtils() {
        StandardPBEStringEncryptor standardPBEStringEncryptor = new StandardPBEStringEncryptor();
        standardPBEStringEncryptor.setPassword("Password");

        String helloWorld = "10000";
        String encodeToUrlSafeString = Base64.getUrlEncoder().encodeToString(standardPBEStringEncryptor.encrypt(helloWorld).getBytes());

        log.info("encodeToUrlSafeString : {}", encodeToUrlSafeString);
        Long valueOf = Long.valueOf(standardPBEStringEncryptor.decrypt(new String(Base64.getUrlDecoder().decode(encodeToUrlSafeString))));

        log.info("valueOf : {}", valueOf);
    }

    @Test
    public void testMonetaryAmountFormat() {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        MonetaryAmountFormat moneyFormat = MonetaryFormats.getAmountFormat(
                AmountFormatQueryBuilder.of(Locale.of("tr", "TR"))
                        .set(CurrencyStyle.SYMBOL)
                        .set(AmountFormatParams.PATTERN, "###,###.##¤")
                        .build());

        OffsetDateTime createdDateTime = OffsetDateTime.now();

        // @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentZoneOffsetAsString")
        ZoneOffset zoneOffset = ZoneOffset.UTC;

        String format = createdDateTime.atZoneSameInstant(zoneOffset).format(dateTimeFormat);
        log.info("format : {}", format);

        String aTry = moneyFormat.format(Money.of(100, "TRY"));
        log.info("aTry : {}", aTry);
    }

} 
