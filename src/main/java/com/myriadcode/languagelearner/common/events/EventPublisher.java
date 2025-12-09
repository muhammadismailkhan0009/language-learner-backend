package com.myriadcode.languagelearner.common.events;

public interface EventPublisher {

    void publish(DomainEvent domainEvent);
}
