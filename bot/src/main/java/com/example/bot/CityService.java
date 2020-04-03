package com.example.bot;


import com.example.bot.models.City;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class CityService {
    private final  String citiesPath ="/cities";

    @Value("${app.service-url}")
    private String serviceUrl;

    public List<City> getCities(){
        RestTemplate template = new RestTemplate();
        String path = serviceUrl + citiesPath;
        City[] cities = template.getForObject(path, City[].class);
        return Arrays.asList(cities);
    }

    public City createCity(City city) {
        RestTemplate template = new RestTemplate();
        String path = serviceUrl + citiesPath;
        City created = template.postForObject(path, city, City.class);
        return created;
    }

    public void updateCity(City city) {
        RestTemplate template = new RestTemplate();
        String path = serviceUrl + citiesPath;
        template.put(path, city, City.class);
    }

    public void  deleteCity(Long id){
        RestTemplate restTemplate = new RestTemplate();
        String path = serviceUrl + citiesPath + '/' + id.toString();
        restTemplate.delete(path);
    }
}
