package ch.uzh.ifi.hase.soprafs26.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserAuthDTO;
import ch.uzh.ifi.hase.soprafs26.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs26.service.UserService;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

	private final UserService userService;

	UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/users")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<UserGetDTO> getAllUsers() {
		// fetch all users in the internal representation
		List<User> users = userService.getUsers();
		List<UserGetDTO> userGetDTOs = new ArrayList<>();

		// convert each user to the API representation
		for (User user : users) {
			userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
		}
		return userGetDTOs;
	}

	@PostMapping("/users")
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public UserAuthDTO createUser(@RequestBody UserPostDTO userPostDTO) {
		// 1. Null/blank checks first (service will also do these, but we need
    //    them here before we attempt the .equals() comparison)
		if (userPostDTO.getPassword() == null || userPostDTO.getPassword().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required.");
    }
		if (userPostDTO.getPasswordConfirm() == null || userPostDTO.getPasswordConfirm().isBlank()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please confirm your password.");
    }

		// 2. Match check — safe to call .equals() now, both are non-null
		if (!userPostDTO.getPassword().equals(userPostDTO.getPasswordConfirm())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match.");
    }

		// 3. to service (length check + duplicate check happen there)
		// convert API user to internal representation
		User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

		// create user
		User createdUser = userService.createUser(userInput);
		// convert internal representation of user back to API
		return DTOMapper.INSTANCE.convertEntityToUserAuthDTO(createdUser);
	}

	@PostMapping("/login")
	@ResponseStatus(HttpStatus.OK) //200 OK
	@ResponseBody
	public UserAuthDTO login(@RequestBody UserPostDTO loginDTO) {

		String username = loginDTO.getUsername();
		String password = loginDTO.getPassword();

		User user = userService.login(username, password);
		return DTOMapper.INSTANCE.convertEntityToUserAuthDTO(user);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.OK) //200 OK
	public void logout(@RequestHeader ("Authorization") String token){
		userService.logout(token);
	}

}
