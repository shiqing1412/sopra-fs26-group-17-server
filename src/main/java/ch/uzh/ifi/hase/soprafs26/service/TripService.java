package ch.uzh.ifi.hase.soprafs26.service;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;


@Service
@Transactional
public class TripService {
    
    private final Logger log = LoggerFactory.getLogger(TripService.class);
    private final TripRepository tripRepository;
    private final MembershipRepository membershipRepository;

    public TripService(TripRepository tripRepository, MembershipRepository membershipRepository) {
        this.tripRepository = tripRepository;
        this.membershipRepository = membershipRepository;
    }

    public Trip createTrip(TripPostDTO tripPostDTO, User owner) {
        Trip newTrip = new Trip();
        newTrip.setTripTitle(tripPostDTO.getTripTitle());
        newTrip.setStartDate(tripPostDTO.getStartDate());
        newTrip.setEndDate(tripPostDTO.getEndDate());
        
        validateTripDates(newTrip.getStartDate(), newTrip.getEndDate());
        newTrip.setOwner(owner);
        newTrip.setShareCode(generateShareCode());
        newTrip = tripRepository.save(newTrip);

        //create membership for the owner
        Membership membership = new Membership();
        membership.setTrip(newTrip);
        membership.setUser(owner);
        membership.setRole("OWNER");
        membership.setJoinedAt(LocalDateTime.now());
        membershipRepository.save(membership);

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

    private String generateShareCode() {
        String shareCode;
        do {
            shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (tripRepository.existsByShareCode(shareCode));
        return shareCode;
    }

}


