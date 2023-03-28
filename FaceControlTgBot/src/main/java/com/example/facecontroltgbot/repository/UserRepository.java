package com.example.facecontroltgbot.repository;

import com.example.facecontroltgbot.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByChatId(long chatId);

    List<User> findAllByStatus(String status);

    List<User> findAllByTarix(String tarix);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.firstname = :firstname where u.chatId = :chatId")
    void updateFirstName(@Param("chatId") long chatId, @Param("firstname") String firstname);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.age = :age where u.chatId = :chatId")
    void updateAge(@Param("chatId") long chatId, @Param("age") int age);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.tarix = :tarix where u.chatId = :chatId")
    void updateTarix(@Param("chatId") long chatId, @Param("tarix") String tarix);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.time = :time where u.chatId = :chatId")
    void updateTime(@Param("chatId") long chatId, @Param("time") String time);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.phone = :phone where u.chatId = :chatId")
    void updatePhone(@Param("chatId") long chatId, @Param("phone") String phone);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.photo = :photo where u.chatId = :chatId")
    void updatePhoto(@Param("chatId") long chatId, @Param("photo") byte[] photo);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = "update User u set u.status = :status where u.chatId = :chatId")
    void updateStatus(@Param("chatId") long chatId, @Param("status") String status);

}
