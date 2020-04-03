package com.example.telegramcitybot.cityNote;

import com.example.telegramcitybot.city.City;
import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "city_note")
public class CityNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
}
