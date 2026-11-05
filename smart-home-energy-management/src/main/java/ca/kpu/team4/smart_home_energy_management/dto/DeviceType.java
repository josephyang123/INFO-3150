package ca.kpu.team4.smart_home_energy_management.dto;

public enum DeviceType {
    THERMOSTAT("Thermostat"),
    SPEAKER("Speaker"),
    DOORBELL("Doorbell"),
    LOCK("Lock"),
    FRIDGE("Fridge"),
    OVEN("Oven"),
    LIGHT("Light"),
    TV("TV"),
    KEYCHAIN("Keychain"),
    OTHER("Other");

    private final String label;
    DeviceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    
}
