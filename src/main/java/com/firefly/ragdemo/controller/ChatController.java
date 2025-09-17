package com.firefly.ragdemo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message) {
        return "Hello, " + message;
    }

}
