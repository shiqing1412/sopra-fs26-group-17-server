package ch.uzh.ifi.hase.soprafs26.rest.dto;

public class TripMemberDTO {
    private Long userId;
    private String username;
    private String role;
    private String status;
    private Boolean active;
    private Boolean currentUser;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(Boolean currentUser) {
        this.currentUser = currentUser;
    }

}
