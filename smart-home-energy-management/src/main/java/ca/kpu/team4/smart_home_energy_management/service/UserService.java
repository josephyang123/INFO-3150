package ca.kpu.team4.smart_home_energy_management.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;
import ca.kpu.team4.smart_home_energy_management.repository.RegisterRepository;

@Service 
public class UserService implements UserDetailsService {

    private final LoginRepository loginRepository;

    public UserService(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }


    @Override 
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = loginRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        return org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password(user.getPasswordHash())
                .roles(user.getAccount_role()) 
                .build();
        
        
    }
    
    
}
