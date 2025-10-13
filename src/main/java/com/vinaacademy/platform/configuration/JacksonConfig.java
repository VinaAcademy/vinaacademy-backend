package com.vinaacademy.platform.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 👇 Loại bỏ null field khỏi JSON
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 👇 Tắt timestamp cho date
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 👇 Xử lý các field không nhận dạng được
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // 👇 Tự động escape HTML (optional tùy dự án)
//        mapper.getFactory().configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, true);

        // 👇 DateTime formatter ISO hoặc custom
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalDateTime.class,
                new JsonSerializer<>() {
                    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    @Override
                    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws java.io.IOException {
                        gen.writeString(value.format(formatter));
                    }
                });

        // 👇 Optional: Serialize BigDecimal thành String để tránh mất độ chính xác JSON
        SimpleModule bigDecimalModule = new SimpleModule();
        bigDecimalModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);

        // 👇 Optional: Serialize Enum rõ ràng (theo name thay vì ordinal)
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        // Đăng ký các modules
        mapper.registerModule(timeModule);
        mapper.registerModule(bigDecimalModule);

        return mapper;
    }
}