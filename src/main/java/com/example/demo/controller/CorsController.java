package com.example.demo.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class CorsController {

    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public void corsHeaders() {
        // This method handles preflight OPTIONS requests
    }
}