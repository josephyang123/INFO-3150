package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.controller.DeviceController.DeviceTypeOption;
import ca.kpu.team4.smart_home_energy_management.dto.DeviceType;
import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.service.DeviceService;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.PutMapping;
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
    @RequestBody PowerStateRequest request, Authentication authentication) {
        if (request.powered_state() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "powered_state is required");
            }
            
            deviceService.updatePowerState(deviceId, request.powered_state(),authentication.getName());
        }
        
    @DeleteMapping("/api/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDevice(@PathVariable("deviceId") int deviceId, Authentication authentication) {
        deviceService.deleteDevice(deviceId, authentication.getName());
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
        String manufacturer,
        BigDecimal power_watts
    ){}

    @PostMapping("/api/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public void createDevice(@RequestBody CreateDeviceRequest request,
                            Authentication authentication) {
        deviceService.createDevice(
                authentication.getName(),
                request.device_name(),
                request.device_type(),
                request.manufacturer(),
                request.power_watts()
        );
    }

    @PatchMapping("/api/family/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateFamilyDevicePower(
            @PathVariable("deviceId") int deviceId,
            @RequestBody PowerStateRequest request,
            Authentication authentication) {
        if (request.powered_state() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "powered_state is required");
        }

        deviceService.updatePowerStateForFamily(
                deviceId, authentication.getName(), request.powered_state());
    }

    public record AutomationRequest(
        String automation,
        String automation_comparison,
        BigDecimal automation_threshold_kwh) {}

    @PutMapping("/api/devices/{deviceId}/automation")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveAutomation(
            @PathVariable("deviceId") int deviceId,
            @RequestBody AutomationRequest request,
            Authentication authentication) {

        deviceService.saveAutomation(
                deviceId,
                authentication.getName(),
                request.automation(),
                request.automation_comparison(),
                request.automation_threshold_kwh());
    }
        
        
        
}
