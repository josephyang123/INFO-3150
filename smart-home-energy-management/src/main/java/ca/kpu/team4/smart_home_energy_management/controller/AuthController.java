package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.dto.LoginResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
public class AuthController {
    

    @GetMapping("api/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @GetMapping("/api/me")
    public LoginResponse currentUser(Authentication authentication) {

        String role = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .findFirst()
                .orElse(null);

        return new LoginResponse(
                true,
                "Authenticated",
                role
        );
    }
    
    
}
