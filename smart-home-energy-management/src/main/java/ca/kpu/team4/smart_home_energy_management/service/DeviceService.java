package ca.kpu.team4.smart_home_energy_management.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.dto.DeviceType;
import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceRepository;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;
import jakarta.transaction.Transactional;


@Service 
public class DeviceService {


    private final DeviceRepository deviceRepository;
    private final LoginRepository loginRepository;

    public DeviceService(DeviceRepository deviceRepository, LoginRepository loginRepository){
        this.deviceRepository=deviceRepository;
        this.loginRepository=loginRepository;
    }

    
    public List<Device> getDevicesForOwner(String email) {

        return deviceRepository.findOwnedDevices(requiredUserId(email));
    }
    
    private int requiredUserId(String email) {
        User user = loginRepository.findByEmail(email);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Signed-in account not found");
        }
        return user.getUser_id();
    }
    @Transactional
    public void updatePowerState(int deviceId, boolean poweredState) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found"));

        device.setPowered_state(poweredState);
    }
    
    @Transactional
    public void deleteDevice(int deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found"));

        deviceRepository.delete(device);
    }

    @Transactional
    public void createDevice(String email, String deviceName,
                            DeviceType deviceType, String manufacturer) {
        if (deviceName == null || deviceName.isBlank()
                || manufacturer == null || manufacturer.isBlank()
                || deviceType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Device name, type, and manufacturer are required");
        }

        int userId = requiredUserId(email);
        List<Integer> houseIds = deviceRepository.findOwnedHouseIds(userId);

        if (houseIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Create a house before adding a device");
        }

        if (houseIds.size() > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Select a house before adding a device");
        }

        deviceRepository.save(new Device(
                houseIds.get(0),
                deviceName.trim(),
                deviceType.name(),
                manufacturer.trim(),
                false));
    }

    @Transactional
    public void updatePowerStateForFamily(int deviceId, String email, boolean poweredState) {
        Device device = deviceRepository.findControllableFamilyDevice(deviceId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device unavailable"));
        device.setPowered_state(poweredState);
    }

}
