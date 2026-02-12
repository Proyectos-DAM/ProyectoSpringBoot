package com.refugio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Controlador principal
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "redirect:/facturacion";
    }
}

