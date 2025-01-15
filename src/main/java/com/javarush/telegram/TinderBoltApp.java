package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "JR_Dexter_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7798102833:AAHrs-7PygXqvr9umS5qkTn0tdpr2UE_wFU"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = " "; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo infoAboutMe;
    private UserInfo infoAboutFriend;
    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь

        String message = getMessageText();
        showMainMenu("Главное меню", "/main",
                "Задай вопрос Вселенной", "/gpt",
                "Создай профиль в Tinder", "/profile",
                "Познакомься с твоей мечтой", "/opener",
                "Пообщайся с мечтой", "/message",
                "Напиши звезде", "/date");
        //Mode MAIN
        if (message.equals("/main")){
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }
        //Mode GPT
        if (message.equals("/gpt")){
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }
        if (currentMode == DialogMode.GPT){
            String prompt = loadPrompt("gpt");
            String answer = chatGPT.sendMessage(prompt, message);
            sendTextMessage(answer);
            return;
        }
        //Mode DATE
        if (message.equals("/date")){
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендея", "date_zendaya",
                    "Райан Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }
        if (currentMode == DialogMode.DATE){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")){
                sendPhotoMessage(query);
                sendTextMessage("Хороший выбор! Теперь задача пригласить пару на свидание!");
                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }
            String answer = chatGPT.addMessage(message);
            sendTextMessage(answer);
            return;
        }
        //Mode Message
        if (message.equals("/message")){
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Кинь в чат свою переписку!",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }
        if (currentMode == DialogMode.MESSAGE){
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")){
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);
                Message msg = sendTextMessage("Ожидайте, *ChatGPT* обрабатывает Ваш запрос");
                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                updateTextMessage(msg, message);
                return;
            }
            list.add(message);
            return;
        }
        //Mode PROFILE
        if (message.equals("/profile")){
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");
            infoAboutMe = new UserInfo();
            questionCount ++;
            sendTextMessage("Для составление анкеты *ChatGPT* ответьте на несколько вопросов");
            sendTextMessage("Вопрос: " + questionCount + " Сколько Вам лет?");
            return;
        }
        if (currentMode == DialogMode.PROFILE){
            switch (questionCount){
                case 1:
                    infoAboutMe.age = message;
                    questionCount ++;
                    sendTextMessage("Вопрос: " + questionCount + " Кем вы работаете?");
                    return;
                case 2:
                    infoAboutMe.occupation = message;
                    questionCount ++;
                    sendTextMessage("Вопрос: " + questionCount + " Какое у Вас хобби?");
                    return;
                case 3:
                    infoAboutMe.hobby = message;
                    questionCount ++;
                    sendTextMessage("Вопрос: " + questionCount + " Что Вам НЕ нравится в людях?");
                    return;
                case 4:
                    infoAboutMe.annoys = message;
                    questionCount ++;
                    sendTextMessage("Вопрос: " + questionCount + " Какова цель знакомства?");
                    return;
                case 5:
                    infoAboutMe.goals = message;
                    questionCount = 0;
                    String aboutMe = infoAboutMe.toString();
                    String prompt = loadPrompt("profile");
                    String answer = chatGPT.sendMessage(prompt,aboutMe);
                    Message msg = sendTextMessage("Ожидайте, *ChatGPT* обрабатывает Ваш запрос");
                    updateTextMessage(msg, answer);
                    return;
            }
        }
        //Mode OPENER
        if (message.equals("/opener")){
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");
            infoAboutFriend = new UserInfo();
            questionCount ++;
            sendTextMessage("Для составления анкеты *ChatGPT* пришли анкетные данные друга/подруги");
            sendTextMessage("Впопрос" + questionCount + " Это кто, девушка или парень?");
            return;
        }
        if (currentMode ==DialogMode.OPENER){
            switch (questionCount) {
                case 1:
                    infoAboutFriend.sex = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Сколько лет?");
                    return;
                case 2:
                    infoAboutFriend.age = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Как зовут?");
                    return;
                case 3:
                    infoAboutFriend.name = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Где живет?");
                    return;
                case 4:
                    infoAboutFriend.city = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Привлекателен/Привлекательна (по 10-бальной шкале?");
                    return;
                case 5:
                    infoAboutFriend.handsome = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Чем увлекается?");
                    return;
                case 6:
                    infoAboutFriend.hobby = message;
                    questionCount++;
                    sendTextMessage("Вопрос: " + questionCount + " Какие цели для знакомства?");
                    return;
                case 7:
                    infoAboutFriend.goals = message;
                    questionCount = 0;
                    String aboutFriend = infoAboutFriend.toString();
                    String prompt = loadPrompt("profile");
                    String answer = chatGPT.sendMessage(prompt,aboutFriend);
                    Message msg = sendTextMessage("Ожидайте, *ChatGPT* обрабатывает Ваш запрос");
                    updateTextMessage(msg, answer);
                    return;
            }
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }

}
