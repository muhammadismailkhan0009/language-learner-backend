package com.myriadcode.languagelearner.common.dtos;

public record ApiRequest<T, U>(T payload, U additionalData) {
}
