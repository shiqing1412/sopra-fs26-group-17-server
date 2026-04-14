package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.util.List;

public class TripDetailDTO {
    private Long tripId;
    private String tripTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private String owner;
    private String shareCode;
    private List<TripMemberDTO> members;


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

    public String getOwner() {
        return owner;
    }
    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getShareCode() {
        return shareCode;
    }
    public void setShareCode(String shareCode) {
        this.shareCode = shareCode;
    }

    public List<TripMemberDTO> getMembers() {
        return members;
    }
    public void setMembers(List<TripMemberDTO> members) {
        this.members = members;
    }
}
