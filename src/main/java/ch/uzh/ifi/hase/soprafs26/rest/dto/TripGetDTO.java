package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;

public class TripGetDTO {

    private Long tripId;
    private String title;
    private LocalDate startDate;
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
