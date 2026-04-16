package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class EventController {
  private final EventService eventService;
  private final UserService userService;

  public EventController(EventService eventService, UserService userService) {
    this.eventService = eventService;
    this.userService = userService;
  }

  @GetMapping("/{tripId}/events")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public List<DayDTO> getEventsGroupedByDay(
        @PathVariable Long tripId,
        @RequestHeader("Authorization") String token) {

    User requestingUser = userService.validateToken(token);
    return eventService.getEventsGroupedByDay(tripId, requestingUser);
  }

  @PutMapping("/{tripId}/events/{eventId}")
  @ResponseStatus(HttpStatus.OK)
  @ResponseBody
  public EventGetDTO updateEvent(
        @PathVariable Long tripId,
        @PathVariable Long eventId,
        @RequestHeader("Authorization") String token,
        @RequestBody EventPutDTO eventPutDTO) {
          User requestingUser = userService.validateToken(token);
          return eventService.updateEvent(eventId, eventPutDTO);
  }
}