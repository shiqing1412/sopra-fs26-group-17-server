package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ItineraryPollingResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.entity.Location;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;
import ch.uzh.ifi.hase.soprafs26.entity.EventMember;
import ch.uzh.ifi.hase.soprafs26.constant.ParticipationStatus;
import ch.uzh.ifi.hase.soprafs26.repository.EventMemberRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventMemberDTO;


import java.util.Optional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

  private final EventRepository eventRepository;
  private final TripRepository tripRepository;
  private final MembershipRepository membershipRepository;
  private final EventMemberRepository eventMemberRepository;

  public EventService(EventRepository eventRepository,
                      TripRepository tripRepository,
                      MembershipRepository membershipRepository,
                      EventMemberRepository eventMemberRepository) {
    this.eventRepository = eventRepository;
    this.tripRepository = tripRepository;
    this.membershipRepository = membershipRepository;
    this.eventMemberRepository = eventMemberRepository;
  }

  public ItineraryPollingResponseDTO getEventsGroupedByDay(Long tripId, User requestingUser) {
  
    Trip trip = findTripOrThrow(tripId); //404 if not found

    Membership membership = validateTripMember(tripId, requestingUser);
    membership.setLastSeenAt(LocalDateTime.now());
    membershipRepository.save(membership);

    //Fetch all events for this trip, already sorted by date asc, time asc
    List<Event> events = eventRepository
      .findByTrip_TripIdOrderByDateAscTimeAsc(tripId);

    // Group events by date into a map
    Map<LocalDate, List<EventGetDTO>> byDate = events.stream()
      .collect(Collectors.groupingBy(
        Event::getDate,
        Collectors.mapping(
          e -> toEventGetDTO(e, requestingUser),
          Collectors.toList()
        )
      ));

    // Build a DayDTO for every day in the trip range, even if no events
    List<DayDTO> days = new ArrayList<>();
    LocalDate cursor = trip.getStartDate();
    while (!cursor.isAfter(trip.getEndDate())) {
      days.add(new DayDTO(cursor, byDate.getOrDefault(cursor, List.of())));
      cursor = cursor.plusDays(1);
    }

    LocalDateTime activeThreshold = LocalDateTime.now().minusSeconds(30);

    List<TripMemberDTO> members = membershipRepository.findByTrip(trip).stream()
      .map(m -> {
        TripMemberDTO dto = new TripMemberDTO();
        dto.setUserId(m.getUser().getUserId());
        dto.setUsername(m.getUser().getUsername());
        dto.setRole(m.getRole());
        dto.setStatus(m.getUser().getStatus().toString());

        boolean active = m.getLastSeenAt() != null && m.getLastSeenAt().isAfter(activeThreshold);
        dto.setActive(active);
        dto.setCurrentUser(m.getUser().getUserId().equals(requestingUser.getUserId()));
        return dto;
      
      })
      .toList();
    
    return new ItineraryPollingResponseDTO(days, members);
  

  }



  public EventGetDTO createEvent(Long tripId, EventPostDTO dto, User creator) {
    
    Trip trip = findTripOrThrow(tripId);
    validateTripMember(tripId, creator);
    validateEventPostDTO(dto, trip);
    Event event = DTOMapper.INSTANCE.convertEventPostDTOtoEntity(dto);

    Location location = new Location();
    location.setPlaceId(dto.getPlaceId());
    location.setName(dto.getPlaceName());
    location.setLat(dto.getLat());
    location.setLng(dto.getLng());

    event.setLocation(location);
    event.setCreator(creator);
    event.setTrip(trip);
    event.setCreatedAt(LocalDateTime.now());

    Event saved = eventRepository.save(event);
    autoEnrollMembersForNewEvent(saved, trip);
    return toEventGetDTO(saved, creator);
  }


  //update event method
  public EventGetDTO updateEvent(Long tripId, Long eventId, EventPutDTO dto, User requestingUser) {
    Trip trip = findTripOrThrow(tripId); //404 if not found
    Event event = findEventOrThrow(eventId);//404 if not found

    validateEventBelongsToTrip(event, tripId);//404 if event not in this trip
    validateTripMember(tripId, requestingUser);//403 if not member of this trip
    validateEventPutDTO(dto, trip);//400 if any required field missing or event date outside trip range

    event.setEventTitle(dto.getEventTitle());
    event.setDate(dto.getDate()); 
    event.setTime(dto.getTime());
    event.setNotes(dto.getNotes());
    event.setEndTime(dto.getEndTime());

    if(event.getLocation() == null) {
      event.setLocation(new Location());
    }
    event.getLocation().setPlaceId(dto.getPlaceId());
    event.getLocation().setName(dto.getPlaceName());
    event.getLocation().setLat(dto.getLat());
    event.getLocation().setLng(dto.getLng());

    Event updatedEvent = eventRepository.save(event);
    return toEventGetDTO(updatedEvent, requestingUser);
}

public void deleteEvent(Long tripId, Long eventId, User requestingUser) {
    
  findTripOrThrow(tripId); //404 if not found
  Event event = findEventOrThrow(eventId);//404 if not found
  validateEventBelongsToTrip(event, tripId);//404 if event not in this trip
  
  validateTripMember(tripId, requestingUser);//403 if not member of this trip

  eventRepository.delete(event);
  eventRepository.flush();
}

//validation methods

  private Trip findTripOrThrow(Long tripId) {
    return tripRepository.findById(tripId)
    .orElseThrow(() -> new ResponseStatusException(
      HttpStatus.NOT_FOUND, "Trip not found.")); //404
    }

  private Event findEventOrThrow(Long eventId) {
    return eventRepository.findById(eventId)
    .orElseThrow(() -> new ResponseStatusException(
      HttpStatus.NOT_FOUND, "Event not found.")); //404
    }

  private Membership validateTripMember(Long tripId, User user) {
    return membershipRepository.findByTripIdAndUserId(tripId, user.getUserId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a member of this trip.")); //403
  }

  private void validateEventBelongsToTrip(Event event, Long tripId) {
    if (!event.getTrip().getTripId().equals(tripId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Event not found in this trip."); //404
      }
    }

  private void validateEventDateWithinTrip(LocalDate date, Trip trip) {
    if (date.isBefore(trip.getStartDate()) || date.isAfter(trip.getEndDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Event date is outside the trip's date range.");
      }
    }

  private void validateEventPostDTO(EventPostDTO dto, Trip trip) {
    if (dto.getEventTitle() == null || dto.getEventTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventTitle is required.");
    }
    if (dto.getDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required.");
    }
    if (dto.getPlaceId() == null || dto.getPlaceId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "place_id is required.");
    }
    if (dto.getPlaceName() == null || dto.getPlaceName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "place_name is required.");
    }
    if (dto.getLat() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat is required.");
    }
    if (dto.getLng() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lng is required.");
    }
    if (dto.getTime() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "time is required.");
    }
    if (dto.getEndTime() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endTime is required.");
    }
    
    validateEventDateWithinTrip(dto.getDate(), trip);
    
  }

  private void validateEventPutDTO(EventPutDTO dto, Trip trip) {
    if (dto.getEventTitle() == null || dto.getEventTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventTitle is required.");
    }
    if (dto.getDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "date is required.");
    }
    if (dto.getPlaceId() == null || dto.getPlaceId().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "place_id is required.");
    }
    if (dto.getPlaceName() == null || dto.getPlaceName().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "place_name is required.");
    }
    if (dto.getLat() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lat is required.");
    }
    if (dto.getLng() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lng is required.");
    }
    validateEventDateWithinTrip(dto.getDate(), trip);
  }

  public boolean hasTimeConflict(Event a, Event b) {
    if (!a.getDate().equals(b.getDate())) return false;
    return a.getTime().isBefore(b.getEndTime()) && b.getTime().isBefore(a.getEndTime());
  }

  public void autoEnrollMembersForNewEvent(Event newEvent, Trip trip) {
    List<Membership> memberships = membershipRepository.findByTrip(trip);

    for (Membership membership : memberships) {
      User member = membership.getUser();

      List<EventMember> existingMemberships = eventMemberRepository
        .findByUserAndTripId(member, trip.getTripId());

      boolean hasConflict = existingMemberships.stream()
        .filter(em -> em.getParticipationStatus() == ParticipationStatus.JOINED)
        .anyMatch(em -> hasTimeConflict(newEvent, em.getEvent()));

      if (!hasConflict) {
        EventMember em = new EventMember();
        em.setEvent(newEvent);
        em.setUser(member);
        em.setParticipationStatus(ParticipationStatus.JOINED);
        eventMemberRepository.save(em);
      }
    }
  }



  public void autoEnrollSingleMember(Event event, User member) {
    if (eventMemberRepository.findByEventAndUser(event, member).isPresent()) return;

    EventMember em = new EventMember();
    em.setEvent(event);
    em.setUser(member);
    em.setParticipationStatus(ParticipationStatus.JOINED);
    eventMemberRepository.save(em);
  }

   
  public EventGetDTO joinEvent(Long tripId, Long eventId, User requestingUser) {
    findTripOrThrow(tripId);
    Event event = findEventOrThrow(eventId);
    validateEventBelongsToTrip(event, tripId);
    validateTripMember(tripId, requestingUser);

    EventMember eventMember = eventMemberRepository
      .findByEventAndUser(event, requestingUser)
      .orElseGet(() -> {
          EventMember em = new EventMember();
          em.setEvent(event);
          em.setUser(requestingUser);
          return em;
      });
    eventMember.setParticipationStatus(ParticipationStatus.JOINED);
    eventMemberRepository.save(eventMember);

    List<EventMember> existingMemberships = eventMemberRepository
        .findByUserAndTripId(requestingUser, tripId);

    for (EventMember em : existingMemberships) {
      if (!em.getEvent().getEventId().equals(eventId)
              && em.getParticipationStatus() == ParticipationStatus.JOINED
              && hasTimeConflict(event, em.getEvent())) {
        em.setParticipationStatus(ParticipationStatus.DISMISSED);
        eventMemberRepository.save(em);
      }
    }

    return toEventGetDTO(event, requestingUser);
  }

  public EventGetDTO dismissEvent(Long tripId, Long eventId, User requestingUser, boolean fromConflictFlow) {
    findTripOrThrow(tripId);
    Event event = findEventOrThrow(eventId);
    validateEventBelongsToTrip(event, tripId);
    validateTripMember(tripId, requestingUser);

    EventMember eventMember = eventMemberRepository
        .findByEventAndUser(event, requestingUser)
        .orElseGet(() -> {
            EventMember em = new EventMember();
            em.setEvent(event);
            em.setUser(requestingUser);
            return em;
        });

    eventMember.setParticipationStatus(
        fromConflictFlow ? ParticipationStatus.DISMISSED : ParticipationStatus.OPTED_OUT
    );
    eventMemberRepository.save(eventMember);

    return toEventGetDTO(event, requestingUser);
  }



  private EventGetDTO toEventGetDTO(Event event, User callingUser) {
    EventGetDTO dto = DTOMapper.INSTANCE.convertEntityToEventGetDTO(event);

    List<EventMember> eventMembers = eventMemberRepository.findByEvent(event);
    List<EventMemberDTO> memberDTOs = eventMembers.stream()
        .filter(em -> em.getParticipationStatus() == ParticipationStatus.JOINED)
        .map(em -> {
            EventMemberDTO memberDTO = new EventMemberDTO();
            memberDTO.setUserId(em.getUser().getUserId());
            memberDTO.setUsername(em.getUser().getUsername());
            return memberDTO;
        })
        .collect(Collectors.toList());
    dto.setMembers(memberDTOs);

    if (callingUser != null) {
      Optional<EventMember> userMembership = eventMembers.stream()
          .filter(em -> em.getUser().getUserId().equals(callingUser.getUserId()))
          .findFirst();
      dto.setUserStatus(userMembership
          .map(em -> em.getParticipationStatus().name())
          .orElse("NONE"));

      List<EventMember> userJoinedElsewhere = eventMemberRepository
          .findByUserAndTripId(callingUser, event.getTrip().getTripId())
          .stream()
          .filter(em -> !em.getEvent().getEventId().equals(event.getEventId())
                  && em.getParticipationStatus() == ParticipationStatus.JOINED)
          .collect(Collectors.toList());

      boolean hasConflict = userJoinedElsewhere.stream()
          .anyMatch(em -> hasTimeConflict(event, em.getEvent()));
      dto.setHasConflict(hasConflict);
    } else {
      dto.setUserStatus("NONE");
      dto.setHasConflict(false);
    }

    return dto;
  }

  

}