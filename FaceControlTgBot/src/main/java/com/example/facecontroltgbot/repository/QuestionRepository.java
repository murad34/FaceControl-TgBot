package com.example.facecontroltgbot.repository;

import com.example.facecontroltgbot.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    Question findByKeyAndLanguage(int key, String language);

}
