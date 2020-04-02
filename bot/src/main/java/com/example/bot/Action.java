package com.example.bot;

import lombok.Data;

@Data
public class Action {
    private CallbackAction callbackAction;
    private Long connectedId;
}
