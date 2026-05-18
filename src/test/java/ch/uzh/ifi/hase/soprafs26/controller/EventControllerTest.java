package ch.uzh.ifi.hase.soprafs26.controller;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.DayDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.EventPutDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.ItineraryPollingResponseDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripMemberDTO;
import ch.uzh.ifi.hase.soprafs26.service.EventService;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.ObjectMapper;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final String TOKEN = "test-token";
    private static final Long TRIP_ID = 1L;
    private static final Long EVENT_ID = 1L;
    private static final LocalDate EVENT_DATE = LocalDate.now().plusDays(1);
    private static final LocalTime EVENT_TIME = LocalTime.of(10,0);   
    private static final LocalTime EVENT_END_TIME = LocalTime.of(11, 59);

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper; // For JSON serialization/deserialization

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private UserService userService;

// Helper Methods
    private User mockUser() {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testuser");
        return user;
    }

    private EventPostDTO validEventPostDTO() {
        EventPostDTO dto = new EventPostDTO();
        dto.setEventTitle("New Event");
        dto.setDate(EVENT_DATE);
        dto.setTime(EVENT_TIME); 
        dto.setEndTime(EVENT_END_TIME);

        dto.setPlaceId("111");
        dto.setPlaceName("Zurich HB");
        dto.setLat(47.3769);
        dto.setLng(8.5417);
        dto.setNotes("Ticket");
        return dto;
    }

    private EventPutDTO validEventPutDTO() {
        EventPutDTO dto = new EventPutDTO();
        dto.setEventTitle("Updated Event");
        dto.setDate(EVENT_DATE);
        dto.setTime(EVENT_TIME); 
        dto.setEndTime(EVENT_END_TIME);
        dto.setPlaceId("111");
        dto.setPlaceName("Zurich HB SBB");
        dto.setLat(47.3769);
        dto.setLng(8.5417);
        dto.setNotes("New ticket");
        return dto;
    }

//Helper Methods of Response DTOs 
    //EventGetDTO
    private EventGetDTO mockEventGetDTO(){
        return mockEventGetDTO(EVENT_ID, "Event 1");
    }

    private EventGetDTO mockEventGetDTO(Long eventId, String title) {
        EventGetDTO dto = new EventGetDTO();
        dto.setEventId(eventId);
        dto.setEventTitle(title);
        dto.setDate(EVENT_DATE);
        dto.setTime(EVENT_TIME); 
        dto.setEndTime(EVENT_END_TIME);
        dto.setUserStatus("JOINED");
        dto.setHasConflict(false);
        dto.setMembers(List.of());
        return dto;
    }

    //TripMemberDTO
    private TripMemberDTO mockTripMemberDTO() {
        TripMemberDTO member = new TripMemberDTO();
        member.setUserId(1L);
        member.setUsername("testuser");
        member.setCurrentUser(true);
        member.setActive(true);
        return member;
    }

    //ItineraryPollingResponseDTO
    private ItineraryPollingResponseDTO mockItineraryPollingResponseDTO() {
        DayDTO day = new DayDTO(EVENT_DATE, List.of(mockEventGetDTO()));

        return new ItineraryPollingResponseDTO(
            List.of(day),
            List.of(mockTripMemberDTO())
        );
    }

    //Valid token
    private User mockValidToken() {
        User user = mockUser();
        Mockito.when(userService.validateToken(TOKEN)).thenReturn(user);
        return user;
    }
    
    //POST create event
    private ResultActions performCreateEvent(Object body) throws Exception {
        return mockMvc.perform(post("/trips/{tripId}/events", TRIP_ID)
            .header("Authorization", TOKEN)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(body)));
    }

    //GET events
    private ResultActions performGetEvents() throws Exception {
        return mockMvc.perform(get("/trips/{tripId}/events", TRIP_ID)
            .header("Authorization", TOKEN));
    }

    //PUT event
    private ResultActions performUpdateEvent(Object body) throws Exception {
        return mockMvc.perform(
            put("/trips/{tripId}/events/{eventId}", 
            TRIP_ID,EVENT_ID)
            .header("Authorization", TOKEN)
            .contentType("application/json")
            .content(objectMapper.writeValueAsString(body)));
        }

    //DELETE event
    private ResultActions performDeleteEvent() throws Exception {
        return mockMvc.perform(
            delete("/trips/{tripId}/events/{eventId}", 
            TRIP_ID, EVENT_ID)
            .header("Authorization", TOKEN));
        
    }

    //JOIN event
    private ResultActions performJoinEvent() throws Exception {
        return mockMvc.perform(
            post("/trips/{tripId}/events/{eventId}/join", 
            TRIP_ID, EVENT_ID)
            .header("Authorization", TOKEN));
    }

    //DISMISS event
    private ResultActions performDismissEvent(Long eventId, boolean conflict) throws Exception {
        return mockMvc.perform(
            delete("/trips/{tripId}/events/{eventId}/join", 
            TRIP_ID, eventId)
            .header("Authorization", TOKEN)
        .param("conflict", String.valueOf(conflict)));
    }

    private ResultActions performDismissEvent(boolean conflict) throws Exception {
        return performDismissEvent(EVENT_ID, conflict);
    }


    //===TESTS===
    //CREATE
    @Test
    void createEvent_validInput_Success201() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.createEvent(
            eq(TRIP_ID),
            any(EventPostDTO.class),
            eq(user)))
            .thenReturn(mockEventGetDTO(EVENT_ID, "Event 1"));
            
        performCreateEvent(validEventPostDTO())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventId").value(EVENT_ID))
            .andExpect(jsonPath("$.eventTitle").value("Event 1"));

        Mockito.verify(eventService).createEvent(
            eq(TRIP_ID),
            any(EventPostDTO.class),
            eq(user));
        }

    @Test
    void createEvent_notMember_Failure403() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.createEvent(
            eq(TRIP_ID),
            any(EventPostDTO.class),
            eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));
        
            performCreateEvent(validEventPostDTO())
            .andExpect(status().isForbidden());
    }
    
    @Test
    void createEvent_invalidBody_Failure400() throws Exception {
        User user = mockValidToken();
        EventPostDTO invalidRequest = validEventPostDTO();
        invalidRequest.setEventTitle(""); // Invalid title
        Mockito.when(eventService.createEvent(
            eq(TRIP_ID),
            any(EventPostDTO.class),
            eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event title is required"));
        
            performCreateEvent(invalidRequest)
            .andExpect(status().isBadRequest());
    }


    //GET
    @Test
    void getEvents_validMember_Success200() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.getEventsGroupedByDay(eq(TRIP_ID), eq(user)))
            .thenReturn(mockItineraryPollingResponseDTO());

        performGetEvents()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.days").isArray())
            .andExpect(jsonPath("$..members").isArray());

        Mockito.verify(eventService).getEventsGroupedByDay(TRIP_ID, user);
    }

    @Test
    void getEvents_notMember_Failure403() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.getEventsGroupedByDay(eq(TRIP_ID), eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        performGetEvents().andExpect(status().isForbidden());
    }

    @Test
    void getEvents_tripNotFound_Failure404() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.getEventsGroupedByDay(eq(TRIP_ID), eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

        performGetEvents().andExpect(status().isNotFound());
    }

    @Test //ETag
    void getEvents_withETag_Success304() throws Exception {
        User user = mockValidToken();
        ItineraryPollingResponseDTO responseDTO = mockItineraryPollingResponseDTO();
        String eTag = String.valueOf(
            responseDTO.getDays().hashCode() + responseDTO.getMembers().hashCode());
        //First request to get ETag
        Mockito.when(eventService.getEventsGroupedByDay(eq(TRIP_ID), eq(user)))
            .thenReturn(responseDTO);    
        performGetEvents()
            .andExpect(status().isOk())
            .andExpect(header().string("ETag","\"" + eTag + "\""));

        //Second request with If-None-Match header
        mockMvc.perform(get("/trips/{tripId}/events", TRIP_ID)
            .header("Authorization", TOKEN)
            .header("If-None-Match", "\"" + eTag + "\""))
            .andExpect(status().isNotModified());
    }


    //UPDATE
    @Test
    void updateEvent_validInput_Success204() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.updateEvent(
            eq(TRIP_ID),
            eq(EVENT_ID),
            any(EventPutDTO.class),
            eq(user)))
            .thenReturn(mockEventGetDTO(EVENT_ID, "Event 1"));

        performUpdateEvent(validEventPutDTO())
            .andExpect(status().isNoContent());
    
        Mockito.verify(eventService).updateEvent(
            eq(TRIP_ID),
            eq(EVENT_ID),
            any(EventPutDTO.class),
            eq(user));
    }

    @Test
    void updateEvent_notCreator_Failure403() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.updateEvent(
            eq(TRIP_ID),
            eq(EVENT_ID),
            any(EventPutDTO.class),
            eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the event creator can update the event"));

        performUpdateEvent(validEventPutDTO()).andExpect(status().isForbidden());
    }

    @Test
    void updateEvent_notFound_Failure404() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.updateEvent(
            eq(TRIP_ID),
            eq(EVENT_ID),
            any(EventPutDTO.class),
            eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));
        performUpdateEvent(validEventPutDTO()).andExpect(status().isNotFound());
    }

    @Test
    void updateEvent_invalidBody_Failure400() throws Exception {
        User user = mockValidToken();
        EventPutDTO invalidRequest = validEventPutDTO();
        invalidRequest.setEventTitle(""); // Invalid title
        Mockito.when(eventService.updateEvent(
            eq(TRIP_ID),
            eq(EVENT_ID),
            any(EventPutDTO.class),
            eq(user)))
            .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event title is required"));
        
            performUpdateEvent(invalidRequest).andExpect(status().isBadRequest());
    }

    // DELETE
    @Test
    void deleteEvent_validMember_Success204() throws Exception {
        User user = mockValidToken();
        performDeleteEvent().andExpect(status().isNoContent());
        Mockito.verify(eventService).deleteEvent(TRIP_ID, EVENT_ID, user);
     }

    @Test
    void deleteEvent_notCreator_Failure403() throws Exception {
        User user = mockValidToken();
        Mockito.doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip."))
            .when(eventService).deleteEvent(TRIP_ID, EVENT_ID, user);

        performDeleteEvent().andExpect(status().isForbidden());
    }
    
    @Test
    void deleteEvent_notFound_Failure404() throws Exception {
        User user = mockValidToken();
        Mockito.doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"))
            .when(eventService).deleteEvent(TRIP_ID, EVENT_ID, user);

        performDeleteEvent().andExpect(status().isNotFound());
    }
    
    //JOIN
    @Test
    void joinEvent_validInput_Success200() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.joinEvent(TRIP_ID, EVENT_ID, user))
            .thenReturn(mockEventGetDTO());

        performJoinEvent()
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventId").value(EVENT_ID))
            .andExpect(jsonPath("$.eventTitle").value("Event 1"));

        Mockito.verify(eventService).joinEvent(TRIP_ID, EVENT_ID, user);
     }

    @Test
    void joinEvent_notMember_Failure403() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.joinEvent(TRIP_ID, EVENT_ID, user))
            .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a member of this trip"));

        performJoinEvent().andExpect(status().isForbidden());
    }

    @Test
    void joinEvent_notFound_Failure404() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.joinEvent(TRIP_ID, EVENT_ID, user))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        performJoinEvent().andExpect(status().isNotFound());
    }
    

    //DISMISS
    @Test
    void dismissEvent_validInput_Success200() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.dismissEvent(TRIP_ID, EVENT_ID, user, false))
            .thenReturn(mockEventGetDTO());

        performDismissEvent(false)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.eventId").value(EVENT_ID))
            .andExpect(jsonPath("$.eventTitle").value("Event 1"));

        Mockito.verify(eventService).dismissEvent(TRIP_ID, EVENT_ID, user, false);
     }

    @Test
    void dismissEvent_fromConflictFlow_passesTrue() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.dismissEvent(TRIP_ID, EVENT_ID, user, true))
            .thenReturn(mockEventGetDTO());

        performDismissEvent(true)
            .andExpect(status().isOk());
        Mockito.verify(eventService).dismissEvent(TRIP_ID, EVENT_ID, user, true);
     }

    @Test
    void dismissEvent_notFound_Failure404() throws Exception {
        User user = mockValidToken();
        Mockito.when(eventService.dismissEvent(TRIP_ID, EVENT_ID, user, false))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        performDismissEvent(false).andExpect(status().isNotFound());
    }

}
