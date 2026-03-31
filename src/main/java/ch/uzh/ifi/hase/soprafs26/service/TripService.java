package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;


@Service
@Transactional
public class TripService {
    
    private final Logger log = LoggerFactory.getLogger(TripService.class);
    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip createTrip(TripPostDTO tripPostDTO, User creator) {
        Trip newTrip = new Trip();
        newTrip.setTripTitle(tripPostDTO.getTripTitle());
        newTrip.setStartDate(tripPostDTO.getStartDate());
        newTrip.setEndDate(tripPostDTO.getEndDate());
        
        validateTripDates(newTrip.getStartDate(), newTrip.getEndDate());
        newTrip.setOwner(creator);
        newTrip = tripRepository.save(newTrip); //
        return newTrip;
    }

    private void validateTripDates(LocalDate startDate, LocalDate endDate) {
        
        if (startDate == null || endDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start date and end date must not be null."); //400 Bad Request
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start date must be before end date."); //400 Bad Request
        }
        /*if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start date must not be in the past."); //400 Bad Request
        }*/
    }   

}


