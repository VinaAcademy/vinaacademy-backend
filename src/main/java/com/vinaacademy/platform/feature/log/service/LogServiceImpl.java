package com.vinaacademy.platform.feature.log.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vinaacademy.platform.feature.common.utils.CommonUtils;
import com.vinaacademy.platform.feature.log.constant.LogConstants;
import com.vinaacademy.platform.feature.user.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogServiceImpl implements LogService {
    private final InternalLogService internalLogService;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;
    private final JwtService jwtService;

    @Value("${app.enableLog:true}")
    private boolean isLogEnabled;

    @Override
    public void log(String name, String event, Object oldData, Object newData) {
        doLog(name, event, null, oldData, newData);
    }

    @Async("logTaskExecutor")
    @Override
    public void log(String name, String event, String description, Object oldData, Object newData) {
        doLog(name, event, description, oldData, newData);
    }

    protected void doLog(String name, String event, String description, Object oldData, Object newData) {
        if (!isLogEnabled) {
            return;
        }
        String username = getUsername(event, newData);
        internalLogService.saveLog(name, event, username, description, oldData, newData);
    }

    private String getUsername(String event, Object newData) {
        if (LogConstants.LOGIN_ACTION.equalsIgnoreCase(event)) {
            Map<String, String> map = Optional.ofNullable(objectMapper
                    .convertValue(newData, new TypeReference<Map<String, String>>() {
                    })).orElse(new HashMap<>());
            return map.get("username");
        }
        return Optional.ofNullable(CommonUtils.getJwtFromRequest(request))
                .map(jwtService::extractUsername)
                .orElse(null);
    }
}
