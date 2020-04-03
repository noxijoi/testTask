package com.example.telegramcitybot.cityNote;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/notes")
public class CityNoteController {

    @Autowired
    private CityNoteRepository cityNoteRepository;

    private ModelMapper mapper = new ModelMapper();


    @GetMapping("{id}")
    public NoteDto getNote(@PathVariable Long id) {
        Optional<CityNote> optionalCityNote = cityNoteRepository.findById(id);
        if (optionalCityNote.isPresent()) {
            return mapper.map(optionalCityNote.get(), NoteDto.class);
        }
        throw new ResourceNotFoundException("No note " + id);
    }


    @PostMapping
    public NoteDto createCityNote(@RequestBody CityNote cityNote) {
        CityNote created = cityNoteRepository.save(cityNote);
        return mapper.map(created, NoteDto.class);
    }

    @PutMapping("{id}")
    public NoteDto updateCityNote(@PathVariable Long id, @RequestBody CityNote cityNote) {
        Optional<CityNote> optionalOld = cityNoteRepository.findById(id);
        if (optionalOld.isPresent()) {
            CityNote note = optionalOld.get();
            note.setNote(cityNote.getNote());
            CityNote updated =  cityNoteRepository.save(note);
            return mapper.map(updated, NoteDto.class);
        }
        throw new ResourceNotFoundException();
    }

    @DeleteMapping("{id}")
    public void deleteCityNote(@PathVariable Long id) {
        Optional<CityNote> optionalOld = cityNoteRepository.findById(id);
        if (optionalOld.isPresent()) {
            cityNoteRepository.deleteById(id);
            return;
        }
        throw new ResourceNotFoundException();
    }
}
