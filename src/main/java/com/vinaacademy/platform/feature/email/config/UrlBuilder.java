package com.vinaacademy.platform.feature.email.config;

import com.vinaacademy.platform.feature.email.enums.UrlPath;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@Getter
public class UrlBuilder {
    @Value("${application.url.frontend:http://localhost:3000}")
    private String frontendUrl;

    @Value("${application.hmac.secret:super-secret-key-for-hmac}")
    private String HMAC_SECRET;

    public String buildActionUrl(UrlPath action, String token) {
        String rawUrl = frontendUrl + action.getPath() + "?token=" + token;
        String signature = generateSignature(token);

        return rawUrl + "&signature=" + signature;
    }

    private String generateSignature(String data) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(HMAC_SECRET.getBytes(), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacData = mac.doFinal(data.getBytes());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacData);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate HMAC signature", e);
        }
    }

    public boolean isSignatureValid(String token, String signature) {
        String calculatedSignature = generateSignature(token);
        return calculatedSignature.equals(signature);
    }
}
