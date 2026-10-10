package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.dto.RegisterRequest;
import ca.kpu.team4.smart_home_energy_management.service.RegisterService;

@RestController
public class RegisterController {

    private final RegisterService registerService;

    public RegisterController(RegisterService registerService) {
        this.registerService = registerService;
    }
    
    @PostMapping ("/api/register")
    public String register(@RequestBody RegisterRequest request) {
        registerService.register(request);
        return "User registered successfully";
    }
}
