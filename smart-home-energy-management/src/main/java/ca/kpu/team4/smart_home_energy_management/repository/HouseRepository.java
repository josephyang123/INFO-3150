package ca.kpu.team4.smart_home_energy_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.kpu.team4.smart_home_energy_management.model.House;
import jakarta.persistence.LockModeType;

public interface HouseRepository extends JpaRepository<House, Integer> {
    interface AdminHouseRow {
        int getHouseId();
        String getHouseName();
        int getOwnerId();
        String getOwnerName();
        String getOwnerEmail();
        Integer getMemberId();
        String getMemberName();
        String getMemberEmail();
    }

    @Query(value = """
        SELECT h.house_id AS houseId, h.house_name AS houseName,
               owner.user_id AS ownerId, owner.user_name AS ownerName, owner.email AS ownerEmail,
               member.user_id AS memberId, member.user_name AS memberName, member.email AS memberEmail
        FROM houses h
        JOIN user_accounts owner ON owner.user_id = h.user_id
        LEFT JOIN family_members fm ON fm.house_id = h.house_id
        LEFT JOIN user_accounts member ON member.user_id = fm.user_id
        ORDER BY h.house_name, h.house_id, member.user_name, member.email, member.user_id
        """, nativeQuery = true)
    List<AdminHouseRow> findAdminHouses();

    boolean existsByOwnerId(int ownerId);
    long countByOwnerId(int ownerId);

    @Query("select h.ownerId from House h where h.id = :houseId")
    Optional<Integer> findOwnerId(@Param("houseId") int houseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from House h where h.id = :houseId")
    Optional<House> findByIdForUpdate(@Param("houseId") int houseId);
}
