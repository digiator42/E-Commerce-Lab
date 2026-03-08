package com.ecommerce.lab.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping(value = "{path:[^\\.]*}")
    public String index() { return "index"; }
}