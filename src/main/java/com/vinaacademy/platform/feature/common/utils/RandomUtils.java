package com.vinaacademy.platform.feature.common.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;


@UtilityClass
@Log4j2
public class RandomUtils {

    private static final RandomStringUtils randomStringUtils = RandomStringUtils.secure();

    public static String generateRandomString(int length) {
        return randomStringUtils.nextAlphabetic(length);
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateRandomNumber(int length) {
        return randomStringUtils.nextNumeric(length);
    }
}
