package com.example.bot;

import com.example.bot.models.City;
import com.example.bot.models.CityNote;
import lombok.extern.slf4j.Slf4j;
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
        if (message.hasText()) {
            String text = message.getText();
            Integer userId = message.getFrom().getId();
            Action lastAction = usersLastAction.get(userId);
            CallbackAction userAction = lastAction.getCallbackAction();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getChatId());
            switch (userAction) {
                case CREATE_NOTE: {
                    CityNote note = new CityNote();
                    City city = new City();
                    city.setId(lastAction.getConnectedId());
                    note.setCity(city);
                    note.setNote(text);
                    CityNote created = cityNoteService.createNote(note);
                    sendMessage.setText("Note #" + created.getId() + "\n" + created.getNote());
                    sendMessage.setReplyMarkup(builtNoteKeyboard(created));
                    break;
                }
                case CREATE_CITY: {
                    City city = new City();
                    city.setName(text);
                    cityService.createCity(city);
                    List<City> cities = cityService.getCities();
                    sendMessage.setText("Choose city");
                    sendMessage.setReplyMarkup(buildCitiesKeyboard(cities));
                    break;
                }
                case UPDATE_CITY: {
                    City city = new City();
                    city.setName(text);
                    cityService.updateCity(city);
                    List<City> cities = cityService.getCities();
                    sendMessage.setText("Choose city");
                    sendMessage.setReplyMarkup(buildCitiesKeyboard(cities));
                    break;
                }
                case UPDATE_NOTE:
                    CityNote note = new CityNote();
                    note.setId(lastAction.getConnectedId());
                    note.setNote(text);
                    cityNoteService.updateNote(note);
                    CityNote updated = cityNoteService.getCityNote(lastAction.getConnectedId());
                    sendMessage.setText("Note #" + updated.getId() + "\n" + updated.getNote());
                    sendMessage.setReplyMarkup(builtNoteKeyboard(updated));
                    break;
            }
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
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
                            List<City> cities = cityService.getCities();
                            sendMessage.setReplyMarkup(buildCitiesKeyboard(cities));
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
    }

    private void dispatchCallBackQuery(CallbackQuery callbackQuery, Update update) {
        String callbackData = callbackQuery.getData();
        CallbackAction callbackAction = getCallBackAction(callbackData);
        String data = getCallbackDataValue(callbackData);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        Action action = new Action();
        try {
            switch (callbackAction) {
                case GET_CITY_NOTES:
                    Long cityId = Long.parseLong(data);
                    List<CityNote> cityNotes = cityNoteService.getCityNotes(cityId);
                    sendMessage.setReplyMarkup(buildCityNotesKeyboard(cityNotes, cityId));
                    sendMessage.setText("Notes:");
                    execute(sendMessage);
                    usersLastAction.remove(callbackQuery.getFrom().getId());
                    break;
                case GET_CITIES:
                    List<City> cities = cityService.getCities();
                    sendMessage.setReplyMarkup(buildCitiesKeyboard(cities));
                    sendMessage.setText("Choose city");
                    execute(sendMessage);
                    break;
                case GET_NOTE:
                    CityNote note = cityNoteService.getCityNote(Long.parseLong(data));
                    sendMessage.setText("Note #" + note.getId() + "\n" + note.getNote());
                    sendMessage.setReplyMarkup(builtNoteKeyboard(note));
                    execute(sendMessage);
                    usersLastAction.remove(callbackQuery.getFrom().getId());
                    break;
                case CREATE_CITY:
                    sendMessage.setText("Enter city name");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.CREATE_CITY);
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case CREATE_NOTE:
                    sendMessage.setText("Enter note text");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.CREATE_NOTE);
                    action.setConnectedId(Long.parseLong(data));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case UPDATE_CITY:
                    sendMessage.setText("Enter updated name");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.UPDATE_CITY);
                    action.setConnectedId(Long.parseLong(data));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case UPDATE_NOTE:
                    sendMessage.setText("Enter updated note text");
                    execute(sendMessage);
                    action.setCallbackAction(CallbackAction.UPDATE_NOTE);
                    action.setConnectedId(Long.parseLong(data));
                    usersLastAction.put(callbackQuery.getFrom().getId(), action);
                    break;
                case DELETE_CITY:
                    cityService.deleteCity(Long.parseLong(data));
                    List<City> citiesList = cityService.getCities();
                    sendMessage.setReplyMarkup(buildCitiesKeyboard(citiesList));
                    sendMessage.setText("Choose city");
                    execute(sendMessage);
                    usersLastAction.remove(callbackQuery.getFrom().getId());
                    break;
                case DELETE_NOTE:
                    cityNoteService.deleteNote(Long.parseLong(data));
                    sendMessage.setText("Deleted");
                    execute(sendMessage);
                    usersLastAction.remove(callbackQuery.getFrom().getId());
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

        InlineKeyboardButton toCitiesBtn = new InlineKeyboardButton();
        toCitiesBtn.setCallbackData(CallbackAction.GET_CITIES.name()).setText("Back to cities");
        row.add(toCitiesBtn);

        InlineKeyboardButton deleteBtn = new InlineKeyboardButton();
        deleteBtn.setCallbackData(CallbackAction.DELETE_NOTE.name() + " " + note.getId().toString()).setText("Delete");
        row.add(deleteBtn);

        InlineKeyboardButton updateBtn = new InlineKeyboardButton();
        updateBtn.setCallbackData(CallbackAction.UPDATE_NOTE.name() + " " + note.getId().toString()).setText("Update");
        row.add(updateBtn);

        rows.add(row);

        keyboardMarkup.setKeyboard(rows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup buildCitiesKeyboard(List<City> cities) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
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

    private InlineKeyboardMarkup buildCityNotesKeyboard(List<CityNote> notes, Long cityId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
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
        newNoteBtn.setText("New note").setCallbackData(CallbackAction.CREATE_NOTE.name() + " " + cityId.toString());
        row.add(newNoteBtn);
        rows.add(row);
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
        if (spacePosition < 0) {
            return "";
        }
        return callbackData.substring(spacePosition + 1);
    }

}
