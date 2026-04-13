package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventGetDTO {
  private Long eventId;
  private String eventTitle;
  private LocalDate date;
  private LocalTime time;
  private String notes;
  private String placeName;
  private Double lat;
  private Double lng;
  private String createdBy;

  public Long getEventId() { return eventId; }
  public void setEventId(Long eventId) { this.eventId = eventId; }

  public String getEventTitle() { return eventTitle; }
  public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }

  public LocalTime getTime() { return time; }
  public void setTime(LocalTime time) { this.time = time; }

  public String getNotes() { return notes; }
  public void setNotes(String notes) { this.notes = notes; }

  public String getPlaceName() { return placeName; }
  public void setPlaceName(String placeName) { this.placeName = placeName; }

  public Double getLat() { return lat; }
  public void setLat(Double lat) { this.lat = lat; }

  public Double getLng() { return lng; }
  public void setLng(Double lng) { this.lng = lng; }

  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}