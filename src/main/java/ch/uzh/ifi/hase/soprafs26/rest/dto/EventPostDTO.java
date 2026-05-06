package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EventPostDTO {

  @NotBlank(message = "Event title is required")
  @Size(max = 255, message = "Event title must be at most 255 characters.")
  private String eventTitle;

  @NotNull(message = "Event date is required")
  private LocalDate date;

  @NotNull(message = "Event time is required")
  private LocalTime time;
  @NotNull(message = "Event end time is required")
  private LocalTime endTime;
  
  @Size(max = 2000, message = "Notes must be at most 2000 characters.")
  private String notes;

  @NotBlank(message = "Place ID is required")
  @Size(max = 255, message = "Place ID must be at most 255 characters.")
  private String placeId;

  @NotBlank(message = "Place name is required")
  @Size(max = 255, message = "Place name must be at most 255 characters.")
  private String placeName;

  @NotNull(message = "Latitude is required")
  private Double lat;
  @NotNull(message = "Longitude is required")
  private Double lng;



  public String getEventTitle() { return eventTitle; }
  public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }

  public LocalTime getTime() { return time; }
  public void setTime(LocalTime time) { this.time = time; }

  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public String getPlaceId() { return placeId; }
  public void setPlaceId(String placeId) { this.placeId = placeId; }

  public String getPlaceName() { return placeName; }
  public void setPlaceName(String placeName) { this.placeName = placeName; }

  public Double getLat() { return lat; }
  public void setLat(Double lat) { this.lat = lat; }

  public Double getLng() { return lng; }
  public void setLng(Double lng) { this.lng = lng; }

}