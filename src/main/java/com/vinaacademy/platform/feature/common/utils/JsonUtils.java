package com.vinaacademy.platform.feature.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
@Log4j2
public class JsonUtils {
    public static String object2Json(ObjectMapper objectMapper, Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("Error parse JSON object");
            return null;
        }
    }

    public static <T> T json2Object(ObjectMapper objectMapper, String json, Class<T> clazz) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Error convert json to object");
            return null;
        }
    }
}
