package ca.kpu.team4.smart_home_energy_management.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ca.kpu.team4.smart_home_energy_management.model.DeviceUsage;

@Repository
public interface DeviceUsageRepository 
        extends JpaRepository<DeviceUsage, Integer> {

        @Query(value = """
            SELECT s.*
            FROM device_usage_sessions s
            WHERE s.device_id = :deviceId
                AND s.ended_at IS NULL
            """, nativeQuery = true)
        Optional<DeviceUsage> findOpenSession(
                @Param("deviceId") int deviceId);


         @Query(value = """
            SELECT s.*
            FROM device_usage_sessions s
            JOIN houses h ON h.house_id = s.house_id
            WHERE h.user_id = :ownerId
              AND s.started_at < :windowEnd
              AND (s.ended_at IS NULL OR s.ended_at > :windowStart)
            """, nativeQuery = true)
        List<DeviceUsage> findSessionsOverlappingWindow(
                @Param("ownerId") int ownerId,
                @Param("windowStart") LocalDateTime windowStart,
                @Param("windowEnd") LocalDateTime windowEnd);


        @Query(value = """
            SELECT s.*
            FROM device_usage_sessions s
            WHERE s.house_id = :houseId
            AND s.started_at < :windowEnd
            AND (s.ended_at IS NULL OR s.ended_at > :windowStart)
            """, nativeQuery = true)
        List<DeviceUsage> findHouseSessionsOverlappingWindow(
                @Param("houseId") int houseId,
                @Param("windowStart") LocalDateTime windowStart,
                @Param("windowEnd") LocalDateTime windowEnd);
    
}
