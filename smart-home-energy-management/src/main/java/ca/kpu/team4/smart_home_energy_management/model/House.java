package ca.kpu.team4.smart_home_energy_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "houses")
public class House {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_id")
    private int id;

    @Column(name = "user_id", nullable = false)
    private int ownerId;

    @Column(name = "house_name", nullable = false)
    private String name;

    protected House() {
    }

    public House(int ownerId, String name) {
        this.ownerId = ownerId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}
