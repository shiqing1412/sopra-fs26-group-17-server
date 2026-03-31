package ch.uzh.ifi.hase.soprafs26.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ch.uzh.ifi.hase.soprafs26.entity.Trip;
import java.util.Optional;


@Repository("tripRepository")
public interface TripRepository extends JpaRepository<Trip, Long> { 

    boolean existsByShareCode(String shareCode);
    
    Optional<Trip> findByShareCode(String shareCode); //for join
    
}   