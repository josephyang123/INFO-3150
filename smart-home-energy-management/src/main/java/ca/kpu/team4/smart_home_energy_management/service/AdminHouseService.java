package ca.kpu.team4.smart_home_energy_management.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ca.kpu.team4.smart_home_energy_management.model.House;
import ca.kpu.team4.smart_home_energy_management.model.User;
import ca.kpu.team4.smart_home_energy_management.repository.FamilymemberRepository;
import ca.kpu.team4.smart_home_energy_management.repository.HouseRepository;
import ca.kpu.team4.smart_home_energy_management.repository.LoginRepository;

@Service
public class AdminHouseService {
    public record AdminPerson(int userId, String name, String email) {}
    public record AdminHouse(int houseId, String name, AdminPerson homeowner, List<AdminPerson> members) {}

    private final HouseRepository houses;
    private final LoginRepository users;
    private final FamilymemberRepository familyMembers;

    public AdminHouseService(HouseRepository houses, LoginRepository users,
            FamilymemberRepository familyMembers) {
        this.houses = houses;
        this.users = users;
        this.familyMembers = familyMembers;
    }

    @Transactional(readOnly = true)
    public List<AdminHouse> getHouses() {
        Map<Integer, AdminHouse> result = new LinkedHashMap<>();
        for (var row : houses.findAdminHouses()) {
            AdminHouse house = result.computeIfAbsent(row.getHouseId(), id -> new AdminHouse(
                    id, row.getHouseName(),
                    new AdminPerson(row.getOwnerId(), row.getOwnerName(), row.getOwnerEmail()),
                    new ArrayList<>()));
            if (row.getMemberId() != null) {
                house.members().add(new AdminPerson(row.getMemberId(), row.getMemberName(), row.getMemberEmail()));
            }
        }
        return List.copyOf(result.values());
    }

    @Transactional(readOnly = true)
    public List<AdminPerson> getUnassignedAccounts() {
        return users.findUnassignedFamilyAccounts().stream()
                .map(row -> new AdminPerson(row.getUserId(), row.getName(), row.getEmail()))
                .toList();
    }

    @Transactional
    public int createHouse(String name, Integer homeownerId) {
        if (name == null || name.isBlank() || name.trim().length() > 254
                || homeownerId == null || homeownerId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A house name and homeowner are required");
        }

        User user = lockUnassignedFamily(homeownerId);
        House house = houses.save(new House(homeownerId, name.trim()));
        user.setAccount_role("HOMEOWNER");
        return house.getId();
    }

    @Transactional
    public void addFamilyMember(int houseId, Integer userId) {
        validateMemberIds(houseId, userId);
        lockUnassignedFamily(userId);
        requireHouse(houseId);
        if (familyMembers.addMember(houseId, userId) != 1) {
            throw new IllegalStateException("Could not add family member");
        }
    }

    @Transactional
    public void removeFamilyMember(int houseId, int userId) {
        validateMemberIds(houseId, userId);
        User user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Family account not found"));
        requireHouse(houseId);
        if (!"FAMILY".equals(user.getAccount_role()) || houses.existsByOwnerId(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only family members can be removed");
        }
        if (familyMembers.removeMember(houseId, userId) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Family member not found in this house");
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void transferOwnership(int houseId, Integer newOwnerId) {
        validateMemberIds(houseId, newOwnerId);
        int oldOwnerId = houses.findOwnerId(houseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "House not found"));
        if (oldOwnerId == newOwnerId) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already owns this house");
        }

        User first = lockAccount(Math.min(oldOwnerId, newOwnerId));
        User second = lockAccount(Math.max(oldOwnerId, newOwnerId));
        User oldOwner = oldOwnerId < newOwnerId ? first : second;
        User newOwner = oldOwnerId < newOwnerId ? second : first;
        House house = houses.findByIdForUpdate(houseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "House not found"));
        if (house.getOwnerId() != oldOwnerId) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "House owner changed; try again");
        }
        if (!"HOMEOWNER".equals(oldOwner.getAccount_role())
                || houses.countByOwnerId(oldOwnerId) != 1
                || familyMembers.countForUser(oldOwnerId) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Current owner has inconsistent house assignments");
        }

        List<Integer> memberships = familyMembers.findHouseIdsForUser(newOwnerId);
        if (!"FAMILY".equals(newOwner.getAccount_role())
                || houses.existsByOwnerId(newOwnerId)
                || memberships.size() > 1
                || (!memberships.isEmpty() && memberships.get(0) != houseId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "New owner must be unassigned or a member of this house");
        }
        if (!memberships.isEmpty() && familyMembers.removeMember(houseId, newOwnerId) != 1) {
            throw new IllegalStateException("Could not remove new owner's family membership");
        }
        if (familyMembers.addMember(houseId, oldOwnerId) != 1) {
            throw new IllegalStateException("Could not add previous owner as a family member");
        }
        house.setOwnerId(newOwnerId);
        oldOwner.setAccount_role("FAMILY");
        newOwner.setAccount_role("HOMEOWNER");
    }

    private User lockAccount(int userId) {
        return users.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private User lockUnassignedFamily(int userId) {
        User user = users.findByIdForUpdate(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account not found"));
        if (!"FAMILY".equals(user.getAccount_role())
                || houses.existsByOwnerId(userId)
                || familyMembers.countForUser(userId) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Account must be an unassigned family account");
        }
        return user;
    }

    private void validateMemberIds(int houseId, Integer userId) {
        if (houseId <= 0 || userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "House and account are required");
        }
    }

    private void requireHouse(int houseId) {
        if (!houses.existsById(houseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "House not found");
        }
    }
}
