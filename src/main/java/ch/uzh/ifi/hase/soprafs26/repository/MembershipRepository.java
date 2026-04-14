package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.w3c.dom.ls.LSOutput;

import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import java.util.Optional;
import java.util.List;

@Repository("membershipRepository")
public interface MembershipRepository extends JpaRepository<Membership, Long> {

    @Query("SELECT m FROM Membership m WHERE m.trip.tripId = :tripId AND m.user.id = :userId")
    Optional<Membership> findByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);

    @Query("SELECT COUNT(m) > 0 FROM Membership m WHERE m.trip.tripId = :tripId AND m.user.id = :userId")
    boolean existsByTripIdAndUserId(@Param("tripId") Long tripId, @Param("userId") Long userId);

    boolean existsByTripAndUser(Trip trip, User user);
    List<Membership> findByTrip(Trip trip);
    List<Membership> findByUser(User user);
}
