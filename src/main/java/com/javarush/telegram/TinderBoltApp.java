package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "dyackov_javarush_tinder_ai2_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7410598901:AAG42TBzwN0ZxoeomL4HUP_H1W3pfXnAUy4"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:y8HQgXidYWEPQ52jBuwnJFkblB3T8AScAHOTiNP46pG97Qae"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String messageText = getMessageText();

        switch (messageText) {
            case "/start" -> {
                currentMode = DialogMode.MAIN;
                sendPhotoMessage("main");
                String text = loadMessage("main");
                sendTextMessage(text);

                showMainMenu("Главное меню бота", "/start",
                        "Генерация Tinder-профля \uD83D\uDE0E", "/profile",
                        "Сообщение для знакомства \uD83E\uDD70", "/opener",
                        "Переписка от вашего имени \uD83D\uDE08", "/message",
                        "Переписка со звездами \uD83D\uDD25", "/date",
                        "Задать вопрос чату GPT \uD83E\uDDE0", "/gpt");
                return;
            }
            case "/profile" -> {
                currentMode = DialogMode.PROFILE;
                sendPhotoMessage("profile");
                String text = loadMessage("profile");
                sendTextMessage(text);
            }
            case "/opener" -> {
                currentMode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                String text = loadMessage("opener");
                sendTextMessage(text);
                return;
            }
            case "/message" -> {
                currentMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                String text = loadMessage("message");
                sendTextMessage(text);
                return;
            }
            case "/date" -> {
                currentMode = DialogMode.DATE;
                sendPhotoMessage("date");
                String text = loadMessage("date");
                sendTextMessage(text);
                List<String> people = List.of("Ариана Гранде", "1", "Марго Робби", "2", "Зендея", "3",
                        "Райан Гослинг", "4", "Том Харди", "5");
                sendTextButtonsMessage("Выберите доступный профиль: ", people);
                return;
            }
            case "/gpt" -> {
                currentMode = DialogMode.GPT;
                sendPhotoMessage("gpt");
                String text = loadMessage("gpt");
                sendTextMessage(text);
                return;
            }
            case "/stop" -> {
                sendTextMessage("_*До новых встреч*_");
                return;
            }
            default -> {
                if (messageText.contains("/")) {
                    sendTextMessage("Команды *" + messageText + "* не существует.");
                    return;
                }
            }
        }

        switch (currentMode) {
            case MAIN -> {
                String prompt = loadPrompt("main");
            }
            case PROFILE -> {
                String prompt = loadPrompt("profile");
            }
            case OPENER -> {
                String prompt = loadPrompt("opener");
            }
            case MESSAGE -> {
                String prompt = loadPrompt("message");
            }
//            case DATE -> {
//                String prompt = loadPrompt("date");
//            }
            case GPT -> {
                String prompt = loadPrompt("gpt");
                String answer = chatGPT.sendMessage(prompt, messageText);
                sendTextMessage(answer);
            }
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
