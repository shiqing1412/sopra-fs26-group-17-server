package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Location;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private EventRepository eventRepository;

  private User user;
  private Trip trip;

  @BeforeEach
  void setup() {
    user = new User();
    user.setUsername("eventrepouser");
    user.setPassword("hash");
    user.setStatus(UserStatus.ONLINE);
    user.setToken(UUID.randomUUID().toString());
    entityManager.persist(user);

    trip = new Trip();
    trip.setTripTitle("Repo Test Trip");
    trip.setStartDate(LocalDate.of(2027, 6, 1));
    trip.setEndDate(LocalDate.of(2027, 6, 5));
    trip.setShareCode("EVTREPO1");
    trip.setOwner(user);
    entityManager.persist(trip);

    entityManager.flush();
  }

  private Event buildAndPersistEvent(String title, LocalDate date, LocalTime start, LocalTime end) {
    Location location = new Location();
    location.setPlaceId("place-001");
    location.setName("Test Place");
    location.setLat(47.0);
    location.setLng(8.0);

    Event event = new Event();
    event.setEventTitle(title);
    event.setDate(date);
    event.setTime(start);
    event.setEndTime(end);
    event.setCreatedAt(LocalDateTime.now());
    event.setLocation(location);
    event.setCreator(user);
    event.setTrip(trip);
    entityManager.persist(event);
    return event;
  }

  @Test
  void findByTrip_TripIdOrderByDateAscTimeAsc_returnsEventsChronologically() {
    buildAndPersistEvent("Event C", LocalDate.of(2027, 6, 2), LocalTime.of(14, 0), LocalTime.of(15, 0));
    buildAndPersistEvent("Event A", LocalDate.of(2027, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
    buildAndPersistEvent("Event B", LocalDate.of(2027, 6, 2), LocalTime.of(9,  0), LocalTime.of(10, 0));
    entityManager.flush();

    List<Event> events = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId());

    assertEquals(3, events.size());
    assertEquals("Event A", events.get(0).getEventTitle()); // June 1, 10:00
    assertEquals("Event B", events.get(1).getEventTitle()); // June 2, 09:00
    assertEquals("Event C", events.get(2).getEventTitle()); // June 2, 14:00
  }

  @Test
  void findByTrip_TripIdOrderByDateAscTimeAsc_wrongTripId_returnsEmpty() {
    buildAndPersistEvent("Event", LocalDate.of(2027, 6, 1), LocalTime.of(10, 0), LocalTime.of(11, 0));
    entityManager.flush();

    List<Event> events = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId() + 999);

    assertTrue(events.isEmpty());
  }

  @Test
  void findByTrip_TripIdOrderByDateAscTimeAsc_noEvents_returnsEmpty() {
    List<Event> events = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId());

    assertTrue(events.isEmpty());
  }
}