package ch.uzh.ifi.hase.soprafs26.repository;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;
import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import ch.uzh.ifi.hase.soprafs26.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TripRepositoryIntegrationTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private TripRepository tripRepository;

  private User persistUser(String username) {
    User user = new User();
    user.setUsername(username);
    user.setPassword("hashedPassword");
    user.setStatus(UserStatus.ONLINE);
    user.setToken(UUID.randomUUID().toString());
    entityManager.persist(user);
    return user;
  }

  private Trip persistTrip(User owner, String shareCode) {
    Trip trip = new Trip();
    trip.setTripTitle("Test Trip");
    trip.setStartDate(LocalDate.of(2027, 7, 1));
    trip.setEndDate(LocalDate.of(2027, 7, 10));
    trip.setShareCode(shareCode);
    trip.setOwner(owner);
    entityManager.persist(trip);
    return trip;
  }

  @Test
  void findByShareCode_existingCode_returnsTrip() {
    User owner = persistUser("owner1");
    persistTrip(owner, "CODE1234");
    entityManager.flush();

    Optional<Trip> found = tripRepository.findByShareCode("CODE1234");

    assertTrue(found.isPresent());
    assertEquals("CODE1234", found.get().getShareCode());
    assertEquals("Test Trip", found.get().getTripTitle());
  }

  @Test
  void findByShareCode_unknownCode_returnsEmpty() {
    Optional<Trip> found = tripRepository.findByShareCode("NOTEXIST");
    assertFalse(found.isPresent());
  }

  @Test
  void existsByShareCode_existingCode_returnsTrue() {
    User owner = persistUser("owner2");
    persistTrip(owner, "EXIST123");
    entityManager.flush();

    assertTrue(tripRepository.existsByShareCode("EXIST123"));
  }

  @Test
  void existsByShareCode_unknownCode_returnsFalse() {
    assertFalse(tripRepository.existsByShareCode("MISSING1"));
  }
}