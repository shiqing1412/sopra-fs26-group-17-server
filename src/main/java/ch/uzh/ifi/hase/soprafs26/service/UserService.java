package ch.uzh.ifi.hase.soprafs26.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.repository.UserRepository;

import java.util.List;
import java.util.UUID;

/**
 * User Service
 * This class is the "worker" and responsible for all functionality related to
 * the user
 * (e.g., it creates, modifies, deletes, finds). The result will be passed back
 * to the caller.
 */
@Service
@Transactional
public class UserService {

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;

	public UserService(@Qualifier("userRepository") UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> getUsers() {
		return this.userRepository.findAll();
	}

	public User createUser(User newUser) {
	validateUserInput(newUser);
    newUser.setToken(UUID.randomUUID().toString());
    newUser.setStatus(UserStatus.OFFLINE);
    checkIfUserExists(newUser);
    newUser = userRepository.save(newUser);
    userRepository.flush();
    log.debug("Created Information for User: {}", newUser);
    return newUser;
	}

	public User login(String username, String password) {
		if (username == null || username.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username cannot be empty!"); //400 Bad Request
		}
		if (password == null || password.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The password cannot be empty!"); //400 Bad Request
		}
		User user = userRepository.findByUsername(username);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password!"); //401 Unauthorized
		}
		if (!user.getPassword().equals(password)) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password!"); //401 Unauthorized
		}
		user.setStatus(UserStatus.ONLINE);
		user.setToken(UUID.randomUUID().toString());
		userRepository.save(user);
		userRepository.flush();
		return user;
	}

	/**
	 * This is a helper method that will check the uniqueness criteria of the
	 * username and the name
	 * defined in the User entity. The method will do nothing if the input is unique
	 * and throw an error otherwise.
	 *
	 * @param userToBeCreated
	 * @throws org.springframework.web.server.ResponseStatusException
	 * @see User
	 */
	private void checkIfUserExists(User userToBeCreated) {
		User userByUsername = userRepository.findByUsername(userToBeCreated.getUsername());
		if (userByUsername != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Username already taken. Please choose a different one.");
		}
	}	

	private void validateUserInput(User user) {
		if (user.getUsername() == null || user.getUsername().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required.");
		}
		if (user.getPassword() == null || user.getPassword().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
		}
		if (user.getPassword().length() < 6) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 6 characters.");
		}
	}

	public User validateToken(String token) {
		if (token == null || token.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing token!"); //401 Unauthorized
		}
		User user = userRepository.findByToken(token);
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token!"); //401 Unauthorized
		}
		if (user.getStatus() != UserStatus.ONLINE) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is offline!"); //401 Unauthorized
		}
		return user;
	}

}
