package com.example.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    @Value("${app.botName}")
    private String name;

    @Value("${app.botToken}")
    private String botToken;

    private final String BOT_COMMAND = "bot_command";
    private final String HELP_COMMAND = "/help";
    private final String START_COMMAND = "/start";

    public Bot() {
        super();
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            dispatchCallBackQuery(update.getCallbackQuery(), update);
        }
        if (update.hasMessage()) {
            dispatchMessage(update.getMessage(), update);
        }
    }


    @Override
    public String getBotUsername() {
        return name;
    }

    private void dispatchMessage(Message message, Update update) {
        if (message.isCommand()) {
            List<MessageEntity> entities = message.getEntities();
            dispatchCommands(entities, update);
        }
    }

    private void dispatchCommands(List<MessageEntity> entities, Update update) {
        entities.forEach(messageEntity -> {
            try {
                if (messageEntity.getType().equals(BOT_COMMAND)) {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(update.getMessage().getChatId());
                    switch (messageEntity.getText()) {
                        case HELP_COMMAND:
                            sendMessage.setText("heeelp");
                            execute(sendMessage);
                            break;
                        default:
                            log.info("Unknown command " + messageEntity.getText());
                            sendMessage.setText("I don't know this command");
                            execute(sendMessage);
                    }
                }
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
            }
        });
        //TODO
    }

    private void dispatchCallBackQuery(CallbackQuery callbackQuery, Update update) {
        //TODO
    }

}
