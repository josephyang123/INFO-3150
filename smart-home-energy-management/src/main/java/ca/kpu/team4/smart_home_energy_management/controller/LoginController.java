package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.dto.LoginRequest;
import ca.kpu.team4.smart_home_energy_management.dto.LoginResponse;
import ca.kpu.team4.smart_home_energy_management.service.LoginService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
public class LoginController {
    
    private final LoginService loginService;
    public LoginController(LoginService loginService) {
        // Constructor
        this.loginService = loginService;
    }



    @PostMapping("/api/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        //TODO: process POST request

        ResponseEntity<LoginResponse> authenticated = loginService.authenticate(request);
        System.out.println("Authenticated: " + authenticated);
        return authenticated;
    }
    
}
