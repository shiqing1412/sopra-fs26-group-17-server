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
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.entity.Location;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;

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
    //Resolve trip or 404
    Trip trip = tripRepository.findById(tripId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Trip not found."));

    //Membership check
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
    if (dto.getEventTitle() == null || dto.getEventTitle().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventTitle is required.");
    }
    if (dto.getDayDate() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "day_date is required.");
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
    
    Trip trip = tripRepository.findById(tripId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Trip not found."));

    boolean isMember = membershipRepository.existsByTripIdAndUserId(tripId, creator.getUserId());
    if (!isMember) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "You are not a member of this trip.");
    }

    if (dto.getDayDate().isBefore(trip.getStartDate()) || dto.getDayDate().isAfter(trip.getEndDate())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "Event date is outside the trip's date range.");
    }

    Event event = DTOMapper.INSTANCE.convertEventPostDTOtoEntity(dto);

    Location location = new Location();
    location.setPlaceId(dto.getPlaceId());
    location.setName(dto.getPlaceName());
    location.setLatitude(dto.getLat());
    location.setLongitude(dto.getLng());

    event.setLocation(location);
    event.setCreator(creator);
    event.setTrip(trip);
    event.setCreatedAt(LocalDateTime.now());

    Event saved = eventRepository.save(event);
    return DTOMapper.INSTANCE.convertEntityToEventGetDTO(saved);
  }
}