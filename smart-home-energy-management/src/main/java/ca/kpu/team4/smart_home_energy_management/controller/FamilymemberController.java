package ca.kpu.team4.smart_home_energy_management.controller;

import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.service.FamilymemberService;
import ca.kpu.team4.smart_home_energy_management.service.FamilymemberService.DashboardHouse;
import ca.kpu.team4.smart_home_energy_management.service.FamilymemberService.FamilyMemberOption;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;





@RestController 
public class FamilymemberController {
    
    private final FamilymemberService familymemberService;
    
    public FamilymemberController(FamilymemberService familymemberService){
        this.familymemberService=familymemberService;
    }
    
    
    @GetMapping("/api/family-members")
    public List<FamilyMemberOption> getFamilyMembers(Authentication authentication) {
        return familymemberService.getFamilyMembersForOwner(authentication.getName());
    }
    

    public record UpdatePermissionsRequest(
        Boolean canViewDevices,
        Boolean canControlDevices,
        Boolean canViewEnergy
    ) {}

    @PutMapping("/api/family-members/{memberId}/permissions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePermissions(
            @PathVariable("memberId") int memberId,
            @Valid @RequestBody UpdatePermissionsRequest request,
            Authentication authentication) {
        familymemberService.updatePermissions(
                memberId,
                authentication.getName(),
                request.canViewDevices(),
                request.canControlDevices(),
                request.canViewEnergy());
    }
    
    @GetMapping("/api/family/dashboard")
    public List<DashboardHouse> getFamilyDashboard(Authentication authentication) {
        return familymemberService.getFamilyDashboard(authentication.getName());
    }
}
