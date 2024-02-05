package com.asgarov.jwtdemoapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("resource")
public class ResourceController {

    @GetMapping
    public String welcome() {
        return "Hello World %s".formatted(LocalDateTime.now());
    }
}
