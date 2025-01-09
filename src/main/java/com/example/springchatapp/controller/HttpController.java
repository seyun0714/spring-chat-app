package com.example.springchatapp.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HttpController {
    @GetMapping("/")
    public String serveHomePage(){
        return "index";
    }
}
