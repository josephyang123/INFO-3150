package ca.kpu.team4.smart_home_energy_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import ca.kpu.team4.smart_home_energy_management.model.User;

public interface FamilymemberRepository extends Repository<User, Integer> {

    interface MemberRow {
        int getMemberId();
        String getMemberName();
        int getCanViewDevices();
        int getCanControlDevices();
        int getCanViewEnergy();
    }

    interface HouseAccessRow {
        int getHouseId();
        String getHouseName();
        String getMemberName();
        int getCanViewDevices();
        int getCanControlDevices();
        int getCanViewEnergy();
    }

    @Query(value = """
        SELECT fm.fm_id AS memberId, member_account.user_name AS memberName,
        CASE WHEN fm.can_view_device THEN 1 ELSE 0 END AS canViewDevices,
        CASE WHEN fm.can_edit_device THEN 1 ELSE 0 END AS canControlDevices,
        CASE WHEN fm.can_view_stats THEN 1 ELSE 0 END AS canViewEnergy
        FROM family_members fm
        JOIN user_accounts member_account ON member_account.user_id = fm.user_id
        JOIN houses h ON h.house_id = fm.house_id
        JOIN user_accounts owner_account ON owner_account.user_id = h.user_id
        WHERE owner_account.email = :email
        ORDER BY member_account.user_name, fm.fm_id
        """, nativeQuery = true)
    List<MemberRow> findMembersForOwner(@Param("email") String email);


    @Modifying
    @Query(value = """
        UPDATE family_members fm
        JOIN houses h ON h.house_id = fm.house_id
        JOIN user_accounts owner_account ON owner_account.user_id = h.user_id
        SET fm.can_view_device = :canViewDevices,
            fm.can_edit_device = :canControlDevices,
            fm.can_view_stats = :canViewEnergy
        WHERE fm.fm_id = :memberId
        AND owner_account.email = :email
        """, nativeQuery = true)
    int updatePermissions(
            @Param("memberId") int memberId,
            @Param("email") String email,
            @Param("canViewDevices") boolean canViewDevices,
            @Param("canControlDevices") boolean canControlDevices,
            @Param("canViewEnergy") boolean canViewEnergy);

    @Query(value = """
    SELECT h.house_id AS houseId,
           h.house_name AS houseName,
           u.user_name AS memberName,
           CASE WHEN fm.can_view_device THEN 1 ELSE 0 END AS canViewDevices,
           CASE WHEN fm.can_view_device AND fm.can_edit_device
                THEN 1 ELSE 0 END AS canControlDevices,
           CASE WHEN fm.can_view_stats THEN 1 ELSE 0 END AS canViewEnergy
        FROM family_members fm
        JOIN houses h ON h.house_id = fm.house_id
        JOIN user_accounts u ON u.user_id = fm.user_id
        WHERE u.email = :email
        ORDER BY h.house_name, h.house_id
        """, nativeQuery = true)
    List<HouseAccessRow> findHouseAccessForFamily(@Param("email") String email);
}
