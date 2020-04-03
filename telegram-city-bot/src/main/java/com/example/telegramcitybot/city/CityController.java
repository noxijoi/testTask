package com.example.telegramcitybot.city;

import com.example.telegramcitybot.cityNote.CityNote;
import com.example.telegramcitybot.cityNote.CityNoteRepository;
import com.example.telegramcitybot.cityNote.NoteDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/cities")
public class CityController {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CityNoteRepository cityNoteRepository;

    private ModelMapper mapper = new ModelMapper();

    @GetMapping
    public List<CityDto> getCities() {
        List<City> cities =  cityRepository.findAll();
        return cities.stream()
                .map(city ->mapper.map(city, CityDto.class))
                .collect(Collectors.toList());
    }

    @GetMapping("{id}")
    public CityDto getCity(@PathVariable Long id) {
        Optional<City> optionalCity = cityRepository.findById(id);
        if (optionalCity.isPresent()) {
            City city = optionalCity.get();
            return mapper.map(city, CityDto.class);
        }
        throw new ResourceNotFoundException();
    }

    @GetMapping("{id}/notes")
    public List<NoteDto> getNotesForCity(@PathVariable Long id) {
        List<CityNote> notes = cityNoteRepository.findByCityId(id);
        ModelMapper modelMapper = new ModelMapper();
        List<NoteDto> dtos = notes.stream()
                .map(note -> modelMapper.map(note, NoteDto.class))
                .collect(Collectors.toList());
        return dtos;
    }


    @PostMapping
    public CityDto createCity(@RequestBody City city) {
        City created =  cityRepository.save(city);
        return mapper.map(created, CityDto.class);
    }

    @PutMapping("{id}")
    public CityDto updateCity(@PathVariable Long id, @RequestBody City city) {
        Optional<City> optionalOld = cityRepository.findById(id);
        if (optionalOld.isPresent()) {
            City old = optionalOld.get();
            old.setName(city.getName());
            City updated =  cityRepository.save(old);
            return mapper.map(updated, CityDto.class);
        }
        throw new ResourceNotFoundException();
    }

    @DeleteMapping("{id}")
    public void deleteCity(@PathVariable Long id) {
        Optional<City> optionalOld = cityRepository.findById(id);
        if (optionalOld.isPresent()) {
            cityRepository.deleteById(id);
            return;
        }
        throw new ResourceNotFoundException();
    }
}
