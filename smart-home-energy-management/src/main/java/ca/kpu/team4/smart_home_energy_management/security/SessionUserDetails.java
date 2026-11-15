package ca.kpu.team4.smart_home_energy_management.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class SessionUserDetails extends User {
    private static final long serialVersionUID = 1L;
    private final long sessionVersion;

    public SessionUserDetails(String email, String password, String role, long sessionVersion) {
        super(email, password, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.sessionVersion = sessionVersion;
    }

    public long getSessionVersion() {
        return sessionVersion;
    }
}
