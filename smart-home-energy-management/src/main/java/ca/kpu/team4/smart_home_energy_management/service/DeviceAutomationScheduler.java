package ca.kpu.team4.smart_home_energy_management.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ca.kpu.team4.smart_home_energy_management.repository.DeviceRepository;

@Component
public class DeviceAutomationScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(DeviceAutomationScheduler.class);

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    public DeviceAutomationScheduler(
            DeviceRepository deviceRepository,
            DeviceService deviceService) {

        this.deviceRepository = deviceRepository;
        this.deviceService = deviceService;
    }

    @Scheduled(initialDelay = 30_000, fixedDelay = 30_000)
    public void checkAutomationRules() {
        for (int deviceId : deviceRepository.findAutomationDeviceIds()) {
            try {
                deviceService.evaluateAutomation(deviceId);
            } catch (RuntimeException exception) {
                log.error(
                        "Could not evaluate automation for device {}",
                        deviceId,
                        exception);
            }
        }
    }
}