package com.example.fileServer;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FileServerController {
    
    @GetMapping("/")
    public String index() {
        return "Zdarova!";
    }
    
}
