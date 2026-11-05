package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.controller.DeviceController.DeviceTypeOption;
import ca.kpu.team4.smart_home_energy_management.dto.DeviceType;
import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.service.DeviceService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;


@RestController 
public class DeviceController {

    private final DeviceService deviceService;
    
    public DeviceController(DeviceService deviceService){
        this.deviceService=deviceService;
    }
    
    
    @GetMapping("/api/devices")
    public List<Device> getDevices(Authentication authentication) {
        return deviceService.getDevicesForOwner(authentication.getName());
    }
    
    
    public record PowerStateRequest(Boolean powered_state) {}
    @PatchMapping("/api/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePowerState(@PathVariable("deviceId") int deviceId,
    @RequestBody PowerStateRequest request) {
        if (request.powered_state() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "powered_state is required");
            }
            
            deviceService.updatePowerState(deviceId, request.powered_state());
        }
        
        @DeleteMapping("/api/devices/{deviceId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void deleteDevice(@PathVariable("deviceId") int deviceId) {
            deviceService.deleteDevice(deviceId);
        }
        
        
        public record DeviceTypeOption(String value, String label) {}
        @GetMapping("/api/device-types")
        public List<DeviceTypeOption> getDeviceTypes() {
            return Arrays.stream(DeviceType.values())
            .map(type -> new DeviceTypeOption(type.name(), type.getLabel()))
            .toList();
        }   

        public record CreateDeviceRequest(
            String device_name,
            DeviceType device_type,
            String manufacturer
        ){}
        @PostMapping("/api/devices")
        @ResponseStatus(HttpStatus.CREATED)
        public void createDevice(@RequestBody CreateDeviceRequest request,
                                Authentication authentication) {
            deviceService.createDevice(
                    authentication.getName(),
                    request.device_name(),
                    request.device_type(),
                    request.manufacturer());
}

        
        
        
        
}
