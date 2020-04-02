package com.example.bot;

import com.example.bot.models.CityNote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CityNoteService {
    private final String notesPath = "/notes";
    private final String citiesPath = "/cities";

    @Value("${app.service-url}")
    private String serviceUrl;

    public List<CityNote> getCityNotes(Long id){
        RestTemplate template = new RestTemplate();
        String path = serviceUrl +citiesPath +'/' + id.toString() +   notesPath ;
        CityNote[] notes = template.getForObject(path, CityNote[].class);
        return Arrays.asList(notes);
    }

    public CityNote getCityNote(Long id) {
        RestTemplate template = new RestTemplate();
        String path = serviceUrl + notesPath + '/' + id.toString();
        CityNote note = template.getForObject(path, CityNote.class);
        return note;
    }

}
