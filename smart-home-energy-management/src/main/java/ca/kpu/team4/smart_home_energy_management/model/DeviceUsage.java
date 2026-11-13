package ca.kpu.team4.smart_home_energy_management.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "device_usage_sessions")
public class DeviceUsage {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "session_id", nullable = false, unique = true)
    private int session_id;

    @Column (name = "device_id", nullable = false)
    private int device_id;
    @Column (name = "house_id", nullable = false)
    private int house_id;
    @Column (name = "power_watts", nullable = false)
    private BigDecimal power_watts;
    @Column (name = "started_at", nullable = false)
    private LocalDateTime start_time;
    @Column (name = "ended_at", nullable = true)
    private LocalDateTime end_time;



    public int getSession_id() {
        return session_id;
    }

    public void setSession_id(int session_id) {
        this.session_id = session_id;
    }

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
    public BigDecimal getPower_watts() {
        return power_watts;
    }
    public void setPower_watts(BigDecimal power_watts) {
        this.power_watts = power_watts;
    }
    public LocalDateTime getStart_time() {
        return start_time;
    }
    public void setStart_time(LocalDateTime start_time) {
        this.start_time = start_time;
    }
    public LocalDateTime getEnd_time() {
        return end_time;
    }
    public void setEnd_time(LocalDateTime end_time) {
        this.end_time = end_time;
    }

    public DeviceUsage() {
        // Default constructor for JPA
    }

    public DeviceUsage(int device_id, 
            int house_id, 
            BigDecimal power_watts, 
            LocalDateTime start_time) {
        this.device_id = device_id;
        this.house_id = house_id;
        this.power_watts = power_watts;
        this.start_time = start_time;
        
    }
}
