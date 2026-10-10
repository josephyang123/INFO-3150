package ca.kpu.team4.smart_home_energy_management.service;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ca.kpu.team4.smart_home_energy_management.repository.RegisterRepository;
import ca.kpu.team4.smart_home_energy_management.dto.RegisterRequest;
import ca.kpu.team4.smart_home_energy_management.model.User;

@Service 
public class RegisterService {

    private final RegisterRepository registerRepository;
    private final PasswordEncoder passwordEncoder;


    public RegisterService(
        RegisterRepository registerRepository, 
        PasswordEncoder passwordEncoder) {

        this.registerRepository = registerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request){
        if(request.username().isBlank() || request.email().isBlank() || request.password().isBlank()){
            throw new ResponseStatusException(400, "All fields are required", null);
        }

        if(request.password().length() < 8){
            throw new ResponseStatusException(400, "Password must be at least 8 characters long", null);
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = new User( request.username(), request.email(), hashedPassword);
        registerRepository.save(user);
    }
    
}
