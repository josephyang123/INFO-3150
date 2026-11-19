package ca.kpu.team4.smart_home_energy_management.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity 
@Table(name = "smart_devices")
public class Device {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "device_id", nullable = false, unique = true)
    private int device_id;

    @Column (name = "house_id", nullable = false)
    private int house_id;
    
    @Column (name = "device_name", nullable = false)
    private String device_name;
    
    @Column (name = "device_type", nullable = false)
    private String device_type;
    @Column (name = "manufacturer", nullable = false)
    private String manufacturer;
    @Column (name = "powered_state", nullable = false)
    private boolean powered_state;
    @Column (name = "power_watts", nullable = false, precision = 8, scale = 2)
    private BigDecimal power_watts;
    @Column (name = "date_added", nullable = false, insertable = false, updatable = false)
    private String date_added;
    @Column (name = "automation", nullable = false)
    private String automation = "DO_NOTHING";
    @Column(name = "automation_threshold_kwh", precision = 10, scale = 4)
    private BigDecimal automation_threshold_kwh;

    @Column(name = "automation_comparison")
    private String automation_comparison;

    @Column(name = "automation_last_triggered_date")
    private LocalDate automation_last_triggered_date;

    
    public int getDevice_id() {
        return device_id;
    }
    public void setDevice_id(int device_id) {
        this.device_id = device_id;
    }
    public int getHouse_id() {
        return house_id;
    }
    public void setHouse_id(int house_id) {
        this.house_id = house_id;
    }

    public String getDevice_name() {
        return device_name;
    }
    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
    public String getDevice_type() {
        return device_type;
    }
    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }
    public String getManufacturer() {
        return manufacturer;
    }
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public boolean getPowered_state() {
        return powered_state;
    }

    public void setPowered_state(boolean powered_state) {
        this.powered_state = powered_state;
    }

    public BigDecimal getPower_watts() {
        return power_watts;
    }

    public void setPower_watts(BigDecimal power_watts) {
        this.power_watts = power_watts;
    }

    public String getDate_added() {
        return date_added;
    }

    public String getAutomation() {
        return automation;
    }

    public void setAutomation(String automation) {
        this.automation = automation;
    }

    public BigDecimal getAutomation_threshold_kwh() {
        return automation_threshold_kwh;
    }

    public void setAutomation_threshold_kwh(BigDecimal value) {
        this.automation_threshold_kwh = value;
    }

    public String getAutomation_comparison() {
        return automation_comparison;
    }

    public void setAutomation_comparison(String value) {
        this.automation_comparison = value;
    }

    public LocalDate getAutomation_last_triggered_date() {
        return automation_last_triggered_date;
    }

    public void setAutomation_last_triggered_date(LocalDate value) {
        this.automation_last_triggered_date = value;
    }


    public Device() {
        // Default constructor for JPA
    }
    public Device(int house_id, String device_name, String device_type, String manufacturer, boolean powered_state, BigDecimal power_watts) {
        this.house_id = house_id;
        this.device_name = device_name;
        this.device_type = device_type;
        this.manufacturer = manufacturer;
        this.powered_state = powered_state;
        this.power_watts = power_watts;
    }

    
}
