package com.example.telegramcitybot.city;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/cities")
public class CityController {

    @Autowired
    private CityRepository cityRepository;

    @GetMapping
    public List<City> getCities() {
        return cityRepository.findAll();
    }

    @GetMapping("{id}")
    public City getCity(@PathVariable Long id) {
        Optional<City> optionalCityNote = cityRepository.findById(id);
        if (optionalCityNote.isPresent())
            return optionalCityNote.get();
        throw new ResourceNotFoundException();
    }


    @PostMapping
    public City createCity(@RequestBody City city) {
        return cityRepository.save(city);
    }

    @PutMapping("{id}")
    public City updateCity(@PathVariable Long id, @RequestBody City city) {
        Optional<City> optionalOld = cityRepository.findById(id);
        if (optionalOld.isPresent()) {
            city.setId(id);
            return cityRepository.save(city);
        }
        throw new ResourceNotFoundException();
    }

    @DeleteMapping("{id}")
    public void deleteCity(@PathVariable Long id) {
        Optional<City> optionalOld = cityRepository.findById(id);
        if (optionalOld.isPresent()) {
            cityRepository.deleteById(id);
        }
        throw new ResourceNotFoundException();
    }
}
