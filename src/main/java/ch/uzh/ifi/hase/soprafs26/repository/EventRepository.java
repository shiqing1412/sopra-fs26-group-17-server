package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository("eventRepository")
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByTrip_TripIdOrderByDateAscTimeAsc(Long tripId);

    List<Event> findByTrip_TripIdAndCreatedAtAfter(Long tripId, LocalDateTime since);
}