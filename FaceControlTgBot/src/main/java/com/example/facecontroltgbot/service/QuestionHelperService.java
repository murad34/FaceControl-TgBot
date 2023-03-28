package com.example.facecontroltgbot.service;

import com.example.facecontroltgbot.entity.QuestionHelper;
import com.example.facecontroltgbot.repository.QuestionHelperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuestionHelperService {

    private final QuestionHelperRepository questionHelperRepository;

    public QuestionHelper getQuestionHelperByChatId(long chatId) {
        return questionHelperRepository.findByChatId(chatId);
    }

    public void saveQuestionHelper(QuestionHelper questionHelper) {
        questionHelperRepository.save(questionHelper);
    }

    public void setLanguageToChatId(long chatId, String language) {
        questionHelperRepository.updateLanguage(chatId, language);
    }

    public void updateKeyInChatId(long chatId) {
        questionHelperRepository.updateKey(chatId, getQuestionHelperByChatId(chatId).getKey()+1);
    }

    public void updateKeyToZero(long chatId) {
        questionHelperRepository.updateKeyToZero(chatId);
    }

    public void deleteQuestionHelper(QuestionHelper questionHelper) {
        questionHelperRepository.delete(questionHelper);
    }

}
