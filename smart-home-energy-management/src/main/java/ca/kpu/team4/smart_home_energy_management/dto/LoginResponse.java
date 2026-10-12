package ca.kpu.team4.smart_home_energy_management.dto;

public record LoginResponse(
    boolean ok,
    String message,
    String role) {
    
}
