package ua.bugaienko.telegrambot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ua.bugaienko.telegrambot.config.BotConfig;
import ua.bugaienko.telegrambot.model.Anketa;
import ua.bugaienko.telegrambot.model.Answer;
import ua.bugaienko.telegrambot.model.User;
import ua.bugaienko.telegrambot.model.UserRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sergii Bugaienko
 */

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig config;
    private final UserRepository userRepository;
    private final AnketaService anketaService;
    private final QuestionsService questionsService;
    private final AnswersService answersService;

    private static final String HELP_TEXT = "This is a bot-assistant to nutrition consultant Irina Bug.\n\n"
            + "You can execute commands from the main menu on left or by typing a command:\n\n"
            + "Type /start to see a welcome message\n"
            + "Type /data  to see data stored about yourself\n"
            + "Type /help to see this message again";

    @Autowired
    public TelegramBot(BotConfig config, UserRepository userRepository, AnketaService anketaService, QuestionsService questionsService, AnswersService answersService) {
        this.config = config;
        this.userRepository = userRepository;
        this.anketaService = anketaService;
        this.questionsService = questionsService;
        this.answersService = answersService;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Get a welcome message"));
        listOfCommands.add(new BotCommand("/checkList", "Fill anketa"));

        try {
            execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();

            long chatId = update.getMessage().getChatId();

            Anketa anketa = anketaService.getAnketaByChatId(chatId);
            if (anketa != null && anketa.isWaitingForAnswer()) {
                System.out.println("IF! status: " + anketa.getCurrentState() );
                processAnswer(chatId, messageText);

            } else {
                switch (messageText) {
                    case "/start":
                        registerUser(update.getMessage());
//                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                        break;
                    case "/checkList":
                        checklistCommandReceived(update);
                        break;
                    case "/help":
                        sendMessage(chatId, HELP_TEXT, generateCleanKeyboard());
                        break;
                    default:
                        sendMessage(chatId, "Sorry, command was not recognize :(");

                }

            }
        }
    }


    private void checklistCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        Message msg = update.getMessage();
        var chat = msg.getChat();
        Optional<User> optionalUser = userRepository.findById(msg.getChatId());
        User user = optionalUser.orElseGet(() -> registerUser(msg));

        Anketa anketa = anketaService.getAnketaByChatId(chatId);


        //TODO Ask a few questions
        int currentState = anketa.getCurrentState();
        Map<Integer, String> questionsMap = questionsService.getQuestionsMap();

        if (currentState <= anketa.getLastIndex()) {

            sendMessage(chatId, "Вопрос " + currentState + ":\n" + questionsMap.get(currentState));
            anketa.setWaitingForAnswer(true);
            anketaService.save(anketa);
//            String answerStr = msg.getText();
//            anketaService.saveAnswer(chatId, answerStr, currentState);

        } else {
            //Все вопросы заданы
            sendMessage(chatId, "Все вопросы уже заданы");
            anketa.setWaitingForAnswer(false);
            anketa = anketaService.save(anketa);
            List<Answer> answers = answersService.getAllAnswerByAnketa(anketa);

            for (Answer answer: answers) {
            StringBuilder sb = new StringBuilder();
                sb.append("Вопрос ")
                        .append(answer.getAnswerNumber())
                        .append(": ")
                        .append(answer.getQuestion()).append("\n");
                sb.append(answer.getAnswer()).append("\n\n");
                sendMessage(chatId, sb.toString());
            }
            sendMessage(chatId, "Спасибо за Ваши ответы");
        }

    }

    private void processAnswer(long chatId, String msg) {
//        System.out.println("processAnswer start");
        Anketa anketa = anketaService.getAnketaByChatId(chatId);
        int currentState = anketa.getCurrentState();

        if (currentState <= anketa.getLastIndex()) {
            System.out.println("currentState " + currentState);
            Map<Integer, String> questionsMap = questionsService.getQuestionsMap();
            System.out.println("ответ: " + msg);

            if (!msg.equalsIgnoreCase("stop")) {
                anketa = anketaService.saveAnswer(chatId, msg, currentState);
                System.out.println(anketa.getCurrentState());
                currentState++;
                if (currentState <= anketa.getLastIndex()) {
                    String questionNumber = "Вопрос " + currentState + ":\n";
                    sendMessage(chatId, questionNumber + questionsMap.get(currentState));
                }
            } else {
                anketa.setWaitingForAnswer(false);
                anketaService.save(anketa);
            }
        } else {
            anketa.setWaitingForAnswer(false);
            anketaService.save(anketa);
        }


    }


    private User registerUser(Message msg) {
        Optional<User> optionalUser = userRepository.findById(msg.getChatId());
        if (optionalUser.isEmpty()) {
            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUsername(chat.getUserName());
            user.setActive(true);
            user.setRegisterAt(new Timestamp(System.currentTimeMillis()));

            log.info("user saved: " + user);
            return userRepository.save(user);
        } else {
            return optionalUser.get();
        }

    }

    private void startCommandReceived(long chatId, String name) {

        String answer = EmojiParser.parseToUnicode("Hi, " + name + ", nice to see you! " + " :wave:");
//        String answer = "Hi, " + name + ", nice to meet you!";
        log.info("Replied to user " + name);


        sendMessage(chatId, answer);
//        sendMessage(chatId, answer, generateKeyboardForStartMessage());

    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboardMarkup keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        message.setReplyMarkup(keyboardMarkup);


        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup generateKeyboardForStartMessage() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();

        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");

        keyboardRows.add(row);

        row = new KeyboardRow();

        row.add("register");
        row.add("check my data");
        row.add("delete my data");

        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

    private ReplyKeyboardMarkup generateCleanKeyboard() {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);

        return keyboardMarkup;
    }

}
