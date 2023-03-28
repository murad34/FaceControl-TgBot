package com.example.facecontroltgbot.service;

import com.example.facecontroltgbot.entity.User;
import com.example.facecontroltgbot.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User getUserByChatId(long chatId) {
        return userRepository.findByChatId(chatId);
    }

    public void saveUser(User User) {
        userRepository.save(User);
    }

    public void deleteUser(User User) {
        userRepository.delete(User);
    }

    public void setFirstName(long chatId, String firstName) {
        userRepository.updateFirstName(chatId, firstName);
    }

    public void setAge(long chatId, int age) {
        userRepository.updateAge(chatId, age);
    }

    public void setTarix(long chatId, String tarix) {
        userRepository.updateTarix(chatId, tarix);
    }

    public void setTime(long chatId, String time) {
        userRepository.updateTime(chatId, time);
    }

    public void setPhone(long chatId, String phone) {
        userRepository.updatePhone(chatId, phone);
    }

    public void setPhoto(long chatId, byte[] photo) {
        userRepository.updatePhoto(chatId, photo);
    }

    public void setStatus(long chatId, String status) {
        userRepository.updateStatus(chatId, status);
    }

    @Transactional
    public List<User> getAllUsersByStatus(String status) {
        return userRepository.findAllByStatus(status);
    }

    @Transactional
    public List<User> getAllUsersBySelectedDate(String tarix) {
        return userRepository.findAllByTarix(tarix);
    }

}
