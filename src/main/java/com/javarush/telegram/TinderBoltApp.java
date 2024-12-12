package com.javarush.telegram;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class TinderBoltApp extends MultiSessionTelegramBot {
    public static final String TELEGRAM_BOT_NAME = "dyackov_javarush_tinder_ai2_bot"; //TODO: добавь имя бота в кавычках
    public static final String TELEGRAM_BOT_TOKEN = "7410598901:AAG42TBzwN0ZxoeomL4HUP_H1W3pfXnAUy4"; //TODO: добавь токен бота в кавычках
    public static final String OPEN_AI_TOKEN = "gpt:y8HQgXidYWEPQ52jBuwnJFkblB3T8AScAHOTiNP46pG97Qae"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);
    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();
    private UserInfo me;
    private UserInfo she;
    private int questionCount;


    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        switch (message) {
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
                me = new UserInfo();
                questionCount = 1;
                sendTextMessage("Сколько вам лет?");

                return;
            }
            case "/opener" -> {
                currentMode = DialogMode.OPENER;
                sendPhotoMessage("opener");
                String text = loadMessage("opener");
                sendTextMessage(text);
                she = new UserInfo();
                questionCount = 1;
                sendTextMessage("Имя девушки?");
                return;
            }
            case "/message" -> {
                currentMode = DialogMode.MESSAGE;
                sendPhotoMessage("message");
                String text = loadMessage("message");
                sendTextButtonsMessage(text, "Следующее сообщение", "message_next",
                        "Пригласить на свидание", "message_date");
                return;
            }
            case "/date" -> {
                currentMode = DialogMode.DATE;
                sendPhotoMessage("date");
                String text = loadMessage("date");
                List<String> people = List.of("Ариана Гранде", "date_grande",
                        "Марго Робби", "date_robbie",
                        "Зендея", "date_zendaya",
                        "Райан Гослинг", "date_gosling",
                        "Том Харди", "date_hardy");
                sendTextButtonsMessage(text, people);
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
                if (message.contains("/")) {
                    sendTextMessage("Команды *" + message + "* не существует.");
                    return;
                }
            }
        }

        switch (currentMode) {
            case PROFILE -> {
                if (!isMessageCommand()) {
                    switch (questionCount) {
                        case 1:
                            me.age = message;
                            questionCount = 2;
                            sendTextMessage("Кем вы работаете?");
                            return;
                        case 2:
                            me.occupation = message;
                            questionCount = 3;
                            sendTextMessage("У вас есть хобби?");
                            return;
                        case 3:
                            me.hobby = message;
                            questionCount = 4;
                            sendTextMessage("Что вам НЕ нравиться в людях?");
                            return;
                        case 4:
                            me.annoys = message;
                            questionCount = 5;
                            sendTextMessage("Цель знакомства?");
                            return;
                        case 5:
                            me.goals = message;
                            String aboutMyself = me.toString();
                            String prompt = loadPrompt("profile");
                            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT \uD83E\uDDE0 думает...");
                            String answer = chatGPT.sendMessage(prompt, aboutMyself);
                            updateTextMessage(msg, answer);
                            return;
                    }
                }
            }
            case OPENER -> {
                if (!isMessageCommand()) {
                    switch (questionCount) {
                        case 1:
                            she.name = message;
                            questionCount = 2;
                            sendTextMessage("Сколько ей лет?");
                            return;
                        case 2:
                            me.age = message;
                            questionCount = 3;
                            sendTextMessage("Есть ли у неё хобби и какие?");
                            return;
                        case 3:
                            me.hobby = message;
                            questionCount = 4;
                            sendTextMessage("Кем она работает?");
                            return;
                        case 4:
                            me.occupation = message;
                            questionCount = 5;
                            sendTextMessage("Цель знакомства?");
                            return;
                        case 5:
                            me.goals = message;
                            String aboutFriend = me.toString();
                            String prompt = loadPrompt("opener");
                            Message msg = sendTextMessage("Подождите пару секунд - ChatGPT \uD83E\uDDE0 думает...");
                            String answer = chatGPT.sendMessage(prompt, aboutFriend);
                            updateTextMessage(msg, answer);
                            return;
                    }
                }
            }
            case MESSAGE -> {
                if (!isMessageCommand()) {
                    String query = getCallbackQueryButtonKey();
                    if (query.startsWith("message_")) {
                        String prompt = loadPrompt(query);
                        String userChatHistory = String.join("\n\n", list);
                        Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                        String answer = chatGPT.sendMessage(prompt, userChatHistory);
                        updateTextMessage(msg, answer);
                        return;
                    }
                }
                list.add(message);
                return;
            }
            case DATE -> {
                if (!isMessageCommand()) {
                    String query = getCallbackQueryButtonKey();
                    if (query.startsWith("date_")) {
                        sendPhotoMessage(query);
                        sendTextMessage("Отличный выбор");
                        String prompt = loadPrompt(query);
                        chatGPT.setPrompt(prompt);
                        return;
                    }
                }
                Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                String answer = chatGPT.addMessage(message);
                updateTextMessage(msg, answer);
                return;
            }
            case GPT -> {
                if (!isMessageCommand()) {
                    String prompt = loadPrompt("gpt");
                    Message msg = sendTextMessage("Подождите пару секунд - ChatGPT думает...");
                    String answer = chatGPT.sendMessage(prompt, message);
                    updateTextMessage(msg, answer);
                    return;
                }
            }
        }
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
