package ca.kpu.team4.smart_home_energy_management.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
    private final EnergyService energyService;

    private static final ZoneId VANCOUVER = ZoneId.of("America/Vancouver");
    
    public DeviceService(DeviceRepository deviceRepository,
         LoginRepository loginRepository,
         DeviceUsageRepository deviceUsageRepository,
         EnergyService energyService){
    
        this.deviceRepository=deviceRepository;
        this.loginRepository=loginRepository;
        this.deviceUsageRepository=deviceUsageRepository;
        this.energyService=energyService;
    }

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

    @Transactional
    public void saveAutomation(
            int deviceId,
            String email,
            String action,
            String comparison,
            BigDecimal thresholdKwh) {

        // Only the device's homeowner may change its rule.
        Device device = deviceRepository.findOwnedDeviceForUpdate(
                deviceId, requiredUserId(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Device not found"));

        boolean validAction =
                "TURN_ON".equals(action)
                || "TURN_OFF".equals(action)
                || "DO_NOTHING".equals(action);

        if (!validAction) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid automation action");
        }

        if ("DO_NOTHING".equals(action)) {
            comparison = null;
            thresholdKwh = null;
        } else {
            if (!"GT".equals(comparison) && !"LT".equals(comparison)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Choose above or below");
            }

            if (thresholdKwh == null
                    || thresholdKwh.signum() < 0
                    || thresholdKwh.compareTo(
                            new BigDecimal("999999.9999")) > 0
                    || thresholdKwh.stripTrailingZeros().scale() > 4) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Threshold must be between 0 and 999999.9999 kWh"
                                + " with up to four decimal places");
            }
        }

        device.setAutomation(action);
        device.setAutomation_comparison(comparison);
        device.setAutomation_threshold_kwh(thresholdKwh);

        // Saving the rule allows it to fire again.
        device.setAutomation_last_triggered_date(null);
    }

    @Transactional
    public void evaluateAutomation(int deviceId) {
        Device device = deviceRepository.findDeviceForUpdate(deviceId)
                .orElse(null);

        // It may have been deleted since the scheduler fetched its ID.
        if (device == null) {
            return;
        }

        String action = device.getAutomation();

        // Recheck because the homeowner may have disabled the rule.
        if (!"TURN_ON".equals(action) && !"TURN_OFF".equals(action)) {
            return;
        }

        BigDecimal threshold = device.getAutomation_threshold_kwh();
        String comparison = device.getAutomation_comparison();

        if (threshold == null
                || (!"GT".equals(comparison) && !"LT".equals(comparison))) {
            return;
        }

        LocalDate today = LocalDate.now(VANCOUVER);

        if (today.equals(device.getAutomation_last_triggered_date())) {
            return;
        }

        double todayKwh =
                energyService.getTodayKwhForHouse(device.getHouse_id());

        // If midnight passed during the calculation, retry next time.
        if (!today.equals(LocalDate.now(VANCOUVER))) {
            return;
        }

        int result = BigDecimal.valueOf(todayKwh).compareTo(threshold);

        boolean conditionMet =
                ("GT".equals(comparison) && result > 0)
                || ("LT".equals(comparison) && result < 0);

        if (!conditionMet) {
            return;
        }

        // Reuse your existing usage-session handling.
        applyPowerState(device, "TURN_ON".equals(action));

        device.setAutomation_last_triggered_date(today);
    }

}
