package ca.kpu.team4.smart_home_energy_management.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.model.Device;
import ca.kpu.team4.smart_home_energy_management.repository.DeviceRepository;
import ca.kpu.team4.smart_home_energy_management.repository.FamilymemberRepository;

@Service 
public class FamilymemberService {
    public record FamilyMemberOption(
        int value,
        String label,
        boolean canViewDevices,
        boolean canControlDevices,
        boolean canViewEnergy
    ) {}

    private final FamilymemberRepository repository;
    private final DeviceRepository deviceRepository;

    public FamilymemberService(FamilymemberRepository repository, DeviceRepository deviceRepository) {
        this.repository = repository;
        this.deviceRepository = deviceRepository;
    }

    public List<FamilyMemberOption> getFamilyMembersForOwner(String email) {
        return repository.findMembersForOwner(email).stream()
                .map(row -> new FamilyMemberOption(
                        row.getMemberId(),
                        row.getMemberName(),
                        row.getCanViewDevices() == 1,
                        row.getCanControlDevices() == 1,
                        row.getCanViewEnergy() == 1
                ))
                .toList();
    }


    @Transactional
    public void updatePermissions(int memberId, String email,
                                boolean canViewDevices, boolean canControlDevices,
                                boolean canViewEnergy) {
        if (canControlDevices && !canViewDevices) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Control devices requires View devices");
        }

        int updated = repository.updatePermissions(
                memberId, email, canViewDevices, canControlDevices, canViewEnergy);

        if (updated == 0 && repository.findMembersForOwner(email).stream()
                .noneMatch(row -> row.getMemberId() == memberId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found");
        }
    }

    public record DashboardHouse(
        int houseId, String houseName, String memberName,
        boolean canViewDevices, boolean canControlDevices, boolean canViewEnergy,
        List<Device> devices
) {}

    @Transactional(readOnly = true)
    public List<DashboardHouse> getFamilyDashboard(String email) {
        var houses = repository.findHouseAccessForFamily(email);
        var devicesByHouse = deviceRepository.findVisibleFamilyDevices(email).stream()
                .collect(Collectors.groupingBy(Device::getHouse_id));

        return houses.stream()
                .map(house -> new DashboardHouse(
                        house.getHouseId(),
                        house.getHouseName(),
                        house.getMemberName(),
                        house.getCanViewDevices() == 1,
                        house.getCanControlDevices() == 1,
                        house.getCanViewEnergy() == 1,
                        devicesByHouse.getOrDefault(house.getHouseId(), List.of())))
                .toList();
    }
}
