package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.entity.Location;

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

  public EventService(EventRepository eventRepository,
                      TripRepository tripRepository,
                      MembershipRepository membershipRepository) {
    this.eventRepository = eventRepository;
    this.tripRepository = tripRepository;
    this.membershipRepository = membershipRepository;
  }

  public List<DayDTO> getEventsGroupedByDay(Long tripId, User requestingUser) {
  
    Trip trip = findTripOrThrow(tripId); //404 if not found
    /**Trip trip = tripRepository.findById(tripId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Trip not found."));**/
        
    // Membership check    
    boolean isMember = membershipRepository
      .existsByTripIdAndUserId(tripId, requestingUser.getUserId());
    boolean isOwner = trip.getOwner().getUserId().equals(requestingUser.getUserId());

    if (!isMember && !isOwner) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a member of this trip.");
    }

    //Fetch all events for this trip, already sorted by date asc, time asc
    List<Event> events = eventRepository
      .findByTrip_TripIdOrderByDateAscTimeAsc(tripId);

    // Group events by date into a map
    Map<LocalDate, List<EventGetDTO>> byDate = events.stream()
      .collect(Collectors.groupingBy(
        Event::getDate,
        Collectors.mapping(
          DTOMapper.INSTANCE::convertEntityToEventGetDTO,
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

    return days;
  }



  public EventGetDTO createEvent(Long tripId, EventPostDTO dto, User creator) {
    
    validateEventPostDTO(dto, null);
    /**
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
    **/
    /**
    if (dto.getDate().isBefore(trip.getStartDate()) || dto.getDate().isAfter(trip.getEndDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Event date is outside the trip's date range.");
    }**/

    Trip trip = findTripOrThrow(tripId);
    /**Trip trip = tripRepository.findById(tripId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Trip not found."));**/
    
    validateTripMember(tripId, creator);
    /**boolean isMember = membershipRepository.existsByTripIdAndUserId(tripId, creator.getUserId());
    if (!isMember) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a member of this trip.");
    }**/

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
    return DTOMapper.INSTANCE.convertEntityToEventGetDTO(saved);
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

    if(event.getLocation() == null) {
      event.setLocation(new Location());
    }
    event.getLocation().setPlaceId(dto.getPlaceId());
    event.getLocation().setName(dto.getPlaceName());
    event.getLocation().setLat(dto.getLat());
    event.getLocation().setLng(dto.getLng());

    Event updatedEvent = eventRepository.save(event);
    return DTOMapper.INSTANCE.convertEntityToEventGetDTO(updatedEvent);
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

  private void validateTripMember(Long tripId, User user) {
    boolean isMember = membershipRepository.existsByTripIdAndUserId(tripId, user.getUserId());
    if (!isMember) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a member of this trip.");//403
      }
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

}