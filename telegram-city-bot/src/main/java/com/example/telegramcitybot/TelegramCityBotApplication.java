package com.example.telegramcitybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.example.telegramcitybot"})
@EntityScan(basePackages = {"com.example.telegramcitybot"})
public class TelegramCityBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramCityBotApplication.class, args);
    }

}
