package com.example.OnlineHelpDesk.repository;

import com.example.OnlineHelpDesk.model.Role;
import com.example.OnlineHelpDesk.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    List<User> findAllByRole(Role role);

    List<User> findAllByRoleAndFacilityId(Role role, Integer facilityId);

    List<User> findAllByFacilityId(Integer facilityId);
}
