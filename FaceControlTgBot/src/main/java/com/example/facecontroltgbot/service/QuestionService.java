package com.example.facecontroltgbot.service;

import com.example.facecontroltgbot.entity.Question;
import com.example.facecontroltgbot.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    public Question getQuestionByKeyAndLanguage(int key, String language) {
        return questionRepository.findByKeyAndLanguage(key, language);
    }

}
