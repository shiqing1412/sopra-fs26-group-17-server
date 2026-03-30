package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public Trip createTrip(TripPostDTO tripPostDTO) {
        Trip newTrip = new Trip();
        newTrip.setTripTitle(tripPostDTO.getTripTitle());
        newTrip.setStartDate(tripPostDTO.getStartDate());
        newTrip.setEndDate(tripPostDTO.getEndDate());

        newTrip = tripRepository.save(newTrip); //
        return newTrip;
    }
}
    

