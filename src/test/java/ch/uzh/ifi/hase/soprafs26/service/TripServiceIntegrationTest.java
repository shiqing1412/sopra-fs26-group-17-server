package ch.uzh.ifi.hase.soprafs26.service;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripJoinResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TripServiceIntegrationTest {

    private static final String SHARE_CODE = "INTCODE1";

    @Autowired
    private TripService tripService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private MembershipRepository membershipRepository;

    private User user;
    private Trip trip;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setUsername("integrationuser");
        user.setPassword("password");
        user.setStatus(UserStatus.ONLINE);
        userRepository.save(user);

        trip = new Trip();
        trip.setTripTitle("Integration Trip");
        trip.setStartDate(LocalDate.of(2026, 7, 1));
        trip.setEndDate(LocalDate.of(2026, 7, 10));
        trip.setShareCode(SHARE_CODE);
        trip.setOwner(user);
        tripRepository.save(trip);
    }

    @Test
    public void joinTripAlreadyMemberDoesNotCreateDuplicateMembership() {
        TripJoinResponseDTO firstCall = tripService.joinTrip(SHARE_CODE, user);
        assertFalse(firstCall.isAlreadyMember());

        TripJoinResponseDTO secondCall = tripService.joinTrip(SHARE_CODE, user);
        assertTrue(secondCall.isAlreadyMember());

        long membershipCount = membershipRepository.findByTrip(trip).stream()
                .filter(m -> m.getUser().getUserId().equals(user.getUserId()))
                .count();
        assertEquals(1, membershipCount);
    }

    @Test
    public void joinTripNewMemberCreatesMembershipInDb() {
        tripService.joinTrip(SHARE_CODE, user);

        List<?> memberships = membershipRepository.findByTrip(trip);
        assertEquals(1, memberships.size());
    }

    @Test
    public void createTripPersistsOwnerMembership() {
        TripPostDTO dto = new TripPostDTO();
        dto.setTripTitle("New Trip");
        dto.setStartDate(LocalDate.of(2026, 8, 1));
        dto.setEndDate(LocalDate.of(2026, 8, 10));

        Trip created = tripService.createTrip(dto, user);

        long ownerMemberships = membershipRepository.findByTrip(created).stream()
                .filter(m -> "OWNER".equals(m.getRole()))
                .count();
        assertEquals(1, ownerMemberships);
    }
}
