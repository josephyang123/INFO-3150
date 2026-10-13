package ca.kpu.team4.smart_home_energy_management.service;

import java.util.List;

import org.springframework.stereotype.Service;

import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceRepository;


@Service 
public class DeviceService {


    private final DeviceRepository deviceRepository;
    

    public DeviceService(DeviceRepository deviceRepository){
        this.deviceRepository=deviceRepository;
    }

    public List<Device> getAllDevices(){

        return deviceRepository.findAll();
    }
    
}
