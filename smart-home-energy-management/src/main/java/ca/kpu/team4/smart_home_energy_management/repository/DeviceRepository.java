package ca.kpu.team4.smart_home_energy_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ca.kpu.team4.smart_home_energy_management.model.Device;

public interface DeviceRepository 
        extends JpaRepository<Device,Integer> {
    
}
