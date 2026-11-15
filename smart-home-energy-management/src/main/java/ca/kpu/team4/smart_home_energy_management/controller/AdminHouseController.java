package ca.kpu.team4.smart_home_energy_management.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.service.AdminHouseService;
import ca.kpu.team4.smart_home_energy_management.service.AdminHouseService.AdminHouse;
import ca.kpu.team4.smart_home_energy_management.service.AdminHouseService.AdminPerson;

@RestController
@RequestMapping("/api/admin/houses")
public class AdminHouseController {
    private final AdminHouseService service;

    public AdminHouseController(AdminHouseService service) {
        this.service = service;
    }

    public record CreateHouseRequest(String name, Integer homeownerId) {}
    public record CreateHouseResponse(int houseId) {}
    public record AddFamilyMemberRequest(Integer userId) {}
    public record TransferOwnershipRequest(Integer userId) {}

    @GetMapping
    public List<AdminHouse> getHouses() {
        return service.getHouses();
    }

    @GetMapping("/unassigned-accounts")
    public List<AdminPerson> getUnassignedAccounts() {
        return service.getUnassignedAccounts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateHouseResponse createHouse(@RequestBody CreateHouseRequest request) {
        return new CreateHouseResponse(service.createHouse(request.name(), request.homeownerId()));
    }

    @PostMapping("/{houseId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public void addFamilyMember(@PathVariable("houseId") int houseId,
            @RequestBody AddFamilyMemberRequest request) {
        service.addFamilyMember(houseId, request.userId());
    }

    @DeleteMapping("/{houseId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFamilyMember(@PathVariable("houseId") int houseId,
            @PathVariable("userId") int userId) {
        service.removeFamilyMember(houseId, userId);
    }

    @PostMapping("/{houseId}/owner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void transferOwnership(@PathVariable("houseId") int houseId,
            @RequestBody TransferOwnershipRequest request) {
        service.transferOwnership(houseId, request.userId());
    }
}
