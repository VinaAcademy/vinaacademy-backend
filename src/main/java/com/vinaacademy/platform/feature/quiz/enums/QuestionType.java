package com.vinaacademy.platform.feature.quiz.enums;

public enum QuestionType {
    SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE, TEXT;

    public boolean isSingleChoice() {
        return this == SINGLE_CHOICE;
    }

    public boolean isMultipleChoice() {
        return this == MULTIPLE_CHOICE;
    }

    public boolean isTrueFalse() {
        return this == TRUE_FALSE;
    }

    public boolean isText() {
        return this == TEXT;
    }
}
