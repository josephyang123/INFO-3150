package ca.kpu.team4.smart_home_energy_management.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.model.DeviceUsage;
import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceUsageRepository;
import ca.kpu.team4.smart_home_energy_management.repository.FamilymemberRepository;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;

@Service 
public class EnergyService {

    private static final ZoneId  VANCOUVER = ZoneId.of("America/Vancouver");
    private final DeviceUsageRepository deviceUsageRepository;
    private final LoginRepository loginRepository;
    private final FamilymemberRepository familymemberRepository;



    public EnergyService(LoginRepository loginRepository, 
            DeviceUsageRepository deviceUsageRepository,
            FamilymemberRepository familymemberRepository) {
        this.loginRepository = loginRepository;
        this.deviceUsageRepository = deviceUsageRepository;
        this.familymemberRepository = familymemberRepository;
    }

    private int requiredUserId(String email) {
        User user = loginRepository.findByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User not found: " + email);
        }
        return user.getUser_id();
    }

    public List<DailyEnergy> getDailyEnergy(String email) {
        return getDailyEnergy(email, null);
    }

    public List<DailyEnergy> getFamilyDailyEnergy(String email, int houseId) {
        boolean canViewEnergy = familymemberRepository.findHouseAccessForFamily(email)
                .stream()
                .anyMatch(house -> house.getHouseId() == houseId
                        && house.getCanViewEnergy() == 1);

        if (!canViewEnergy) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return getDailyEnergy(email, houseId);
    }

    public List<DailyEnergy> getDailyEnergy(String email, Integer houseId) {
        Instant currentTime = Instant.now();
        LocalDateTime nowUtc = LocalDateTime.ofInstant(currentTime, ZoneOffset.UTC);
        LocalDate today = currentTime.atZone(VANCOUVER).toLocalDate();
        LocalDate firstDate = today.minusDays(6);
        LocalDateTime windowStart = midnightUtc(firstDate);

        List<DeviceUsage> sessions = houseId == null
            ? deviceUsageRepository.findSessionsOverlappingWindow(
                    requiredUserId(email), windowStart, nowUtc)
            : deviceUsageRepository.findHouseSessionsOverlappingWindow(
                    houseId, windowStart, nowUtc);

        List<DailyEnergy> totals = new ArrayList<>();

        for (int day = 0; day < 7; day++) {
            LocalDate date = firstDate.plusDays(day);
            LocalDateTime dayStart = midnightUtc(date);
            LocalDateTime dayEnd = date.equals(today)
                    ? nowUtc
                    : midnightUtc(date.plusDays(1));

            double totalKwh = 0.0;

            for (DeviceUsage session : sessions) {
                LocalDateTime sessionStart = session.getStart_time();
                LocalDateTime sessionEnd = session.getEnd_time();
                if (sessionEnd == null) {
                    sessionEnd = nowUtc;
                }

                LocalDateTime overlapStart = sessionStart.isAfter(dayStart)
                        ? sessionStart : dayStart;
                LocalDateTime overlapEnd = sessionEnd.isBefore(dayEnd)
                        ? sessionEnd : dayEnd;

                if (overlapEnd.isAfter(overlapStart)) {
                    double hours = Duration.between(overlapStart, overlapEnd)
                            .toMillis() / 3_600_000.0;
                    totalKwh += session.getPower_watts().doubleValue() * hours / 1000.0;
                }
            }

            totals.add(new DailyEnergy(date, totalKwh));
        }

        return totals;
    }



    public double getTodayKwhForHouse(int houseId) {
        // Capture the current time.
        Instant currentTime = Instant.now();

        // Usage session timestamps are stored in UTC.
        LocalDateTime nowUtc =
                LocalDateTime.ofInstant(currentTime, ZoneOffset.UTC);

        // Decide which day it is in Vancouver.
        LocalDate today =
                currentTime.atZone(VANCOUVER).toLocalDate();

        // Convert today's Vancouver midnight into UTC.
        LocalDateTime dayStart = midnightUtc(today);

        // Get usage sessions that overlap today for this house.
        List<DeviceUsage> sessions =
                deviceUsageRepository.findHouseSessionsOverlappingWindow(
                        houseId, dayStart, nowUtc);

        double totalKwh = 0.0;

        for (DeviceUsage session : sessions) {
            LocalDateTime sessionStart = session.getStart_time();
            LocalDateTime sessionEnd = session.getEnd_time();

            // No end time means the device is still running.
            if (sessionEnd == null) {
                sessionEnd = nowUtc;
            }

            // Only count the portion of this session inside today.
            LocalDateTime overlapStart =
                    sessionStart.isAfter(dayStart)
                            ? sessionStart : dayStart;

            LocalDateTime overlapEnd =
                    sessionEnd.isBefore(nowUtc)
                            ? sessionEnd : nowUtc;

            if (overlapEnd.isAfter(overlapStart)) {
                double hours =
                        Duration.between(overlapStart, overlapEnd)
                                .toMillis() / 3_600_000.0;

                totalKwh +=
                        session.getPower_watts().doubleValue()
                                * hours / 1000.0;
            }
        }

        return totalKwh;
    }

    private static LocalDateTime midnightUtc(LocalDate date) {
            return date.atStartOfDay(VANCOUVER)
                    .withZoneSameInstant(ZoneOffset.UTC)
                    .toLocalDateTime();
    }

    public record DailyEnergy(LocalDate date, double kwh) {}
    
}
