package com.example.OnlineHelpDesk.repository;

import com.example.OnlineHelpDesk.model.Request;
import com.example.OnlineHelpDesk.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Integer> {

    List<Request> findAllByUserId(Integer userId);

    List<Request> findAllByFacilityId(Integer facilityId);

    List<Request> findAllByAssignedUserId(Integer userId);

    Long countByStatus(Status status);

    Long countByAssignedUserIdAndStatus(Integer userId, Status status);

    Long countAllByUserIdAndStatus(Integer userId, Status status);

    Long countAllByFacilityIdAndStatus(Integer facilityId, Status status);

    Long countAllByAssignedUserId(Integer userId);

    Long countAllByUserId(Integer userId);

    Long countAllByFacilityId(Integer facilityId);

    @Query("""
        SELECT r FROM Request r 
        WHERE r.createdAt BETWEEN :startDate AND :endDate
        AND (:facilityId IS NULL OR r.facilityId = :facilityId)
    """)
    List<Request> findRequestsByDateRangeAndFacility(Date startDate, Date endDate, Integer facilityId);
}
