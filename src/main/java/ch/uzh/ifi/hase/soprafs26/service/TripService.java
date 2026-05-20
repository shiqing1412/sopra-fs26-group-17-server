package ch.uzh.ifi.hase.soprafs26.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

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
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;
import ch.uzh.ifi.hase.soprafs26.entity.Event;
import ch.uzh.ifi.hase.soprafs26.repository.EventRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPreviewDTO;

@Service
@Transactional
public class TripService {
    
    private final TripRepository tripRepository;
    private final MembershipRepository membershipRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;

    public TripService(TripRepository tripRepository, MembershipRepository membershipRepository, EventRepository eventRepository,
                   EventService eventService) {
        this.tripRepository = tripRepository;
        this.membershipRepository = membershipRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
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
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Start date must not be in the past."); //400 Bad Request
        }
    }

    private String generateShareCode() {
        String shareCode;
        do {
            shareCode = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        } while (tripRepository.existsByShareCode(shareCode));
        return shareCode;
    }

    private Trip getTripByShareCode(String shareCode) {
        return tripRepository.findByShareCode(shareCode)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found with the provided share code."));
    }

    public TripPreviewDTO getTripPreview(String joinToken) {
        Trip trip = getTripByShareCode(joinToken);
        TripPreviewDTO preview = new TripPreviewDTO();
        preview.setTripId(trip.getTripId());
        preview.setTripTitle(trip.getTripTitle());
        preview.setStartDate(trip.getStartDate());
        preview.setEndDate(trip.getEndDate());
        preview.setIllustration(null);
        return preview;
    }

    public TripJoinResponseDTO joinTrip(String joinToken, User currentUser){
        Trip trip = getTripByShareCode(joinToken);
        boolean alreadyMember = membershipRepository.existsByTripAndUser(trip, currentUser);

        if (!alreadyMember) {
            Membership membership = new Membership();
            membership.setTrip(trip);
            membership.setUser(currentUser);
            membership.setRole("MEMBER");
            membership.setJoinedAt(LocalDateTime.now());
            membershipRepository.save(membership);
            autoEnrollNewMemberForExistingEvents(currentUser, trip);
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
    
    public List<TripMemberDTO> getTripMembers(Long tripId, User currentUser) {
        Trip trip = getAuthorizedTrip(tripId, currentUser);
        List<Membership> memberships = membershipRepository.findByTrip(trip);
        
        List<TripMemberDTO> members = new ArrayList<>();
        for (Membership membership : memberships) {
            TripMemberDTO memberDTO = new TripMemberDTO();
            memberDTO.setUserId(membership.getUser().getUserId());
            memberDTO.setUsername(membership.getUser().getUsername());
            memberDTO.setRole(membership.getRole());
            memberDTO.setStatus(membership.getUser().getStatus().toString());
            members.add(memberDTO);
        }
        
        return members;
    }

    // validate that the user is a member of the trip before returning the trip details
    public Trip getAuthorizedTrip(Long tripId, User currentUser) {
        Trip trip = getTripById(tripId);
        checkMembership(trip, currentUser);
        return trip;
    }


    private void autoEnrollNewMemberForExistingEvents(User newMember, Trip trip) {
        List<Event> tripEvents = eventRepository
            .findByTrip_TripIdOrderByDateAscTimeAsc(trip.getTripId());

        List<Event> enrolledSoFar = new ArrayList<>();

        for (Event event : tripEvents) {
            boolean hasConflict = enrolledSoFar.stream()
                .anyMatch(enrolled -> eventService.hasTimeConflict(event, enrolled));

            if (!hasConflict) {
                eventService.autoEnrollSingleMember(event, newMember);
                enrolledSoFar.add(event);
            }
        }
    }

    
    public void leaveTrip(Long tripId, User currentUser) {
        Trip trip = getTripById(tripId);
        checkMembership(trip, currentUser);

        boolean isOwner = trip.getOwner().getUserId().equals(currentUser.getUserId());

        if (isOwner) {
            long memberCount = membershipRepository.countByTrip(trip);
            if (memberCount > 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Trip owner cannot leave while other members remain. Transfer ownership first.");
            }
            tripRepository.delete(trip);
        } else {
            Membership membership = membershipRepository.findByTripAndUser(trip, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not a member of this trip."));
            membershipRepository.delete(membership);
        }
    }
    @Transactional
    public void deleteTrip(Long tripId, User currentUser) {
        Trip trip = getTripById(tripId);

        if (!trip.getOwner().getUserId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only the trip owner can delete this trip.");
        }

        eventRepository.deleteAllByTrip(trip);
        membershipRepository.deleteAllByTrip(trip);
        tripRepository.delete(trip);
    }

    public Long transferOwnership(Long tripId, Long newOwnerUserId, User currentUser) {
        Trip trip = getTripById(tripId);

        if (!trip.getOwner().getUserId().equals(currentUser.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Only the current trip owner can transfer ownership.");
        }

        if (currentUser.getUserId().equals(newOwnerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "You are already the owner of this trip.");
        }

        Membership currentOwnerMembership = membershipRepository.findByTripAndUser(trip, currentUser)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Owner membership record not found."));
        currentOwnerMembership.setRole("MEMBER");
        membershipRepository.save(currentOwnerMembership);

        Membership newOwnerMembership = membershipRepository
            .findByTripIdAndUserId(tripId, newOwnerUserId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Target user is not a member of this trip."));
        newOwnerMembership.setRole("OWNER");
        membershipRepository.save(newOwnerMembership);

        trip.setOwner(newOwnerMembership.getUser());
        tripRepository.save(trip);

        return newOwnerUserId;
    }

}


