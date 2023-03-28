package com.example.facecontroltgbot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @Column(name = "chatId")
    private Long chatId;
    @Column(name = "firstname")
    private String firstname;
    @Column(name = "age")
    private int age;
    @Column(name = "tarix")
    private String tarix;
    @Column(name = "time")
    private String time;
    @Column(name = "phone")
    private String phone;
    @Lob
    @Column(name = "photo")
    private byte[] photo;
    @Column(name = "status")
    private String status;

    public User(Long chatId) {
        this.chatId = chatId;
    }

    @Override
    public String toString() {
        return "ℹ️  User = " + chatId +
                "\n\uD83E\uDD35  Name = " + firstname +
                "\n\uD83D\uDD1E  Age = " + age +
                "\n\uD83D\uDCC5  Date = " + tarix +
                "\n\uD83D\uDD52  Time = " + time +
                "\n☎  Phone number = " + phone;
    }

    public String toString2() {
        return "ℹ️  User = " + chatId +
                "\n\uD83E\uDD35  Name = " + firstname +
                "\n\uD83D\uDD1E  Age = " + age +
                "\n\uD83D\uDCC5  Date = " + tarix +
                "\n\uD83D\uDD52  Time = " + time +
                "\n☎  Phone number = " + phone +
                "\n▶️  Status = " + status;
    }

}
