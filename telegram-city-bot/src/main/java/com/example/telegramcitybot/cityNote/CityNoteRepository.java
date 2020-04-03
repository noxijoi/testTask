package com.example.telegramcitybot.cityNote;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CityNoteRepository extends JpaRepository<CityNote, Long> {
    List<CityNote> getByCityId(Long cityId);

    List<CityNote> findByCityId(Long id);
}
