package com.myriadcode.languagelearner.language_learning_system.reading_and_listening.infra.jpa.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "words_to_listen_to")
public class WordToListenToEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String word;

    @Column(name = "user_id", nullable = false)
    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
