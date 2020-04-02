package com.example.bot;

import com.example.bot.models.City;
import com.example.bot.models.CityNote;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private HashMap<Integer, Action> usersLastAction = new HashMap<>();

    @Autowired
    private CityService cityService;

    @Autowired
    private CityNoteService cityNoteService;

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
        if (message.hasText()){
            String text = message.getText();
            Integer userId = message.getFrom().getId();
            Action lastAction = usersLastAction.get(userId);
            CallbackAction userAction = lastAction.getCallbackAction();
            switch (userAction){
                case CREATE_NOTE:
                    CityNote note = new CityNote();
                    note.setCityId(lastAction.getConnectedId());
                case CREATE_CITY:
                case UPDATE_CITY:
                case UPDATE_NOTE:
            }
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
                            sendMessage.setText(START_COMMAND + " to start app \n" +
                                    HELP_COMMAND + " to get help");
                            execute(sendMessage);
                            break;
                        case START_COMMAND:
                            buildCitiesKeyboard(sendMessage);
                            sendMessage.setText("Choose city");
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
        String callbackData = callbackQuery.getData();
        CallbackAction callbackAction = getCallBackAction(callbackData);
        String data = getCallbackDataValue(callbackData);
        SendMessage sendMessage = new SendMessage();
        Action action = new Action();
        try {
            switch (callbackAction) {
                case GET_CITY_NOTES:
                    List<CityNote> cityNotes = cityNoteService.getCityNotes(Long.getLong(data));
                    buildCityNotesKeyboard(sendMessage, cityNotes);
                    execute(sendMessage);
                    break;
                case GET_NOTE:
                    CityNote note = cityNoteService.getCityNote(Long.getLong(data));
                    sendMessage.setText(note.getNote());
                    builtNoteKeyboard(note);
                    execute(sendMessage);
                    break;
                case CREATE_CITY:
                    sendMessage.setText("Enter city name");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.CREATE_CITY);
                    usersLastAction.put(callbackQuery.getFrom().getId(), action );
                    break;
                case CREATE_NOTE:
                    sendMessage.setText("Enter note text");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.CREATE_NOTE);
                    action.setConnectedId(Long.getLong(callbackQuery.getData()));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case UPDATE_CITY:
                    sendMessage.setText("Enter updated name");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.UPDATE_CITY);
                    action.setConnectedId(Long.getLong(callbackQuery.getData()));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case UPDATE_NOTE:
                    sendMessage.setText("Enter updated note text");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.UPDATE_NOTE);
                    action.setConnectedId(Long.getLong(callbackQuery.getData()));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    private InlineKeyboardMarkup builtNoteKeyboard(CityNote note) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton deleteBtn = new InlineKeyboardButton();
        deleteBtn.setCallbackData(CallbackAction.DELETE_NOTE.name()).setText("Delete");
        row.add(deleteBtn);
        InlineKeyboardButton UpdateBtn = new InlineKeyboardButton();
        UpdateBtn.setCallbackData(CallbackAction.UPDATE_NOTE.name()).setText("Update");
        row.add(UpdateBtn);
        rows.add(row);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup buildCitiesKeyboard(SendMessage sendMessage) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setReplyMarkup(keyboardMarkup);
        List<City> cities = cityService.getCities();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        cities.forEach(city -> {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(city.getName()).setCallbackData(CallbackAction.GET_CITY_NOTES.name() + " " + city.getId());
            row.add(btn);
            rows.add(row);
        });

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton newCityBtn = new InlineKeyboardButton();
        newCityBtn.setText("New city").setCallbackData(CallbackAction.CREATE_CITY.name());
        row.add(newCityBtn);

        rows.add(row);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup buildCityNotesKeyboard(SendMessage sendMessage, List<CityNote> notes) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        sendMessage.setReplyMarkup(keyboardMarkup);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < notes.size(); i++) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(Integer.toString(i));
            btn.setCallbackData(CallbackAction.GET_NOTE.name() + " " + notes.get(i).getId());
            row.add(btn);
            rows.add(row);
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton newNoteBtn = new InlineKeyboardButton();
        newNoteBtn.setText("New note").setCallbackData(CallbackAction.CREATE_NOTE.name());
        row.add(newNoteBtn);
        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }


    private CallbackAction getCallBackAction(String callBackData) {
        int spacePosition = callBackData.indexOf(" ");
        String actionStr = callBackData;
        if (spacePosition > 0) {
             actionStr = callBackData.substring(0, spacePosition);
        }
        return CallbackAction.valueOf(actionStr);
    }

    private String getCallbackDataValue(String callbackData) {
        int spacePosition = callbackData.indexOf(" ");
        if(spacePosition < 0) {
            return  "";
        }
        return callbackData.substring(spacePosition);
    }

}
