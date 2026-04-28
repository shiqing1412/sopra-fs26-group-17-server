package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.constant.ParticipationStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.EventMember;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventMemberRepository extends JpaRepository<EventMember, Long> {

  List<EventMember> findByEvent(Event event);

  Optional<EventMember> findByEventAndUser(Event event, User user);

  @Query("SELECT em FROM EventMember em WHERE em.user = :user AND em.event.trip.tripId = :tripId")
  List<EventMember> findByUserAndTripId(@Param("user") User user, @Param("tripId") Long tripId);

  @Modifying
  @Query("UPDATE EventMember em SET em.participationStatus = :status WHERE em.event = :event AND em.user = :user")
  void updateStatusByEventAndUser(@Param("event") Event event,
                                  @Param("user") User user,
                                  @Param("status") ParticipationStatus status);
}