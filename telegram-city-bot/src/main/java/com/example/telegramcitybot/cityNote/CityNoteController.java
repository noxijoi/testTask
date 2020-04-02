package com.example.telegramcitybot.cityNote;

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

    @GetMapping
    public List<CityNote> getCityNote(@RequestParam Long cityId) {
        return cityNoteRepository.findAllByCityId(cityId);
    }

    @GetMapping("{id}")
    public CityNote getNote(@PathVariable Long id) {
        Optional<CityNote> optionalCityNote = cityNoteRepository.findById(id);
        if (optionalCityNote.isPresent())
            return optionalCityNote.get();
        throw new ResourceNotFoundException("No note " + id);
    }

    @PostMapping
    public CityNote createCityNote(@RequestBody CityNote cityNote) {
        return cityNoteRepository.save(cityNote);
    }

    @PutMapping("{id}")
    public CityNote updateCityNote(@PathVariable Long id, @RequestBody CityNote cityNote) {
        Optional<CityNote> optionalOld = cityNoteRepository.findById(id);
        if (optionalOld.isPresent()) {
            cityNote.setId(id);
            return cityNoteRepository.save(cityNote);
        }
        throw new ResourceNotFoundException();
    }

    @DeleteMapping("{id}")
    public void deleteCityNote(@PathVariable Long id) {
        Optional<CityNote> optionalOld = cityNoteRepository.findById(id);
        if (optionalOld.isPresent()) {
            cityNoteRepository.deleteById(id);
        }
        throw new ResourceNotFoundException();
    }
}
