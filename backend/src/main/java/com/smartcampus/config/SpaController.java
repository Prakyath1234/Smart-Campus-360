package com.smartcampus.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/login",
        "/register",
        "/student",
        "/student/**",
        "/faculty",
        "/faculty/**",
        "/admin",
        "/admin/**",
        "/security",
        "/security/**",
        "/tools",
        "/tools/**",
        "/login.html",
        "/register.html",
        "/student/dashboard.html",
        "/faculty/dashboard.html",
        "/admin/dashboard.html",
        "/security/dashboard.html"
    })
    public String forwardSpa() {
        return "forward:/index.html";
    }
}