package ch.uzh.ifi.hase.soprafs26.service;


import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripJoinResponseDTO;  
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.web.server.ResponseStatusException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/*Methods here:
getAuthorizedTrip() 200, 401
createTrip() 201, 400
getTripById() 200, 404
getTripsForUser() 200 (or empty list if no trips)
getTripMembers() 200  (NOT IN THE SPEC: 403 not a member or 404 Trip not found))
getAuthorizedTrip() 200, 403, 404
joinTrip() 200, 404

Helper methods:
checkMembership() 403 if not a member
validateTripDates() 400 -> although in spec, but due to it's a private helper method, no need to do tests

No need to do: generateShareCode()
Not implemented service yet: deleteTrip()

Line Coverage:94%
*/


@ExtendWith(MockitoExtension.class)
public class TripServiceTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private MembershipRepository membershipRepository;
    @InjectMocks
    private TripService tripService;

    private User owner;
    private User member;
    private User stranger;
    private Trip trip;
    private TripPostDTO tripPostDTO;

    @BeforeEach
    public void setup() {
        owner = new User();
        owner.setUserId(1L);
        owner.setUsername("owner");
        owner.setStatus(UserStatus.ONLINE);

        member = new User();
        member.setUserId(2L);
        member.setUsername("member");
        member.setStatus(UserStatus.ONLINE);

        stranger = new User();
        stranger.setUserId(99L);
        stranger.setUsername("stranger");
        stranger.setStatus(UserStatus.OFFLINE);

        trip = new Trip();
        trip.setTripId(10L);
        trip.setTripTitle("Japan Trip");
        trip.setStartDate(LocalDate.of(2026, 4, 1));
        trip.setEndDate(LocalDate.of(2026, 4, 10));
        trip.setOwner(owner);
        trip.setShareCode("ABC12345");

        tripPostDTO = new TripPostDTO();
        tripPostDTO.setTripTitle("Japan Trip");
        tripPostDTO.setStartDate(LocalDate.of(2026, 4, 1));
        tripPostDTO.setEndDate(LocalDate.of(2026, 4, 10));
    }

    @Test //getAuthorizedTrip() 200
    public void testGetAuthorizedTrip200() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(membershipRepository.existsByTripAndUser(trip, owner)).thenReturn(true);
        Trip authorizedTrip = tripService.getAuthorizedTrip(10L, owner);
        assertNotNull(authorizedTrip);
        assertEquals("Japan Trip", authorizedTrip.getTripTitle());
    }

    @Test //getAuthorizedTrip() 403 (in spec is 401(token missing/invalid), but here we check if user is not a member of the trip)
    public void testGetAuthorizedTrip403() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(membershipRepository.existsByTripAndUser(trip, stranger)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> tripService.getAuthorizedTrip(10L, stranger));
    }

    @Test //createTrip() 201
    public void testCreateTrip201() {
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);
        when(tripRepository.existsByShareCode(anyString())).thenReturn(false);
        Trip createdTrip = tripService.createTrip(tripPostDTO, owner);
        assertNotNull(createdTrip);
        assertEquals("Japan Trip", createdTrip.getTripTitle());
        assertEquals(owner, createdTrip.getOwner());
    }

    @Test //createTrip() 400
    public void testCreateTrip400() {
        tripPostDTO.setEndDate(LocalDate.of(2026, 3, 30)); // End date before start date
        assertThrows(ResponseStatusException.class, () -> tripService.createTrip(tripPostDTO, owner));
    }

    @Test //getTripById() 200
    public void testGetTripById200() {
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        Trip foundTrip = tripService.getTripById(10L);
        assertNotNull(foundTrip);
        assertEquals("Japan Trip", foundTrip.getTripTitle());
    }

    @Test //getTripById() 404
    public void testGetTripById404() {
        when(tripRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> tripService.getTripById(10L));
    }

    @Test //getTripsForUser() 200
    public void testGetTripsForUser200() {
        Membership membership1 = new Membership();
        membership1.setTrip(trip);
        membership1.setUser(owner);
        
        Trip trip2 = new Trip();
        trip2.setTripId(11L);
        trip2.setTripTitle("Korea Trip");
        
        Membership membership2 = new Membership();
        membership2.setTrip(trip2);
        membership2.setUser(owner);
        
        when(membershipRepository.findByUser(owner)).thenReturn(List.of(membership1, membership2));
        List<Trip> trips = tripService.getTripsForUser(owner);
        assertNotNull(trips);
        assertEquals(2, trips.size());
    }

    @Test //getTripMembers() 200
    public void testGetTripMembers200() {
        Membership membership1 = new Membership();
        membership1.setUser(owner);
        Membership membership2 = new Membership();
        membership2.setUser(member);
        when(membershipRepository.existsByTripAndUser(trip, owner)).thenReturn(true);
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(membershipRepository.findByTrip(trip)).thenReturn(List.of(membership1, membership2));
        List<TripMemberDTO> members = tripService.getTripMembers(10L, owner);
        assertNotNull(members);
        assertEquals(2, members.size());
    }

    @Test //getTripMembers() 403 not a member
    public void testGetTripMembers403() {   
        when(tripRepository.findById(10L)).thenReturn(Optional.of(trip));
        when(membershipRepository.existsByTripAndUser(trip, stranger)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> tripService.getTripMembers(10L, stranger));
    }

    @Test //getTripMembers() 404 Trip not found
    public void testGetTripMembers404() {
        when(tripRepository.findById(10L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> tripService.getTripMembers(10L, owner));
    }

    @Test //joinTrip() 200
    public void testJoinTrip200() {
       when(tripRepository.findByShareCode("ABC12345")).thenReturn(Optional.of(trip));
       when(membershipRepository.existsByTripAndUser(trip, member)).thenReturn(false);
       
       TripJoinResponseDTO response = tripService.joinTrip("ABC12345", member);
       assertNotNull(response);
       assertEquals(10L, response.getTripId());
       assertEquals("Japan Trip", response.getTripTitle());
       assertFalse(response.isAlreadyMember());
    }

    @Test //joinTrip() 400 already a member
    public void testJoinTrip400() {
        when(tripRepository.findByShareCode("ABC12345")).thenReturn(Optional.of(trip));
        when(membershipRepository.existsByTripAndUser(trip, member)).thenReturn(true);

        TripJoinResponseDTO response = tripService.joinTrip("ABC12345", member);

        assertTrue(response.isAlreadyMember());
        verify(membershipRepository, never()).save(any(Membership.class));
    }

    @Test //joinTrip() 404 Trip not found
    public void testJoinTrip404() {
        when(tripRepository.findByShareCode("INVALID_CODE")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> tripService.joinTrip("INVALID_CODE", member));
    }

    @Test //checkMembership() 403 if not a member
    public void testCheckMembership403() {
        when(membershipRepository.existsByTripAndUser(trip, stranger)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> tripService.checkMembership(trip, stranger));
    }

}


