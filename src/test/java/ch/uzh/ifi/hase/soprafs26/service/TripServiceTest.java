package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.Membership;
import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.repository.TripRepository;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;
import ch.uzh.ifi.hase.soprafs26.repository.MembershipRepository;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

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
validateTripDates() 400 if invalid dates

Not need to do: generateShareCode()
Not implemented service yet: deleteTrip()
*/

//Below haven't implemented yet, just setting up the test class and the necessary mocks//

public class TripServiceTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TripService tripService;

    private User testUser;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUsername");
        testUser.setPassword("password123");

        Mockito.when(userRepository.save(Mockito.any())).thenReturn(testUser);
    }
}


