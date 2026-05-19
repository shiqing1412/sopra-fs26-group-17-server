package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.ParticipationStatus;
import ch.uzh.ifi.hase.soprafs26.entity.EventMember;
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
import ch.uzh.ifi.hase.soprafs26.rest.dto.ItineraryPollingResponseDTO;
import ch.uzh.ifi.hase.soprafs26.repository.EventMemberRepository;

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
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;
  @Mock
  private TripRepository tripRepository;
  @Mock
  private MembershipRepository membershipRepository;
  @Mock
  private EventMemberRepository eventMemberRepository;

  @InjectMocks
  private EventService eventService;

  private User member;
  private User stranger;
  private Trip trip;
  private Event event;
  private Event eventPast;
  private Membership membership;
  private EventPostDTO validPostDTO;
  private EventPutDTO validPutDTO;


  @BeforeEach
  void setup() {
    member = new User();
    member.setUserId(1L);
    member.setUsername("member");
    member.setStatus(ch.uzh.ifi.hase.soprafs26.constant.UserStatus.ONLINE);

    stranger = new User();
    stranger.setUserId(99L);
    stranger.setUsername("stranger");

    trip = new Trip();
    trip.setTripId(10L);
    trip.setTripTitle("Japan Trip");
    trip.setStartDate(LocalDate.of(2027, 5, 1));
    trip.setEndDate(LocalDate.of(2027, 5, 3));
    trip.setOwner(member);

    Location location = new Location();
    location.setPlaceId("place-001");
    location.setName("Tokyo Tower");
    location.setLat(35.6586);
    location.setLng(139.7454);

    event = new Event();
    event.setEventId(100L);
    event.setEventTitle("Visit Tokyo Tower");
    event.setDate(LocalDate.of(2027, 5, 1));
    event.setTime(LocalTime.of(10, 0));
    event.setEndTime(LocalTime.of(12, 0));
    event.setNotes("Bring camera");
    event.setLocation(location);
    event.setCreator(member);
    event.setTrip(trip);

    eventPast = new Event();
    eventPast.setEventId(101L);
    eventPast.setEventTitle("Visit Kyoto");
    eventPast.setDate(LocalDate.of(2027, 4, 30));
    eventPast.setTime(LocalTime.of(10, 0));
    eventPast.setEndTime(LocalTime.of(12, 0));
    eventPast.setNotes("Bring camera");
    eventPast.setLocation(location);
    eventPast.setCreator(member);
    eventPast.setTrip(trip);

    membership = new Membership();
    membership.setMembershipId(1L);
    membership.setUser(member);
    membership.setTrip(trip);
    membership.setRole("MEMBER");

    validPostDTO = new EventPostDTO();
    validPostDTO.setEventTitle("Visit Tokyo Tower");
    validPostDTO.setDate(LocalDate.of(2027, 5, 1));
    validPostDTO.setTime(LocalTime.of(10, 0));
    validPostDTO.setEndTime(LocalTime.of(12, 0));
    validPostDTO.setPlaceId("place-001");
    validPostDTO.setPlaceName("Tokyo Tower");
    validPostDTO.setLat(35.6586);
    validPostDTO.setLng(139.7454);

    validPutDTO = new EventPutDTO();
    validPutDTO.setEventTitle("Updated Title");
    validPutDTO.setDate(LocalDate.of(2027, 5, 2));
    validPutDTO.setTime(LocalTime.of(14, 0));
    validPutDTO.setEndTime(LocalTime.of(16, 0));
    validPutDTO.setPlaceId("place-002");
    validPutDTO.setPlaceName("Shibuya");
    validPutDTO.setLat(35.6595);
    validPutDTO.setLng(139.7004);
  }

  //gitpedByDay
  @Test
  void getEventsGroupedByDay_memberAccess_returnsAllDays() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.findByTrip_TripIdOrderByDateAscTimeAsc(10L)).thenReturn(List.of(event));
    when(membershipRepository.findByTrip(trip)).thenReturn(List.of(membership)); // add this
    when(eventMemberRepository.findByEvent(any(Event.class))).thenReturn(List.of());
    when(eventMemberRepository.findByUserAndTripId(any(User.class), any(Long.class))).thenReturn(List.of());

    ItineraryPollingResponseDTO response = eventService.getEventsGroupedByDay(10L, member);
    List<DayDTO> days = response.getDays();
    // Trip spans 3 days (May 1–3), so we expect 3 DayDTOs
    assertEquals(3, days.size());
    // The event on May 1 should appear in the first day
    assertEquals(1, days.get(0).getEvents().size());
    // May 2 and May 3 should be empty
    assertEquals(0, days.get(1).getEvents().size());
    assertEquals(0, days.get(2).getEvents().size());
  }

  @Test
  void getEventsGroupedByDay_noEvents_returnsEmptyDaysForRange() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.findByTrip_TripIdOrderByDateAscTimeAsc(10L))
            .thenReturn(List.of());
    when(membershipRepository.findByTrip(trip)).thenReturn(List.of(membership));

    ItineraryPollingResponseDTO response = eventService.getEventsGroupedByDay(10L, member);
    List<DayDTO> days = response.getDays();

    assertEquals(3, days.size());
    days.forEach(day -> assertEquals(0, day.getEvents().size()));
  }

  @Test
  void getEventsGroupedByDay_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.getEventsGroupedByDay(10L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void getEventsGroupedByDay_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    // stranger is not owner (owner is member with id=1), not in membership
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.getEventsGroupedByDay(10L, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void getEventsGroupedByDay_ignoresEventsOutsideTripRange() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.findByTrip_TripIdOrderByDateAscTimeAsc(10L))
            .thenReturn(List.of(event, eventPast)); // eventPast is before trip start
    when(membershipRepository.findByTrip(trip)).thenReturn(List.of(membership));
    when(eventMemberRepository.findByEvent(any(Event.class))).thenReturn(List.of());
    when(eventMemberRepository.findByUserAndTripId(any(User.class), any(Long.class))).thenReturn(List.of());

    ItineraryPollingResponseDTO response = eventService.getEventsGroupedByDay(10L, member);
    List<DayDTO> days = response.getDays();

    // Only the event on May 1 should be included, not the one on April 30
    assertEquals(3, days.size());
    assertEquals(1, days.get(0).getEvents().size()); // May 1 has 1 event
    assertEquals(0, days.get(1).getEvents().size()); // May 2 has 0 events
    assertEquals(0, days.get(2).getEvents().size()); // May 3 has 0 events

    boolean containsEventPast = days.stream()
            .flatMap(day -> day.getEvents().stream())
            .anyMatch(e -> e.getEventId().equals(101L)); // eventPast has ID 101
    assertFalse(containsEventPast); // eventPast should not be included

  }

  //  createEvent

  @Test
  void createEvent_validInput_returnsEventGetDTO() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.save(any(Event.class))).thenReturn(event);

    EventGetDTO result = eventService.createEvent(10L, validPostDTO, member);

    assertNotNull(result);
    verify(eventRepository, times(1)).save(any(Event.class));
  }

  @Test
  void createEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void createEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingTitle_throws400() {
    validPostDTO.setEventTitle(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingDate_throws400() {
    validPostDTO.setDate(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingPlaceId_throws400() {
    validPostDTO.setPlaceId(null);
    
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingPlaceName_throws400() {
    validPostDTO.setPlaceName(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingLat_throws400() {
    validPostDTO.setLat(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingLng_throws400() {
    validPostDTO.setLng(null);
    
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));


    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingTime_throws400() {
    validPostDTO.setTime(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void createEvent_missingEndTime_throws400() {
    validPostDTO.setEndTime(null);

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

@Test
void createEvent_endTimeBeforeStartTime_throws400() {
    validPostDTO.setTime(LocalTime.of(12, 0));
    validPostDTO.setEndTime(LocalTime.of(11, 0));

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
    verify(eventRepository, times(0)).save(any(Event.class));
  }

  @Test
  void createEvent_dateOutsideTripRange_throws400() {
    validPostDTO.setDate(LocalDate.of(2030, 1, 1)); // far outside trip range

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.createEvent(10L, validPostDTO, member));
    assertEquals(400, ex.getStatusCode().value());
    verify(eventRepository, times(0)).save(any(Event.class));
  }

  //updateEvent

  @Test
  void updateEvent_validInput_returnsUpdatedDTO() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventRepository.save(any(Event.class))).thenReturn(event);

    EventGetDTO result = eventService.updateEvent(10L, 100L, validPutDTO, member);

    assertNotNull(result);
    verify(eventRepository, times(1)).save(any(Event.class));
  }

  @Test
  void updateEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void updateEvent_eventNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void updateEvent_eventBelongsToDifferentTrip_throws404() {
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
  void updateEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void updateEvent_missingTitle_throws400() {
    validPutDTO.setEventTitle(null);
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void updateEvent_dateOutsideTripRange_throws400() {
    validPutDTO.setDate(LocalDate.of(2030, 1, 1)); // far outside trip range
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  @Test
  void updateEvent_endTimeBeforeStartTime_throws400() {
    validPutDTO.setTime(LocalTime.of(12, 0));
    validPutDTO.setEndTime(LocalTime.of(11, 0));

    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.updateEvent(10L, 100L, validPutDTO, member));
    assertEquals(400, ex.getStatusCode().value());
  }

  // deleteEvent 

  @Test
  void deleteEvent_validInput_deletesSuccessfully() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));

    eventService.deleteEvent(10L, 100L, member);

    verify(eventRepository, times(1)).delete(event);
  }

  @Test
  void deleteEvent_tripNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void deleteEvent_eventNotFound_throws404() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, member));
    assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void deleteEvent_notMember_throws403() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

    ResponseStatusException ex = assertThrows(ResponseStatusException.class,
            () -> eventService.deleteEvent(10L, 100L, stranger));
    assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void hasTimeConflict_overlappingOnSameDay_returnsTrue() {
      Event a = new Event();
      a.setDate(LocalDate.of(2027, 5, 1));
      a.setTime(LocalTime.of(10, 0));
      a.setEndTime(LocalTime.of(12, 0));

      Event b = new Event();
      b.setDate(LocalDate.of(2027, 5, 1));
      b.setTime(LocalTime.of(11, 0));
      b.setEndTime(LocalTime.of(13, 0));

      assertTrue(eventService.hasTimeConflict(a, b));
  }

  @Test
  void hasTimeConflict_adjacentNonOverlapping_returnsFalse() {
      // b starts exactly when a ends — not a conflict
      Event a = new Event();
      a.setDate(LocalDate.of(2027, 5, 1));
      a.setTime(LocalTime.of(10, 0));
      a.setEndTime(LocalTime.of(11, 0));

      Event b = new Event();
      b.setDate(LocalDate.of(2027, 5, 1));
      b.setTime(LocalTime.of(11, 0));
      b.setEndTime(LocalTime.of(12, 0));

      assertFalse(eventService.hasTimeConflict(a, b));
  }

  @Test
  void hasTimeConflict_differentDays_returnsFalse() {
      Event a = new Event();
      a.setDate(LocalDate.of(2027, 5, 1));
      a.setTime(LocalTime.of(10, 0));
      a.setEndTime(LocalTime.of(12, 0));

      Event b = new Event();
      b.setDate(LocalDate.of(2027, 5, 2));
      b.setTime(LocalTime.of(10, 0));
      b.setEndTime(LocalTime.of(12, 0));

      assertFalse(eventService.hasTimeConflict(a, b));
  }

  @Test
  void joinEvent_validInput_setsJoinedAndReturnsDTO() {
    when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
    when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
    when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
    when(eventMemberRepository.findByEventAndUser(event, member)).thenReturn(Optional.empty());
    when(eventMemberRepository.findByUserAndTripId(any(User.class), any(Long.class))).thenReturn(List.of());
    when(eventMemberRepository.findByEvent(event)).thenReturn(List.of());

    EventGetDTO result = eventService.joinEvent(10L, 100L, member);

    assertNotNull(result);
    verify(eventMemberRepository, atLeastOnce()).save(any(EventMember.class));
  }

  @Test
  void joinEvent_tripNotFound_throws404() {
      when(tripRepository.findById(10L)).thenReturn(Optional.empty());

      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
              () -> eventService.joinEvent(10L, 100L, member));
      assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void joinEvent_eventNotFound_throws404() {
      when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
      when(eventRepository.findById(100L)).thenReturn(Optional.empty());

      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
              () -> eventService.joinEvent(10L, 100L, member));
      assertEquals(404, ex.getStatusCode().value());
  }

  @Test
  void joinEvent_notMember_throws403() {
      when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
      when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
      when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
              () -> eventService.joinEvent(10L, 100L, stranger));
      assertEquals(403, ex.getStatusCode().value());
  }

  @Test
  void dismissEvent_notFromConflict_setsOptedOut() {
      EventMember em = new EventMember();
      em.setEvent(event);
      em.setUser(member);
      em.setParticipationStatus(ParticipationStatus.JOINED);

      when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
      when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
      when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
      when(eventMemberRepository.findByEventAndUser(event, member)).thenReturn(Optional.of(em));
      when(eventMemberRepository.findByEvent(event)).thenReturn(List.of());
      when(eventMemberRepository.findByUserAndTripId(any(User.class), any(Long.class))).thenReturn(List.of());

      eventService.dismissEvent(10L, 100L, member, false);

      assertEquals(ParticipationStatus.OPTED_OUT, em.getParticipationStatus());
  }

  @Test
  void dismissEvent_fromConflictFlow_setsDismissed() {
      EventMember em = new EventMember();
      em.setEvent(event);
      em.setUser(member);
      em.setParticipationStatus(ParticipationStatus.JOINED);

      when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
      when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
      when(membershipRepository.findByTripIdAndUserId(10L, 1L)).thenReturn(Optional.of(membership));
      when(eventMemberRepository.findByEventAndUser(event, member)).thenReturn(Optional.of(em));
      when(eventMemberRepository.findByEvent(event)).thenReturn(List.of());
      when(eventMemberRepository.findByUserAndTripId(any(User.class), any(Long.class))).thenReturn(List.of());

      eventService.dismissEvent(10L, 100L, member, true);

      assertEquals(ParticipationStatus.DISMISSED, em.getParticipationStatus());
  }

  @Test
  void dismissEvent_notMember_throws403() {
      when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
      when(eventRepository.findById(100L)).thenReturn(Optional.of(event));
      when(membershipRepository.findByTripIdAndUserId(10L, 99L)).thenReturn(Optional.empty());

      ResponseStatusException ex = assertThrows(ResponseStatusException.class,
              () -> eventService.dismissEvent(10L, 100L, stranger, false));
      assertEquals(403, ex.getStatusCode().value());
  }

}