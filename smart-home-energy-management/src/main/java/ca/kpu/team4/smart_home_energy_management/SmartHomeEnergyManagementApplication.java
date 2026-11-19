package ca.kpu.team4.smart_home_energy_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartHomeEnergyManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                SmartHomeEnergyManagementApplication.class, args);
    }
}