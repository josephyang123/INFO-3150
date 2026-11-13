package ca.kpu.team4.smart_home_energy_management.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.dto.DeviceType;
import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.model.DeviceUsage;
import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceRepository;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceUsageRepository;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;
import jakarta.transaction.Transactional;


@Service 
public class DeviceService {


    private final DeviceRepository deviceRepository;
    private final LoginRepository loginRepository;
    private final DeviceUsageRepository deviceUsageRepository;

    private void applyPowerState(Device device, boolean poweredState) {
        if (device.getPowered_state() == poweredState) {
            return;
        }

        LocalDateTime now = LocalDateTime.now(Clock.systemUTC());

        if (poweredState) {
            if (device.getPower_watts() != null) {
                DeviceUsage session = new DeviceUsage(
                        device.getDevice_id(),
                        device.getHouse_id(),
                        device.getPower_watts(),
                        now
                );
                deviceUsageRepository.save(session);
            }
        } else {
            closeOpenSession(device.getDevice_id(), now);
        }

        device.setPowered_state(poweredState);
    }

    private void closeOpenSession(int deviceId, LocalDateTime endedAt) {
        deviceUsageRepository.findOpenSession(deviceId)
                .ifPresent(session -> {
                    session.setEnd_time(endedAt);
                    deviceUsageRepository.save(session);
                });
    }

    public DeviceService(DeviceRepository deviceRepository,
         LoginRepository loginRepository,
         DeviceUsageRepository deviceUsageRepository){

        this.deviceRepository=deviceRepository;
        this.loginRepository=loginRepository;
        this.deviceUsageRepository=deviceUsageRepository;
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
    public void updatePowerState(int deviceId, boolean poweredState,String email) {
        Device device = deviceRepository.findOwnedDeviceForUpdate(deviceId, requiredUserId(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found"));

        applyPowerState(device, poweredState);
    }
    
    @Transactional
    public void deleteDevice(int deviceId, String email) {
        Device device = deviceRepository.findOwnedDeviceForUpdate(deviceId, requiredUserId(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found"));

        deviceRepository.delete(device);
    }

    @Transactional
    public void createDevice(String email, String deviceName,
                            DeviceType deviceType, String manufacturer, BigDecimal powerWatts) {
        if (deviceName == null || deviceName.isBlank()
                || manufacturer == null || manufacturer.isBlank()
                || deviceType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Device name, type, and manufacturer are required");
        }
        if (powerWatts == null
            || powerWatts.signum() <= 0
            || powerWatts.compareTo(new BigDecimal("999999.99")) > 0
            || powerWatts.stripTrailingZeros().scale() > 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Power draw must be between 0.01 and 999999.99 W with up to two decimal places");
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
                false,
                powerWatts
        ));
    }

    @Transactional
    public void updatePowerStateForFamily(int deviceId, String email, boolean poweredState) {
        Device device = deviceRepository.findControllableFamilyDevice(deviceId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device unavailable"));
        applyPowerState(device, poweredState);
    }

    

}
