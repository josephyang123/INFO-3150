package ca.kpu.team4.smart_home_energy_management.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import ca.kpu.team4.smart_home_energy_management.service.EnergyService;

@RestController 
public class EnergyController {
    

     private final EnergyService energyService;

    public EnergyController(EnergyService energyService) {
        this.energyService = energyService;
    }

    @GetMapping("/api/energy/daily")
    public List<EnergyService.DailyEnergy> getDailyEnergy(
            Authentication authentication) {
        return energyService.getDailyEnergy(authentication.getName());
    }

    @GetMapping("/api/family/houses/{houseId}/energy/daily")
    public List<EnergyService.DailyEnergy> getFamilyDailyEnergy(
            @PathVariable("houseId") int houseId,
            Authentication authentication) {
        return energyService.getFamilyDailyEnergy(authentication.getName(), houseId);
    }
}
