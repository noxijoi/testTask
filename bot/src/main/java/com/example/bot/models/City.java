package com.example.bot.models;

import lombok.Data;

import java.util.List;

@Data
public class City {
    private Long id;
    private String name;
    private List<CityNote> notes;
}
