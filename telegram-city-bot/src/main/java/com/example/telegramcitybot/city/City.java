package com.example.telegramcitybot.city;

import com.example.telegramcitybot.cityNote.CityNote;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "city")
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<CityNote> notes;
}
