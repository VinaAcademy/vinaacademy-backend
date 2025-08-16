package com.vinaacademy.platform.feature.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * Service for handling internationalized messages.
 * Centralizes message resolution with locale support.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageSource messageSource;

    /**
     * Get message by key with current locale
     *
     * @param key Message key
     * @return Localized message
     */
    public String getMessage(String key) {
        return getMessage(key, null);
    }

    /**
     * Get message by key with arguments and current locale
     *
     * @param key  Message key
     * @param args Message arguments
     * @return Localized message
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(key, args, LocaleContextHolder.getLocale());
    }

    /**
     * Get message by key with arguments and specific locale
     *
     * @param key    Message key
     * @param args   Message arguments
     * @param locale Specific locale
     * @return Localized message
     */
    public String getMessage(String key, Object[] args, Locale locale) {
        try {
            return messageSource.getMessage(key, args, locale);
        } catch (Exception e) {
            log.warn("Failed to resolve message for key: {} with locale: {}", key, locale, e);
            return key; // Return key as fallback
        }
    }

    /**
     * Get message with default fallback if key not found
     *
     * @param key           Message key
     * @param args          Message arguments
     * @param defaultMessage Default message if key not found
     * @return Localized message or default
     */
    public String getMessage(String key, Object[] args, String defaultMessage) {
        return getMessage(key, args, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get message with default fallback and specific locale
     *
     * @param key           Message key
     * @param args          Message arguments
     * @param defaultMessage Default message if key not found
     * @param locale        Specific locale
     * @return Localized message or default
     */
    public String getMessage(String key, Object[] args, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(key, args, defaultMessage, locale);
        } catch (Exception e) {
            log.warn("Failed to resolve message for key: {} with locale: {}", key, locale, e);
            return defaultMessage != null ? defaultMessage : key;
        }
    }
}
