package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripJoinResponseDTO;
import ch.uzh.ifi.hase.soprafs26.service.TripService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;



@RestController
@RequestMapping("/trips")
public class TripController {

	private final TripService tripService;
	private final UserService userService;

	public TripController(TripService tripService, UserService userService) {
		this.tripService = tripService;
		this.userService = userService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED) // 201 CREATED
	@ResponseBody
	public TripGetDTO createTrip(@RequestBody TripPostDTO tripPostDTO, @RequestHeader("Authorization") String token) {
		
		User currentUser = userService.validateToken(token);
		Trip createdTrip = tripService.createTrip(tripPostDTO, currentUser);	
		
		return 	DTOMapper.INSTANCE.convertEntityToTripGetDTO(createdTrip);
	}

	@PostMapping ("/join/{joinToken}")
	@ResponseStatus(HttpStatus.OK) // 200 OK
	@ResponseBody
	public TripJoinResponseDTO joinTrip(@PathVariable String joinToken, @RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		return tripService.joinTrip(joinToken, currentUser);
	}

	@GetMapping("/{tripId}")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public TripGetDTO getTripById(@PathVariable Long tripId, @RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		Trip trip = tripService.getTripById(tripId);
		tripService.checkMembership(trip, currentUser);
		return DTOMapper.INSTANCE.convertEntityToTripGetDTO(trip);
	}

}
