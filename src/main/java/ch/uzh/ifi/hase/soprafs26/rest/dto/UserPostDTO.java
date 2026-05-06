package ch.uzh.ifi.hase.soprafs26.rest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserPostDTO {

    @NotBlank (message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters.")
    private String username;

    @NotBlank (message = "Password is required")
    @Size(min = 6, max = 255, message = "Password must be between 6 and 255 characters.")
	private String password;
	
	@NotBlank (message = "Password confirmation is required")
    @Size(max = 255, message = "Password confirmation must be at most 255 characters.")
	private String passwordConfirm;

    public String getUsername() { 
			return username; 
		}

    public void setUsername(String username) {
			this.username = username; 
		}

    public String getPassword() { 
			return password; 
		}

    public void setPassword(String password) { 
			this.password = password; 
		}

		public String getPasswordConfirm() { 
			return passwordConfirm; 
		}

		public void setPasswordConfirm(String passwordConfirm) { 
			this.passwordConfirm = passwordConfirm; 
		}
}
