package app.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class AuthController {
    

    @PostMapping("/login")
    public String login(@RequestBody String request) {
        //TODO: process POST request
        return request;
    }
    
}
