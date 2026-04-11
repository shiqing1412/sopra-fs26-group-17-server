package ch.uzh.ifi.hase.soprafs26.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final TripService tripService;

    public EventService(EventRepository eventRepository, TripService tripService) {
        this.eventRepository = eventRepository;
        this.tripService = tripService;
    }

    public List<DayDTO> getEventsGroupedByDay(Long tripId, User requestingUser) {
        Trip trip = tripService.getTripById(tripId);
        tripService.checkMembership(trip, requestingUser);

        List<Event> events = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(tripId);

        Map<LocalDate, List<EventGetDTO>> byDate = events.stream()
            .collect(Collectors.groupingBy(
                Event::getDate,
                Collectors.mapping(
                    DTOMapper.INSTANCE::convertEntityToEventGetDTO,
                    Collectors.toList()
                )
            ));

        // One DayDTO per day in trip range, empty list if no events
        List<DayDTO> days = new ArrayList<>();
        LocalDate cursor = trip.getStartDate();
        while (!cursor.isAfter(trip.getEndDate())) {
            days.add(new DayDTO(cursor, byDate.getOrDefault(cursor, List.of())));
            cursor = cursor.plusDays(1);
        }

        return days;
    }
}