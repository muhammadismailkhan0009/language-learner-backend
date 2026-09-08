package com.myriadcode.languagelearner.language_learning_system.domain.word_practice.exceptions;

public class InvalidWordPracticeBatchException extends RuntimeException {
    public InvalidWordPracticeBatchException(String reason) {
        super(reason);
    }
}
