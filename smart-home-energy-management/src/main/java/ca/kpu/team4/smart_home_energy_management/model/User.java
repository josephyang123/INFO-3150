package ca.kpu.team4.smart_home_energy_management.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "user_id", nullable = false, unique = true)
    private int id;

    @Column(name = "user_name", nullable = false, unique = true)
    private String username;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String passwordHash;
    @Column (name = "account_role", nullable = false)
    private String account_role;

    public String getAccount_role() {
        return account_role;
    }

    public void setAccount_role(String account_role) {
        this.account_role = account_role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }


    protected User() {
        // Default constructor for JPA
    }
    public User( String username, String email, String passwordHash) {
        
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.account_role = "FAMILY";
    }

    

}
