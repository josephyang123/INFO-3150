package ca.kpu.team4.smart_home_energy_management.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.kpu.team4.smart_home_energy_management.model.User;

public interface RegisterRepository 
        extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
}
