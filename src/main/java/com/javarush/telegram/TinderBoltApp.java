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
    public static final String OPEN_AI_TOKEN = "gpt:AA_pElEwSQtguqFRo3wdLHVlvuUd5jALcNlsyA_RlyTGo-CROhqxgdG9gLVdL-L56LJD_G9KnmJFkblB3TMVTCX8S9U7k7AjDOqsJkIgPCg2e9pzC665lADggdxudAZ3pHwW2f1nCYRzdCnmZKklHOSh-GOs"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        //String welcome_speech = loadMessage("welcome_speech");
        //sendTextMessage(welcome_speech);
        String message = getMessageText();
        showMainMenu("Главное меню", "/main",
                "Задай вопрос Вселенной", "/gpt",
                "Создай профиль в Tinder", "/profile",
                "Познакомься с твоей мечтой", "/opener",
                "Пообщайся с мечтой", "/message",
                "Напиши звезде", "/date");
        if (message.equals("/main")){
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);
            return;
        }
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
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }

}
