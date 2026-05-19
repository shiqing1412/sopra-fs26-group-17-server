package ch.uzh.ifi.hase.soprafs26.controller;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.service.UserService;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAuthDTO;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void givenUsers_whenGetUsers_thenReturnJsonArray() throws Exception {
        User user = new User();
        user.setUsername("testUsername");
        user.setStatus(UserStatus.OFFLINE);

        List<User> allUsers = Collections.singletonList(user);
        given(userService.getUsers()).willReturn(allUsers);

        MockHttpServletRequestBuilder getRequest = get("/users").contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(getRequest)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].status", is(user.getStatus().toString())));
    }

    @Test
    void createUser_validInput_userCreated() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testUsername");
        user.setToken("1");
        user.setStatus(UserStatus.OFFLINE);

        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("password123");
        userPostDTO.setPasswordConfirm("password123");

        given(userService.createUser(Mockito.any())).willReturn(user);

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", is(user.getUserId().intValue())))
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.status", is(user.getStatus().toString())));
    }

    @Test
    void createUser_passwordMismatch_throwsBadRequest() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("password123");
        userPostDTO.setPasswordConfirm("differentPassword");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_nullPasswordConfirm_throwsBadRequest() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("testUsername");
        userPostDTO.setPassword("password123");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_invalidUsername_tooShort_throwsBadRequestsWithMessage() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("ab");
        userPostDTO.setPassword("password123");
        userPostDTO.setPasswordConfirm("password123");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username must be between 3 and 20 characters.")));
    }

    @Test
    void createUser_invalidUsername_tooLong_throwsBadRequestsWithMessage() throws Exception {
        UserPostDTO userPostDTO = new UserPostDTO();
        userPostDTO.setUsername("a".repeat(21));
        userPostDTO.setPassword("password123");
        userPostDTO.setPasswordConfirm("password123");

        MockHttpServletRequestBuilder postRequest = post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(userPostDTO));

        mockMvc.perform(postRequest)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username must be between 3 and 20 characters.")));
    
        Mockito.verify(userService, Mockito.never()).createUser(Mockito.any());
    }

    private String asJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JacksonException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("The request body could not be created.%s", e.toString()));
        }
    }


    @Test
    void login_validCredentials_returns200WithToken() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setUsername("testUsername");
        user.setToken("session-token");
        user.setStatus(UserStatus.ONLINE);

        given(userService.login("testUsername", "password123")).willReturn(user);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUsername\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testUsername")))
                .andExpect(jsonPath("$.token", is("session-token")));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        given(userService.login("testUsername", "wrongPassword"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The password is incorrect!"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testUsername\",\"password\":\"wrongPassword\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_unknownUsername_returns401() throws Exception {
        given(userService.login("unknown", "password123"))
                .willThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "The username is not correct!"));

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"unknown\",\"password\":\"password123\"}"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    void logout_validToken_returns200() throws Exception {
        doNothing().when(userService).logout("valid-token");

        mockMvc.perform(post("/logout")
                        .header("Authorization", "valid-token"))
                .andExpect(status().isOk());
    }

    @Test
    void logout_invalidToken_returns401() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!"))
                .when(userService).logout("bad-token");

        mockMvc.perform(post("/logout")
                        .header("Authorization", "bad-token"))
                .andExpect(status().isUnauthorized());
    }

}
