package ca.kpu.team4.smart_home_energy_management.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import ca.kpu.team4.smart_home_energy_management.dto.LoginRequest;
import ca.kpu.team4.smart_home_energy_management.dto.LoginResponse;
import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;

@Service 
public class LoginService {
    
    private final LoginRepository loginRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(LoginRepository loginRepository, PasswordEncoder passwordEncoder) {
        this.loginRepository = loginRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<LoginResponse> authenticate(LoginRequest request) {
        User user = loginRepository.findByEmail(request.email());
        if (user == null) {
            return ResponseEntity.badRequest().body(new LoginResponse(false, "Invalid email or password", null));
        }

        if (passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(200).body(new LoginResponse(true, "Login successful", user.getAccount_role()));
        } else {
            return ResponseEntity.status(400).body(new LoginResponse(false, "Invalid email or password", null));
        }
    }
}
