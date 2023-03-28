package com.example.facecontroltgbot.repository;

import com.example.facecontroltgbot.entity.QuestionHelper;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionHelperRepository extends JpaRepository<QuestionHelper, Long> {

    QuestionHelper findByChatId(long chatId);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update QuestionHelper qh set qh.language = :language where qh.chatId = :chatId")
    void updateLanguage(@Param("chatId") long chatId, @Param("language") String language);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update QuestionHelper qh set qh.key = :key where qh.chatId = :chatId")
    void updateKey(@Param("chatId") long chatId, @Param("key") int key);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update QuestionHelper qh set qh.key = 0 where qh.chatId = :chatId")
    void updateKeyToZero(@Param("chatId") long chatId);

}
