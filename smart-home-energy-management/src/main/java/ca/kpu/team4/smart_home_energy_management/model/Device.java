package ca.kpu.team4.smart_home_energy_management.model;

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
    
    public int getDevice_id() {
        return device_id;
    }
    public void setDevice_id(int device_id) {
        this.device_id = device_id;
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

    
}
