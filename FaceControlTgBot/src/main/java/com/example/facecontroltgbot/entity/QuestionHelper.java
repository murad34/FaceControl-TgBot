package com.example.facecontroltgbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questionhelper")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionHelper {

    @Id
    @Column(name = "chatId")
    private Long chatId;
    @Column(name = "language")
    private String language;
    @Column(name = "key")
    private int key;

    public QuestionHelper(Long chatId, int key) {
        this.chatId = chatId;
        this.key = key;
    }
}
