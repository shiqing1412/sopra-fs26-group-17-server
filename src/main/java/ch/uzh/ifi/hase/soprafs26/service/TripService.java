package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

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
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripJoinResponseDTO;

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

    public TripJoinResponseDTO joinTrip(String joinToken, User currentUser){
        Trip trip = tripRepository.findByShareCode(joinToken)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found with the provided share code."));

        boolean alreadyMember = membershipRepository.existsByTripAndUser(trip, currentUser);

        if (!alreadyMember) {
            Membership membership = new Membership();
            membership.setTrip(trip);
            membership.setUser(currentUser);
            membership.setRole("MEMBER");
            membership.setJoinedAt(LocalDateTime.now());
            membershipRepository.save(membership);
        }

        
        TripJoinResponseDTO response = new TripJoinResponseDTO();
        response.setTripId(trip.getTripId());
        response.setTripTitle(trip.getTripTitle());
        response.setAlreadyMember(alreadyMember);

        return response;
    }

    public void checkMembership(Trip trip, User user) {
    boolean isMember = membershipRepository.existsByTripAndUser(trip, user);
    boolean isOwner = trip.getOwner().getUserId().equals(user.getUserId());
    if (!isMember && !isOwner) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
            "You are not a member of this trip.");
        }
    }

    public Trip getTripById(Long tripId) {
        return tripRepository.findById(tripId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Trip not found."));
    }

    public List<Trip> getTripsForUser(User user){
        List<Membership> memberships = membershipRepository.findByUser(user);
        
        List<Trip> trips = new ArrayList<>();
        for (Membership membership : memberships) {
            trips.add(membership.getTrip());
            }
            
        return trips;
    } 

    


}


