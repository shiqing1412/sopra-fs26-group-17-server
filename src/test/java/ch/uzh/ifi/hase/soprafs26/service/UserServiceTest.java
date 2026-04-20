package ch.uzh.ifi.hase.soprafs26.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

/* Methods we have：
 * creatUser: 201,400,409
 * login: 200,401
 * logout: 200,401
 * getUsers: 200
 * checkIfUserExists
 * validateUserInput
 * validateToken 
 */

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

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

//Register tests

    @Test //Register 201
    public void createUser_validInputs_success() {
        User createdUser = userService.createUser(testUser);

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals(testUser.getUserId(), createdUser.getUserId());
        assertEquals(testUser.getUsername(), createdUser.getUsername());
        assertNotNull(createdUser.getToken());
        assertEquals(UserStatus.ONLINE, createdUser.getStatus());
    }

    @Test //Register 409
    public void createUser_duplicateUsername_throwsException() { 
        userService.createUser(testUser);

        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test //Register 400
    public void createUser_blankPassword_throwsException() {
        testUser.setPassword("");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test //Register 400
    public void createUser_shortPassword_throwsException() {
        testUser.setPassword("abc");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test //Register 400
    public void createUser_blankUsername_throwsException() {
        testUser.setUsername("");

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

    @Test //Register 400
    public void createUser_nullUsername_throwsException() {
        testUser.setUsername(null);

        assertThrows(ResponseStatusException.class, () -> userService.createUser(testUser));
    }

//Login tests

    @Test //Login 200
    public void login_validCredentials_success() {
        String rawPassword = "password123";
        String hashedPassword = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray());
        testUser.setPassword(hashedPassword);
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        User loggedInUser = userService.login(testUser.getUsername(), rawPassword);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals(testUser.getUserId(), loggedInUser.getUserId());
        assertEquals(testUser.getUsername(), loggedInUser.getUsername());
        assertNotNull(loggedInUser.getToken());
        assertEquals(UserStatus.ONLINE, loggedInUser.getStatus());
    }

    @Test //Login 401
    public void login_incorrectPassword_throwsException() {
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(testUser);

        assertThrows(ResponseStatusException.class, () -> userService.login(testUser.getUsername(), "wrongPassword"));
    }

    @Test //Login 401
    public void login_nonExistentUsername_throwsException() {
        Mockito.when(userRepository.findByUsername(Mockito.any())).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> userService.login("nonExistentUser", "password123"));
    }

    @Test //Not in spec: Login 400
    public void login_blankUsername_throwsException() {
        assertThrows(ResponseStatusException.class, () -> userService.login("", "password123"));
    }

    @Test //Not in spec: Login 400
    public void login_blankPassword_throwsException() {
        assertThrows(ResponseStatusException.class, () -> userService.login("testUsername", ""));
    }

//GetUsers tests (Not in spec)

    @Test //GetUsers 200
    public void getUsers_success() {
        userService.getUsers();
        Mockito.verify(userRepository, Mockito.times(1)).findAll();
    }
    
// Logout tests
    @Test //Logout 200
    public void logout_validToken_success() {
        String token = "validToken";
        testUser.setToken(token);
        testUser.setStatus(UserStatus.ONLINE);
        Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(testUser);
        userService.logout(token);
        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any());
        assertEquals(UserStatus.OFFLINE, testUser.getStatus());
    }

    @Test //Logout 401
    public void logout_invalidToken_throwsException() {
        Mockito.when(userRepository.findByToken(Mockito.any())).thenReturn(null);
        assertThrows(ResponseStatusException.class, () -> userService.logout("invalidToken"));
    }
}

//Other helper method doesn't have specific test cases as they are covered by the main methods and are private.