package com.example.OnlineHelpDesk.repository;

import com.example.OnlineHelpDesk.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Integer> {

    Optional<Facility> findById(Integer facilityId);
}
