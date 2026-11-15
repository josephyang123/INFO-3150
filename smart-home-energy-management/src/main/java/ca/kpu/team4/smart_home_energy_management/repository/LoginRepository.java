package ca.kpu.team4.smart_home_energy_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ca.kpu.team4.smart_home_energy_management.model.User;
import jakarta.persistence.LockModeType;

public interface LoginRepository 
        extends JpaRepository<User, Integer> {
    User findByEmail(String email);

    interface AdminAccountRow {
        int getUserId();
        String getName();
        String getEmail();
    }

    @Query(value = """
        SELECT u.user_id AS userId, u.user_name AS name, u.email AS email
        FROM user_accounts u
        WHERE u.account_role = 'FAMILY'
          AND NOT EXISTS (SELECT 1 FROM houses h WHERE h.user_id = u.user_id)
          AND NOT EXISTS (SELECT 1 FROM family_members fm WHERE fm.user_id = u.user_id)
        ORDER BY u.user_name, u.email, u.user_id
        """, nativeQuery = true)
    List<AdminAccountRow> findUnassignedFamilyAccounts();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") int id);
    
    
}
