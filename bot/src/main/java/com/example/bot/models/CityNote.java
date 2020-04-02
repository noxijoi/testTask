package com.example.bot.models;

import lombok.Data;

@Data
public class CityNote {
    private Long id;
    private String note;
    private Long cityId;
}
