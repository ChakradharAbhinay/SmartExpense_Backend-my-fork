package com.teamfour.smartexpense.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
public class ApiDocController {

    public String redirectToSwaggerUI() {
        return "redirect:/swagger-ui/index.html?url=/swagger/openapi.json";
    }

}
