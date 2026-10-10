package com.example.posapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String redirectRoot() {
        return "redirect:/jk_wellness/";
    }

    @GetMapping({"/jk_wellness", "/jk_wellness/"})
    public String showApp() {
        return "forward:/jk_wellness/index.html";
    }
}