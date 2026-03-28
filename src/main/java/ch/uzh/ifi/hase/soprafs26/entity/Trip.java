package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "trips")
public class Trip implements Serializable {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private Long tripId;
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;
   
    @Column(nullable = false)
    private LocalDate endDate;

    
    public Long getTripId() {
        return tripId;
    }
    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }


    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
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

    
}
