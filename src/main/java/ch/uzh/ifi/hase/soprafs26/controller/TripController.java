package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.TripService;



@RestController
@RequestMapping("/trips")
public class TripController {

	private final TripService tripService;

	public TripController(TripService tripService) {
		this.tripService = tripService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED) // 201 CREATED
	@ResponseBody
	public TripGetDTO createTrip(@RequestBody TripPostDTO tripPostDTO) {
		
		Trip createdTrip = tripService.createTrip(tripPostDTO);
		
		return 	convertEntityToTripGetDTO(createdTrip);
	}

	private TripGetDTO convertEntityToTripGetDTO(Trip trip) {
		TripGetDTO dto = new TripGetDTO();
		dto.setTripId(trip.getTripId());
		dto.setTripTitle(trip.getTripTitle());
		dto.setStartDate(trip.getStartDate());
		dto.setEndDate(trip.getEndDate());
		return dto;
	}

}
