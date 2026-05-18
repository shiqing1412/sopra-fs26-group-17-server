package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripDetailDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPreviewDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripJoinResponseDTO;
import ch.uzh.ifi.hase.soprafs26.service.TripService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;

import java.util.List;
import java.util.ArrayList;
import jakarta.validation.Valid;



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
	public TripGetDTO createTrip(
		@Valid @RequestBody TripPostDTO tripPostDTO, 
		@RequestHeader("Authorization") String token) {
		
		User currentUser = userService.validateToken(token);
		Trip createdTrip = tripService.createTrip(tripPostDTO, currentUser);	
		
		return 	DTOMapper.INSTANCE.convertEntityToTripGetDTO(createdTrip);
	}

	@PostMapping ("/join/{joinToken}")
	@ResponseStatus(HttpStatus.OK)
	public TripJoinResponseDTO joinTrip(
		@PathVariable("joinToken") String joinToken, 
		@RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		return tripService.joinTrip(joinToken, currentUser);
	}

	@GetMapping("/join/{joinToken}/preview")
	@ResponseStatus(HttpStatus.OK)
	public TripPreviewDTO getTripPreview(
		@PathVariable("joinToken") String joinToken) {
		return tripService.getTripPreview(joinToken);
	}

	@GetMapping("/{tripId}")
	@ResponseStatus(HttpStatus.OK)
	public TripDetailDTO getTripById(
		@PathVariable("tripId") Long tripId, 
		@RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		Trip trip = tripService.getAuthorizedTrip(tripId, currentUser);
		List<TripMemberDTO> members = tripService.getTripMembers(tripId, currentUser);
		TripDetailDTO tripDetailDTO = DTOMapper.INSTANCE.convertEntityToTripDetailDTO(trip);
		tripDetailDTO.setMembers(members);
		return tripDetailDTO;
	}

	@GetMapping //show all trips of the user
	@ResponseStatus(HttpStatus.OK)
	public List<TripGetDTO> getAllTrips(
		@RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		List<TripGetDTO> tripGetDTOs = new ArrayList<>();
		for (Trip trip : tripService.getTripsForUser(currentUser)) {
			tripGetDTOs.add(DTOMapper.INSTANCE.convertEntityToTripGetDTO(trip));
		}
		return tripGetDTOs;
	}

	@GetMapping("/{tripId}/members")
	@ResponseStatus(HttpStatus.OK)
	public List<TripMemberDTO> getTripMembers(
		@PathVariable("tripId") Long tripId, 
		@RequestHeader("Authorization") String token) {
		User currentUser = userService.validateToken(token);
		return tripService.getTripMembers(tripId, currentUser);
	}

	@DeleteMapping("/{tripId}/members/me")
    @ResponseStatus(HttpStatus.OK)
    public java.util.Map<String, String> leaveTrip(
			@PathVariable("tripId") Long tripId,
			@RequestHeader("Authorization") String token) {

			User currentUser = userService.validateToken(token);
			tripService.leaveTrip(tripId, currentUser);
			return java.util.Map.of("message", "Successfully left the trip.");
	}	

	@DeleteMapping("/{tripId}")
	@ResponseStatus(HttpStatus.OK)
	public java.util.Map<String, String> deleteTrip(
		@PathVariable("tripId") Long tripId,
		@RequestHeader("Authorization") String token) {

		User currentUser = userService.validateToken(token);
		tripService.deleteTrip(tripId, currentUser);
		return java.util.Map.of("message", "Trip successfully deleted.");
	}

	@PatchMapping("/{tripId}/members/{userId}/owner")
	@ResponseStatus(HttpStatus.OK)
	public java.util.Map<String, Long> transferOwnership(
		@PathVariable("tripId") Long tripId,
		@PathVariable("userId") Long userId,
		@RequestHeader("Authorization") String token) {

		User currentUser = userService.validateToken(token);
		Long newOwnerId = tripService.transferOwnership(tripId, userId, currentUser);
		return java.util.Map.of("new_owner_id", newOwnerId);
	}
}

