package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ItineraryPollingResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EventServiceIntegrationTest {

  @Autowired private EventService eventService;
  @Autowired private UserRepository userRepository;
  @Autowired private TripRepository tripRepository;
  @Autowired private MembershipRepository membershipRepository;
  @Autowired private EventRepository eventRepository;

  private User user;
  private Trip trip;

  @BeforeEach
  void setup() {
    user = new User();
    user.setUsername("eventintuser");
    user.setPassword("password");
    user.setStatus(UserStatus.ONLINE);
    user.setToken("event-int-token");
    userRepository.save(user);

    trip = new Trip();
    trip.setTripTitle("Event Integration Trip");
    trip.setStartDate(LocalDate.of(2027, 6, 1));
    trip.setEndDate(LocalDate.of(2027, 6, 5)); // 5 days
    trip.setShareCode("EVTINT01");
    trip.setOwner(user);
    tripRepository.save(trip);

    Membership membership = new Membership();
    membership.setUser(user);
    membership.setTrip(trip);
    membership.setRole("OWNER");
    membership.setJoinedAt(LocalDateTime.now());
    membershipRepository.save(membership);
  }


  @Test
  void createEvent_persistsToDbAndReturnsDTO() {
    EventPostDTO dto = validPostDTO(LocalDate.of(2027, 6, 2));

    EventGetDTO result = eventService.createEvent(trip.getTripId(), dto, user);

    assertNotNull(result.getEventId());
    assertEquals("Integration Event", result.getEventTitle());

    List<Event> stored = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId());
    assertEquals(1, stored.size());
  }

  @Test
  void createEvent_dateOutsideRange_throws400AndDoesNotPersist() {
    EventPostDTO dto = validPostDTO(LocalDate.of(2027, 7, 1)); // outside June 1–5

    assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(trip.getTripId(), dto, user));

    assertTrue(eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId()).isEmpty());
  }


  @Test
  void updateEvent_persistsTitleChange() {
    EventGetDTO created = eventService.createEvent(trip.getTripId(), validPostDTO(LocalDate.of(2027, 6, 2)), user);

    EventPutDTO updateDto = validPutDTO(LocalDate.of(2027, 6, 3), "Updated Title");
    EventGetDTO updated = eventService.updateEvent(trip.getTripId(), created.getEventId(), updateDto, user);

    assertEquals("Updated Title", updated.getEventTitle());

    Event stored = eventRepository.findById(created.getEventId()).orElseThrow();
    assertEquals("Updated Title", stored.getEventTitle());
    assertEquals(LocalDate.of(2027, 6, 3), stored.getDate());
  }


  @Test
  void deleteEvent_removesFromDb() {
    EventGetDTO created = eventService.createEvent(trip.getTripId(), validPostDTO(LocalDate.of(2027, 6, 2)), user);
    Long eventId = created.getEventId();

    eventService.deleteEvent(trip.getTripId(), eventId, user);

    assertTrue(eventRepository.findById(eventId).isEmpty());
  }


  @Test
  void getEventsGroupedByDay_returnsOneDayDTOPerDayInRange() {
    eventService.createEvent(trip.getTripId(), validPostDTO(LocalDate.of(2027, 6, 2)), user);

    ItineraryPollingResponseDTO response =
            eventService.getEventsGroupedByDay(trip.getTripId(), user);

    List<DayDTO> days = response.getDays();
    assertEquals(5, days.size()); // June 1 through June 5

    // June 1 — no event
    assertEquals(LocalDate.of(2027, 6, 1), days.get(0).getDate());
    assertEquals(0, days.get(0).getEvents().size());

    // June 2 — 1 event
    assertEquals(LocalDate.of(2027, 6, 2), days.get(1).getDate());
    assertEquals(1, days.get(1).getEvents().size());
  }

  @Test
  void getEventsGroupedByDay_includesCurrentUserInMembers() {
    ItineraryPollingResponseDTO response =
            eventService.getEventsGroupedByDay(trip.getTripId(), user);

    boolean currentUserPresent = response.getMembers().stream()
            .anyMatch(m -> m.getUserId().equals(user.getUserId()) && Boolean.TRUE.equals(m.getCurrentUser()));
    assertTrue(currentUserPresent);
  }


  @Test
  void fullLifecycle_createUpdateDelete_consistentState() {
    // Create
    EventGetDTO created = eventService.createEvent(trip.getTripId(), validPostDTO(LocalDate.of(2027, 6, 2)), user);
    assertNotNull(created.getEventId());

    // Update
    EventPutDTO updateDto = validPutDTO(LocalDate.of(2027, 6, 4), "Final Title");
    EventGetDTO updated = eventService.updateEvent(trip.getTripId(), created.getEventId(), updateDto, user);
    assertEquals("Final Title", updated.getEventTitle());

    // Verify DB reflects update
    ItineraryPollingResponseDTO response =
            eventService.getEventsGroupedByDay(trip.getTripId(), user);
    DayDTO june4 = response.getDays().get(3); // index 3 = June 4
    assertEquals(1, june4.getEvents().size());
    assertEquals("Final Title", june4.getEvents().get(0).getEventTitle());

    // Delete
    eventService.deleteEvent(trip.getTripId(), created.getEventId(), user);
    assertTrue(eventRepository.findById(created.getEventId()).isEmpty());
  }


  private EventPostDTO validPostDTO(LocalDate date) {
    EventPostDTO dto = new EventPostDTO();
    dto.setEventTitle("Integration Event");
    dto.setDate(date);
    dto.setTime(LocalTime.of(10, 0));
    dto.setEndTime(LocalTime.of(11, 0));
    dto.setPlaceId("place-001");
    dto.setPlaceName("Test Place");
    dto.setLat(47.0);
    dto.setLng(8.0);
    return dto;
  }

  private EventPutDTO validPutDTO(LocalDate date, String title) {
    EventPutDTO dto = new EventPutDTO();
    dto.setEventTitle(title);
    dto.setDate(date);
    dto.setTime(LocalTime.of(10, 0));
    dto.setEndTime(LocalTime.of(11, 0));
    dto.setPlaceId("place-001");
    dto.setPlaceName("Test Place");
    dto.setLat(47.0);
    dto.setLng(8.0);
    return dto;
  }
}