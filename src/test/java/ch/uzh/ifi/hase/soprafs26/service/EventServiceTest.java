package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Location;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

  @Mock
  private EventRepository eventRepository;
  @Mock
  private TripRepository tripRepository;
  @Mock
  private MembershipRepository membershipRepository;

  @InjectMocks
  private EventService eventService;

  private User member;
  private User stranger;
  private Trip trip;
  private Event event;
  private Membership membership;
  private EventPostDTO validPostDTO;
  private EventPutDTO validPutDTO;


  @BeforeEach
  public void setup() {
    member = new User();
    member.setUserId(1L);
    member.setUsername("member");

    stranger = new User();
    stranger.setUserId(99L);
    stranger.setUsername("stranger");

    trip = new Trip();
    trip.setTripId(10L);
    trip.setTripTitle("Japan Trip");
    trip.setStartDate(LocalDate.of(2026, 5, 1));
    trip.setEndDate(LocalDate.of(2026, 5, 3));
    trip.setOwner(member);

    Location location = new Location();
    location.setPlaceId("place-001");
    location.setName("Tokyo Tower");
    location.setLat(35.6586);
    location.setLng(139.7454);

    event = new Event();
    event.setEventId(100L);
    event.setEventTitle("Visit Tokyo Tower");
    event.setDate(LocalDate.of(2026, 5, 1));
    event.setTime(LocalTime.of(10, 0));
    event.setEndTime(LocalTime.of(12, 0));
    event.setNotes("Bring camera");
    event.setLocation(location);
    event.setCreator(member);
    event.setTrip(trip);

    membership = new Membership();
    membership.setMembershipId(1L);
    membership.setUser(member);
    membership.setTrip(trip);
    membership.setRole("MEMBER");

    validPostDTO = new EventPostDTO();
    validPostDTO.setEventTitle("Visit Tokyo Tower");
    validPostDTO.setDate(LocalDate.of(2026, 5, 1));
    validPostDTO.setTime(LocalTime.of(10, 0));
    validPostDTO.setEndTime(LocalTime.of(12, 0));
    validPostDTO.setPlaceId("place-001");
    validPostDTO.setPlaceName("Tokyo Tower");
    validPostDTO.setLat(35.6586);
    validPostDTO.setLng(139.7454);

    validPutDTO = new EventPutDTO();
    validPutDTO.setEventTitle("Updated Title");
    validPutDTO.setDate(LocalDate.of(2026, 5, 2));
    validPutDTO.setTime(LocalTime.of(14, 0));
    validPutDTO.setEndTime(LocalTime.of(16, 0));
    validPutDTO.setPlaceId("place-002");
    validPutDTO.setPlaceName("Shibuya");
    validPutDTO.setLat(35.6595);
    validPutDTO.setLng(139.7004);
  }

  //getEventsGroupedByDay
  @Test
  public void getEventsGroupedByDay_memberAccess_returnsAllDays() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.findByTrip_TripIdOrderByDateAscTimeAsc(10L))
      .thenReturn(List.of(event));

    List<DayDTO> days = eventService.getEventsGroupedByDay(10L, member);

    // Trip spans 3 days (May 1–3), so we expect 3 DayDTOs
    assertEquals(3, days.size());
    // The event on May 1 should appear in the first day
    assertEquals(1, days.get(0).getEvents().size());
    // May 2 and May 3 should be empty
    assertEquals(0, days.get(1).getEvents().size());
    assertEquals(0, days.get(2).getEvents().size());
  }

  @Test
  public void getEventsGroupedByDay_noEvents_returnsEmptyDaysForRange() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.findByTrip_TripIdOrderByDateAscTimeAsc(10L))
            .thenReturn(List.of());

    List<DayDTO> days = eventService.getEventsGroupedByDay(10L, member);

    assertEquals(3, days.size());
    days.forEach(day -> assertEquals(0, day.getEvents().size()));
  }

  @Test
  public void getEventsGroupedByDay_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.getEventsGroupedByDay(10L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void getEventsGroupedByDay_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    // stranger is not owner (owner is member with id=1), not in membership
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.getEventsGroupedByDay(10L, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  //  createEvent

  @Test
  public void createEvent_validInput_returnsEventGetDTO() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.save(any(Event.class))).thenReturn(event);

    EventGetDTO result = eventService.createEvent(10L, validPostDTO, member);

    assertNotNull(result);
    verify(eventRepository, times(1)).save(any(Event.class));
  }

  @Test
  public void createEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingTitle_throws400() {
    validPostDTO.setEventTitle(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingDate_throws400() {
    validPostDTO.setDate(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingPlaceId_throws400() {
    validPostDTO.setPlaceId(null);
    
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingPlaceName_throws400() {
    validPostDTO.setPlaceName(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingLat_throws400() {
    validPostDTO.setLat(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingLng_throws400() {
    validPostDTO.setLng(null);
    
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingTime_throws400() {
    validPostDTO.setTime(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void createEvent_missingEndTime_throws400() {
    validPostDTO.setEndTime(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }


  //updateEvent

  @Test
  public void updateEvent_validInput_returnsUpdatedDTO() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.save(any(Event.class))).thenReturn(event);

    EventGetDTO result = eventService.updateEvent(10L, 100L, validPutDTO, member);

    assertNotNull(result);
    verify(eventRepository, times(1)).save(any(Event.class));
  }

  @Test
  public void updateEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(404, ex.getStatusCode().value());
}

  @Test
  public void updateEvent_eventNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void updateEvent_eventBelongsToDifferentTrip_throws404() {
    Trip otherTrip = new Trip();
    otherTrip.setTripId(999L);
    event.setTrip(otherTrip);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void updateEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  public void updateEvent_missingTitle_throws400() {
    validPutDTO.setEventTitle(null);
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  public void updateEvent_dateOutsideTripRange_throws400() {
    validPutDTO.setDate(LocalDate.of(2030, 1, 1)); // far outside trip range
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  // deleteEvent 

  @Test
  public void deleteEvent_validInput_deletesSuccessfully() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    eventService.deleteEvent(10L, 100L, member);

    verify(eventRepository, times(1)).delete(event);
  }

  @Test
  public void deleteEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void deleteEvent_eventNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  public void deleteEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

}