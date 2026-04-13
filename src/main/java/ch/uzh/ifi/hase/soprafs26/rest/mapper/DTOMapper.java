package ch.uzh.ifi.hase.soprafs26.rest.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import ch.uzh.ifi.hase.soprafs26.entity.User;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.TripPostDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs26.rest.dto.UserPostDTO;

/**
 * DTOMapper
 * This class is responsible for generating classes that will automatically
 * transform/map the internal representation
 * of an entity (e.g., the User) to the external/API representation (e.g.,
 * UserGetDTO for getting, UserPostDTO for creating)
 * and vice versa.
 * Additional mappers can be defined for new entities.
 * Always created one mapper for getting information (GET) and one mapper for
 * creating information (POST).
 */
@Mapper
public interface DTOMapper {

	DTOMapper INSTANCE = Mappers.getMapper(DTOMapper.class);

	@Mapping(source = "username", target = "username")
	@Mapping(source = "password", target = "password")
	User convertUserPostDTOtoEntity(UserPostDTO userPostDTO);

	@Mapping(source = "userId", target = "userId")
	@Mapping(source = "username", target = "username")
	@Mapping(source = "token", target = "token")
	@Mapping(source = "status", target = "status")
	UserGetDTO convertEntityToUserGetDTO(User user);

	@Mapping(source = "tripTitle", target = "tripTitle")
	@Mapping(source = "startDate", target = "startDate")	
	@Mapping(source = "endDate", target = "endDate")
	Trip convertTripPostDTOtoEntity(TripPostDTO tripPostDTO);
	
	@Mapping(source = "tripId", target = "tripId")
	@Mapping(source = "tripTitle", target = "tripTitle")
	@Mapping(source = "startDate", target = "startDate")
	@Mapping(source = "endDate", target = "endDate")
	@Mapping(source = "owner", target = "owner", qualifiedByName = "mapUserToUsername")
	@Mapping(source = "shareCode", target = "shareCode")
	TripGetDTO convertEntityToTripGetDTO(Trip trip);

	@Named("mapUserToUsername")
	default String mapUserToUsername(User user) {
		return user != null ? user.getUsername() : null;
	}


} 
