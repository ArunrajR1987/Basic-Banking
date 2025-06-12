package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PitaController {


    @GetMapping("/hello")
    public String Hello() {
        return "String Hello";
    }

}
