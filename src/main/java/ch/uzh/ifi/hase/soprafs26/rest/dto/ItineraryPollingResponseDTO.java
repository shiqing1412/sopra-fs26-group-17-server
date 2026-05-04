package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.util.List;

public class ItineraryPollingResponseDTO {
   
    private List<DayDTO> days;
    private List<TripMemberDTO> members;

    public ItineraryPollingResponseDTO(List<DayDTO> days, List<TripMemberDTO> members) {
        this.days = days;
        this.members = members;
    }


    public List<DayDTO> getDays() {
        return days;
    }
    public void setDays(List<DayDTO> days) {
        this.days = days;
    }

    public List<TripMemberDTO> getMembers() {
        return members;
    }
    public void setMembers(List<TripMemberDTO> members) {
        this.members = members;
    }

}
