package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class EventMemberDTO {
    private Long userId;
    private String username;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}