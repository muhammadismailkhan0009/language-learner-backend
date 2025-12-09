package com.myriadcode.languagelearner.flashcards_study.application.endpoints.dtos;

public record ApiRequest<T, U>(T payload, U additionalData) {
}
