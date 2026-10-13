package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.service.DeviceService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
public class DeviceController {

    private final DeviceService deviceService;
    
    public DeviceController(DeviceService deviceService){
        this.deviceService=deviceService;
    }


    @GetMapping("/api/devices")
    public List<Device> getDevices() {
        return deviceService.getAllDevices();
    }
    

    
}
