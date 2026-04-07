package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
<<<<<<< HEAD
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Membership;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, Long> {
    
}
=======
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import java.util.Optional;

@Repository("membershipRepository")
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @Query("SELECT m FROM Membership m WHERE m.trip.tripId = :tripId AND m.user.userId = :userId")
    Optional<Membership> findByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);

    @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.trip.tripId = :tripId AND m.user.userId = :userId")
    boolean existsByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);
}
>>>>>>> 11c6450 (WIP: before rebase)
