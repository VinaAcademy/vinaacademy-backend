package com.vinaacademy.platform.feature.quiz.service.impl;

import com.vinaacademy.platform.feature.quiz.dto.UserAnswerRequest;
import com.vinaacademy.platform.feature.quiz.enums.QuestionType;
import com.vinaacademy.platform.feature.quiz.service.QuizCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizCacheServiceImpl implements QuizCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String QUIZ_ANSWERS_KEY = "quiz:answers:";
    private static final int CACHE_EXPIRY_HOURS = 24;


    @Override
    public void cacheUserAnswers(UUID userId, UUID quizId, List<UserAnswerRequest> answers,
                                 UUID sessionId) {
        String key = generateKey(userId, sessionId, quizId);

        Map<String, UserAnswerRequest> answersMap = new HashMap<>();
        for (UserAnswerRequest answer : answers) {
            answersMap.put(answer.getQuestionId().toString(), answer);
        }

        saveToCache(key, answersMap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, UserAnswerRequest> getCachedUserAnswers(UUID userId, UUID sessionId, UUID quizId) {
        try {
            String key = generateKey(userId, sessionId, quizId);
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof Map) {
                return (Map<String, UserAnswerRequest>) data;
            }
            return new HashMap<>();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    @Override
    public void updateCacheAnswer(UUID userId, UUID sessionId, UUID quizId, UserAnswerRequest request) {
        String key = generateKey(userId, sessionId, quizId);
        Map<String, UserAnswerRequest> cachedAnswer = getCachedUserAnswers(userId, sessionId, quizId);
        if (cachedAnswer == null) {
            cachedAnswer = new HashMap<>();
        }

        UserAnswerRequest existing = cachedAnswer.get(request.getQuestionId().toString());
        if (existing != null && existing.getSelectedAnswerIds() != null) {
            // unique ids
            Set<UUID> merged = Collections.synchronizedSet(new HashSet<>(existing.getSelectedAnswerIds()));

            QuestionType type = request.getQuestionType();
            if (type == QuestionType.SINGLE_CHOICE || type == QuestionType.TRUE_FALSE) {
                // for single choice
                merged = request.getSelectedAnswerIds().stream()
                        .limit(1)
                        .collect(Collectors.toCollection(HashSet::new));
            } else {
                merged.addAll(request.getSelectedAnswerIds());
            }

            request.setSelectedAnswerIds(new ArrayList<>(merged));
        }

        cachedAnswer.put(request.getQuestionId().toString(), request);
        saveToCache(key, cachedAnswer);
    }

    @Override
    public void clearCache(UUID userId, UUID sessionId, UUID quizId) {
        String key = generateKey(userId, sessionId, quizId);
        redisTemplate.delete(key);
    }

    private void saveToCache(String key, Map<String, UserAnswerRequest> cachedAnswer) {
        redisTemplate.opsForValue().set(key, cachedAnswer);
        redisTemplate.expire(key, CACHE_EXPIRY_HOURS, TimeUnit.HOURS);
    }


    private String generateKey(UUID userId, UUID sessionId, UUID quizId) {
        return String.format(QUIZ_ANSWERS_KEY + "%s:%s:%s",
                userId, sessionId, quizId);
    }
}
