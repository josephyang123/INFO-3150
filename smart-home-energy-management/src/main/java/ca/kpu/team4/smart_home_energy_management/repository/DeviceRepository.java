package ca.kpu.team4.smart_home_energy_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.kpu.team4.smart_home_energy_management.model.Device;

public interface DeviceRepository 
        extends JpaRepository<Device,Integer> {
    @Query(value = """
        SELECT device.*
        FROM smart_devices device
        JOIN houses house ON house.house_id = device.house_id
        WHERE house.user_id = :userId
        """, nativeQuery = true)
        List<Device> findOwnedDevices(@Param("userId") int userId);

        @Query(value = """
        SELECT house_id
        FROM houses
        WHERE user_id = :userId
        """, nativeQuery = true)
        List<Integer> findOwnedHouseIds(@Param("userId") int userId);
}
