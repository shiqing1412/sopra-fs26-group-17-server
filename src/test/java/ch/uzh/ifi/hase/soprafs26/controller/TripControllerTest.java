package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPreviewDTO;
import ch.uzh.ifi.hase.soprafs26.service.TripService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@WebMvcTest(TripController.class)
public class TripControllerTest {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_TOKEN = "Bearer validtoken";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TripService tripService;

    @MockitoBean
    private UserService userService;

    private User mockUser() {
        User user = new User();
        user.setUsername("testuser");
        return user;
    }

    private Trip mockTrip() {
        Trip trip = new Trip();
        trip.setTripId(1L);
        trip.setTripTitle("Test Trip");
        trip.setStartDate(LocalDate.of(2027, 6, 1));
        trip.setEndDate(LocalDate.of(2027, 6, 10));
        trip.setShareCode("abc12345");
        return trip;
    }

    @Test
    public void joinTripInvalidTokenReturns404() throws Exception {
        when(userService.validateToken(anyString())).thenReturn(mockUser());
        when(tripService.joinTrip(eq("badtoken"), any(User.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        
        mockMvc.perform(post("/trips/join/badtoken")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    public void createTripValidInputReturns201() throws Exception {
        when(userService.validateToken(anyString())).thenReturn(mockUser());
        when(tripService.createTrip(any(), any(User.class))).thenReturn(mockTrip());

        String body = "{\"tripTitle\":\"Test Trip\",\"startDate\":\"2027-06-01\",\"endDate\":\"2027-06-10\"}";

        mockMvc.perform(post("/trips")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    public void getTripByIdValidIdReturns200() throws Exception {
        User user = mockUser();
        Trip trip = mockTrip();
        when(userService.validateToken(anyString())).thenReturn(user);
        when(tripService.getAuthorizedTrip(eq(1L), any(User.class))).thenReturn(trip);
        when(tripService.getTripMembers(eq(1L), any(User.class))).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/trips/1")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTripByIdUnauthorizedReturns403() throws Exception {
        when(userService.validateToken(anyString())).thenReturn(mockUser());
        when(tripService.getAuthorizedTrip(eq(1L), any(User.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN));

        mockMvc.perform(get("/trips/1")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getAllTripsReturns200() throws Exception {
        when(userService.validateToken(anyString())).thenReturn(mockUser());
        when(tripService.getTripsForUser(any(User.class))).thenReturn(List.of(mockTrip()));

        mockMvc.perform(get("/trips")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTripMembersValidIdReturns200() throws Exception {
        TripMemberDTO member = new TripMemberDTO();
        member.setUserId(1L);
        member.setUsername("testuser");
        member.setRole("OWNER");

        when(userService.validateToken(anyString())).thenReturn(mockUser());
        when(tripService.getTripMembers(eq(1L), any(User.class))).thenReturn(List.of(member));

        mockMvc.perform(get("/trips/1/members")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTripPreviewValidShareCodeReturns200() throws Exception {
        TripPreviewDTO preview = new TripPreviewDTO();
        preview.setTripId(1L);
        preview.setTripTitle("Test Trip");
        preview.setStartDate(LocalDate.of(2026, 6, 1));
        preview.setEndDate(LocalDate.of(2026, 6, 10));
        preview.setIllustration(null);

        when(tripService.getTripPreview("abc12345")).thenReturn(preview);
    
        mockMvc.perform(get("/trips/join/abc12345/preview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getTripPreviewInvalidShareCodeReturns404() throws Exception {
        when(tripService.getTripPreview("badcode"))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/trips/join/badcode/preview")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createTrip_invalidTripTitle_tooLong_returnsBadRequestWithMessage() throws Exception {
        String body = "{\"tripTitle\":\"" + "a".repeat(256) + "\",\"startDate\":\"2027-06-01\",\"endDate\":\"2027-06-10\"}";

        mockMvc.perform(post("/trips")
                        .header(AUTH_HEADER, AUTH_TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Trip title must be at most 255 characters.")));
    }
}
