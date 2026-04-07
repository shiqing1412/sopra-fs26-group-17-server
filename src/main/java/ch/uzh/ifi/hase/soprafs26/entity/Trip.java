package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
public class Trip implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long tripId;
    
    @Column(nullable = false)
    private String tripTitle;

    @Column(nullable = false)
    private LocalDate startDate;
   
    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, unique = true)
    private String shareCode;

    @ManyToOne //Many trips can be owned by one user, but each trip has only one owner
    @JoinColumn(name = "owner_id", nullable = false)  //DB level, we should handle the owner_id column in the trips table, which is a foreign key referencing the users table
    private User owner;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>();

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Membership> memberships = new ArrayList<>();

    
    public Long getTripId() {
        return tripId;
    }
    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public String getTripTitle() {
        return tripTitle;
    }
    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }


    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getShareCode() { 
        return shareCode; 
    }
    public void setShareCode(String shareCode) { 
        this.shareCode = shareCode; 
    }

    public List<Event> getEvents() { 
        return events; 
    }
    public void setEvents(List<Event> events) { 
        this.events = events; 
    }

    public List<Membership> getMemberships() { 
        return memberships; 
    }
    public void setMemberships(List<Membership> memberships) { 
        this.memberships = memberships; 
    }

}
