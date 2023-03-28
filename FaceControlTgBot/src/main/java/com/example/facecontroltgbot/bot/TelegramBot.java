package com.example.facecontroltgbot.bot;

import com.example.facecontroltgbot.config.BotConfig;
import com.example.facecontroltgbot.entity.QuestionHelper;
import com.example.facecontroltgbot.entity.User;
import com.example.facecontroltgbot.service.QuestionHelperService;
import com.example.facecontroltgbot.service.QuestionService;
import com.example.facecontroltgbot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final UserService userService;

    private final QuestionService questionService;

    private final QuestionHelperService questionHelperService;

    private static final Pattern numberPattern = Pattern.compile("^[0-9]*$");

    private static final Pattern timePattern = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");

    private static final Pattern datePattern = Pattern.compile("^(0[1-9]|[1-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$");

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (message.equals("/start") && chatId == botConfig.getOwnerId()) {

                sendMessageToAdminPanel(chatId);

            } else if (message.equals("/start")) {

                if (questionHelperService.getQuestionHelperByChatId(chatId) != null) {
                    questionHelperService.deleteQuestionHelper(questionHelperService.getQuestionHelperByChatId(chatId));
                }

                if (userService.getUserByChatId(chatId) != null) {
                    userService.deleteUser(userService.getUserByChatId(chatId));
                }

                userService.saveUser(new User(chatId));

                questionHelperService.saveQuestionHelper(new QuestionHelper(chatId, 0));

                languageSelection(chatId);

            } else if (message.equals("/stop")) {

                if (chatId == botConfig.getOwnerId()) {
                    sendMessageToAdminPanel(chatId);
                    return;
                }

                if (questionHelperService.getQuestionHelperByChatId(chatId) != null) {
                    questionHelperService.deleteQuestionHelper(questionHelperService.getQuestionHelperByChatId(chatId));
                }

                if (userService.getUserByChatId(chatId) != null) {
                    userService.deleteUser(userService.getUserByChatId(chatId));
                }

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Yeniden bashlamag ucun  /start");
                executeMessage(sendMessage);

            } else if (message.equals("All requests") && chatId == botConfig.getOwnerId()) {

                if (!userService.getAllUsers().isEmpty()) {
                    sendAllUsersToAdminPanel(userService.getAllUsers());
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Empty");
                    executeMessage(sendMessage);
                }

            } else if (message.equals("Positive") && chatId == botConfig.getOwnerId()) {

                if (!userService.getAllUsersByStatus("+").isEmpty()) {
                    sendAllUsersToAdminPanel(userService.getAllUsersByStatus("+"));
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Empty");
                    executeMessage(sendMessage);
                }

            } else if (message.equals("Negative") && chatId == botConfig.getOwnerId()) {

                if (!userService.getAllUsersByStatus("-").isEmpty()) {
                    sendAllUsersToAdminPanel(userService.getAllUsersByStatus("-"));
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Empty");
                    executeMessage(sendMessage);
                }

            } else if (message.equals("Selected date") && chatId == botConfig.getOwnerId()) {

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("For selecting date enter : /date 29.03.2023");
                executeMessage(sendMessage);

            } else if (message.equals("Update") && chatId == botConfig.getOwnerId()) {

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("To update user enter : /update 862814184");
                executeMessage(sendMessage);

            } else if (message.contains("/date") && chatId == botConfig.getOwnerId()) {

                if (!userService.getAllUsersBySelectedDate(message.substring(6)).isEmpty()) {
                    sendAllUsersToAdminPanel(userService.getAllUsersBySelectedDate(message.substring(6)));
                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Empty");
                    executeMessage(sendMessage);
                }

            } else if (message.contains("/update") && chatId == botConfig.getOwnerId()) {

                if (userService.getUserByChatId(Long.parseLong(message.substring(8))) != null) {

//                    if (userService.getUserByChatId(Long.parseLong(message.substring(8))).getStatus() == "-") {
//                        userService.setStatus(Long.parseLong(message.substring(8)), "+");
//                    } else if (userService.getUserByChatId(Long.parseLong(message.substring(8))).getStatus() == "+") {
//                        userService.setStatus(Long.parseLong(message.substring(8)), "-");
//                    }

                    userService.setStatus(Long.parseLong(message.substring(8)), null);

                    sendUserToAdminPanel(userService.getUserByChatId(Long.parseLong(message.substring(8))));

                } else {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("Empty");
                    executeMessage(sendMessage);
                }

            } else if (!message.equals("start")) {

                if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 2) {

                    userService.setFirstName(chatId, message);

                    sendQuestion(chatId);

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 3) {

                    if (!numberPattern.matcher(update.getMessage().getText()).matches()) {

                        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Yashinizi duzgun qeyd edin");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Укажите свой возраст правильно");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Insert your age correctly");
                            executeMessage(sendMessage);
                        }

                        return;
                    }

                    if (Integer.parseInt(update.getMessage().getText()) < 18) {

                        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Yash 18 den yuxari olmalidi \uD83D\uDD1E");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Возраст должен быть больше 18 \uD83D\uDD1E");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Age must be over 18 \uD83D\uDD1E");
                            executeMessage(sendMessage);
                        }

                        return;
                    }

                    if (Integer.parseInt(update.getMessage().getText()) > 50) {

                        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Yash 50 den ashagi olmalidi \uD83D\uDD1E");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Возраст должен быть меньше 50 \uD83D\uDD1E");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Age must be under 18 \uD83D\uDD1E");
                            executeMessage(sendMessage);
                        }

                        return;
                    }

                    userService.setAge(chatId, Integer.parseInt(message));

                    sendQuestion(chatId);

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 4) {

                    String dateMessage = update.getMessage().getText();

                    if (!datePattern.matcher(dateMessage).matches() || validateDate(dateMessage) == false) {

                        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Tarixi duz qeyd edin zehmet olmasa");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Введите дату правильно пожалуйста");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Please enter date correctly");
                            executeMessage(sendMessage);
                        }

                        return;
                    }

                    userService.setTarix(chatId, message);

                    sendQuestion(chatId);

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 5) {

                    if (!timePattern.matcher(update.getMessage().getText()).matches()) {

                        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Vaxti duz qeyd edin zehmet olmasa");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Введите время правильно пожалуйста");
                            executeMessage(sendMessage);

                        } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Please enter time correctly");
                            executeMessage(sendMessage);
                        }

                        return;
                    }

                    userService.setTime(chatId, message);

                    sendQuestionToGetPhoneNumber(chatId);

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 6) {

                    if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Ashagida ki duymeye basin !!!");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Нажмите на кнопку ниже !!!");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Click the button below !!!");
                        executeMessage(sendMessage);
                    }

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 7) {

                    if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Shekil gonderin !!!");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Отправьте фото !!!");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("Send photo !!!");
                        executeMessage(sendMessage);
                    }

                } else if (questionHelperService.getQuestionHelperByChatId(chatId).getKey() == 8) {

                    if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("aze")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("/start basin !!!");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("rus")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("/start нажмите");
                        executeMessage(sendMessage);

                    } else if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage().equals("eng")) {

                        SendMessage sendMessage = new SendMessage();
                        sendMessage.setChatId(chatId);
                        sendMessage.setText("/start click");
                        executeMessage(sendMessage);
                    }

                }

            }

        } else if (update.hasCallbackQuery()) {

            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String callBackData = update.getCallbackQuery().getData();

            if (callBackData.equals("aze")) {

                setLanguageToChatId(chatId, callBackData);

            } else if (callBackData.equals("rus")) {

                setLanguageToChatId(chatId, callBackData);

            } else if (callBackData.equals("eng")) {

                setLanguageToChatId(chatId, callBackData);

            } else if (callBackData.equals("-")) {

                String caption = update.getCallbackQuery().getMessage().getCaption();

                int startIndex = caption.indexOf("User = ") + "User = ".length();
                int endIndex = caption.indexOf("\n", startIndex);
                long chatIdFromCaption = Long.parseLong(caption.substring(startIndex, endIndex).trim());

                userService.setStatus(chatIdFromCaption, "-");

                editCaption(chatId, update.getCallbackQuery().getMessage().getMessageId().toString(), userService.getUserByChatId(chatIdFromCaption));

                sendMessageToUserFromAdminPanel(userService.getUserByChatId(chatIdFromCaption), "-");

            } else if (callBackData.equals("+")) {

                String caption = update.getCallbackQuery().getMessage().getCaption();

                int startIndex = caption.indexOf("User = ") + "User = ".length();
                int endIndex = caption.indexOf("\n", startIndex);
                long chatIdFromCaption = Long.parseLong(caption.substring(startIndex, endIndex).trim());

                userService.setStatus(chatIdFromCaption, "+");

                editCaption(chatId, update.getCallbackQuery().getMessage().getMessageId().toString(), userService.getUserByChatId(chatIdFromCaption));

                sendMessageToUserFromAdminPanel(userService.getUserByChatId(chatIdFromCaption), "+");

            }

        } else if (update.getMessage().hasContact()) {

            if (questionHelperService.getQuestionHelperByChatId(update.getMessage().getChatId()).getKey() == 6) {

                userService.setPhone(update.getMessage().getChatId(), update.getMessage().getContact().getPhoneNumber());

                sendQuestion(update.getMessage().getChatId());
            }

        } else if (update.getMessage().hasPhoto()) {

            if (questionHelperService.getQuestionHelperByChatId(update.getMessage().getChatId()).getKey() == 7) {

                savePhotoToFileAndDatabase(update);

                sendUserToAdminPanel(userService.getUserByChatId(update.getMessage().getChatId()));

                sendQuestion(update.getMessage().getChatId());

            }

        }

    }

    public void editCaption(long chatId, String messageId, User user) {
        EditMessageCaption editMessageCaption = new EditMessageCaption();
        editMessageCaption.setChatId(chatId);
        editMessageCaption.setMessageId(Integer.parseInt(messageId));
        editMessageCaption.setCaption(user.toString2());

        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void languageSelection(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("♠️ Mojo klubuna xosh gelmisiniz.\n" +
                "\uD83D\uDEC2 Face Control prosesi 2 deqiqenizi alacaq.\n \n" +
                "Hansı dildə dəvam etmək istəyirsiniz ?");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        List<InlineKeyboardButton> inlineRow = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Aze \uD83C\uDDE6\uD83C\uDDFF");
        button1.setCallbackData("aze");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Rus \uD83C\uDDF7\uD83C\uDDFA");
        button2.setCallbackData("rus");
        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Eng \uD83C\uDDEC\uD83C\uDDE7");
        button3.setCallbackData("eng");

        inlineRow.add(button1);
        inlineRow.add(button2);
        inlineRow.add(button3);

        inlineRows.add(inlineRow);
        inlineKeyboardMarkup.setKeyboard(inlineRows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        executeMessage(sendMessage);
    }

    public void setLanguageToChatId(long chatId, String language) {

        if (questionHelperService.getQuestionHelperByChatId(chatId).getLanguage() != null) {
            questionHelperService.updateKeyToZero(chatId);
        }
        questionHelperService.setLanguageToChatId(chatId, language);

        questionHelperService.updateKeyInChatId(chatId);

        sendQuestion(chatId);
    }

    public String getQuestion(long chatId) {

        QuestionHelper questionHelper = questionHelperService.getQuestionHelperByChatId(chatId);

        return questionService.getQuestionByKeyAndLanguage(questionHelper.getKey(), questionHelper.getLanguage()).getQuestion();
    }

    public SendMessage sendQuestion(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(getQuestion(chatId));

        removeKeyboard(sendMessage);

        executeMessage(sendMessage);

        questionHelperService.updateKeyInChatId(chatId);

        return sendMessage;
    }

    public void removeKeyboard(SendMessage sendMessage) {
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
    }

    public void sendQuestionToGetPhoneNumber(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(getQuestion(chatId));

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();

        row.add(KeyboardButton.builder().text("My phone number").requestContact(true).build());
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        executeMessage(sendMessage);

        questionHelperService.updateKeyInChatId(chatId);

    }

    public void savePhotoToFileAndDatabase(Update update) {

        // Get the photo file ID from the update
        List<PhotoSize> photos = update.getMessage().getPhoto();
        String fileId = photos.get(photos.size() - 1).getFileId();

        try {
            // Download the photo from Telegram servers
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = execute(getFileRequest);

            // Generate a unique file name for the downloaded photo
//            String fileName = UUID.randomUUID().toString() + ".jpg";
            String fileName = update.getMessage().getChatId() + ".jpg";
            File downloadedFile = new File("C:\\Users\\Murad\\Desktop\\tgbotphotos\\" + fileName);

            // Save the downloaded photo to the file
            InputStream is = new URL(file.getFileUrl(getBotToken())).openStream();
            OutputStream os = new FileOutputStream(downloadedFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            is.close();

            byte[] photoBytes = Files.readAllBytes(downloadedFile.toPath());

            userService.setPhoto(update.getMessage().getChatId(), photoBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // ---------------------------------------------------------------------------------------------------------------

    public void executeMessage(BotApiMethod sendMessage) {

        try {
            execute(sendMessage);
        } catch (TelegramApiException telegramApiException) {

        }

    }

    // ---------------------------------------------------------------------------------------------------------------


    public void sendMessageToAdminPanel(long chatId) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("You are Admin Panel for Face Control \uD83D\uDEC2 \n" +
                "In order to use required operation, use commands below \uD83D\uDCAC");

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();

        row.add("All requests");
        row.add("Selected date");
        row2.add("Positive");
        row2.add("Negative");
        row2.add("Update");

        keyboardRows.add(row);
        keyboardRows.add(row2);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        executeMessage(sendMessage);
    }

    public void sendUserToAdminPanel(User user) {

        File file = new File("C:\\Users\\Murad\\Desktop\\tgbotphotos\\" + user.getChatId());

        InputFile photoInputFile = new InputFile(new ByteArrayStream(user.getPhoto()), file.getName());
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(botConfig.getOwnerId());
        sendPhoto.setPhoto(photoInputFile);
        sendPhoto.setCaption(user.toString2());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineRows = new ArrayList<>();

        List<InlineKeyboardButton> inlineRow = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("❌");
        button1.setCallbackData("-");
        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("✅");
        button2.setCallbackData("+");

        inlineRow.add(button1);
        inlineRow.add(button2);

        inlineRows.add(inlineRow);
        inlineKeyboardMarkup.setKeyboard(inlineRows);

        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);

        try {

            execute(sendPhoto);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void sendAllUsersToAdminPanel(List<User> allUsers) {

        for (User user : allUsers) {

            File file = new File("C:\\Users\\Murad\\Desktop\\tgbotphotos\\" + user.getChatId());

            InputFile photoInputFile = new InputFile(new ByteArrayStream(user.getPhoto()), file.getName());
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(botConfig.getOwnerId());
            sendPhoto.setPhoto(photoInputFile);
            sendPhoto.setCaption(user.toString2());

            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }

    }

    public void sendMessageToUserFromAdminPanel(User user, String status) {

        String text = null;

        if (questionHelperService.getQuestionHelperByChatId(user.getChatId()).getLanguage().equals("aze")) {
            if (status == "+") {
                text = "Tebrikler, siz Face Controlu ugurla kecdiz ✅";
            } else if (status == "-") {
                text = "Uzur isteyirik, siz Face Controlu kecmediz ❌";
            }
        } else if (questionHelperService.getQuestionHelperByChatId(user.getChatId()).getLanguage().equals("rus")) {
            if (status == "+") {
                text = "Поздравляем, Вы успешно прошли Face Control ✅";
            } else if (status == "-") {
                text = "Извините, Вы не прошли Face Control ❌";
            }
        } else if (questionHelperService.getQuestionHelperByChatId(user.getChatId()).getLanguage().equals("eng")) {
            if (status == "+") {
                text = "Congratulations, You passed face control ✅";
            } else if (status == "-") {
                text = "Unfortunately, You did not passed face control ❌";
            }
        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(user.getChatId());
        sendMessage.setText(text);

        executeMessage(sendMessage);

    }

    public boolean validateDate(String inputDate) {
        LocalDate currentDate = LocalDate.now();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate insertedDate = LocalDate.parse(inputDate, dateTimeFormatter);
        LocalDate monthAfter = currentDate.plusMonths(1);

        if (insertedDate.isBefore(currentDate)) {
            return false;
        } else if (insertedDate.isAfter(monthAfter)) {
            return false;
        }

        return true;
    }

    private static class ByteArrayStream extends ByteArrayInputStream {
        public ByteArrayStream(byte[] buf) {
            super(buf);
        }

        @Override
        public String toString() {
            return "ByteArrayStream";
        }
    }

}