package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;


public class TripPostDTO {
    
    private String tripTitle;
    private LocalDate startDate;
    private LocalDate endDate;

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

}
