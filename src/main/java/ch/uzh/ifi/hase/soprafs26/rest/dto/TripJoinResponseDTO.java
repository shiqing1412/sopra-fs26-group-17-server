package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class TripJoinResponseDTO {

    private Long tripId;
    private String tripTitle;
    private boolean alreadyMember;


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

    public boolean isAlreadyMember() {
        return alreadyMember;
    }
    public void setAlreadyMember(boolean alreadyMember) {
        this.alreadyMember = alreadyMember;
    }

}
