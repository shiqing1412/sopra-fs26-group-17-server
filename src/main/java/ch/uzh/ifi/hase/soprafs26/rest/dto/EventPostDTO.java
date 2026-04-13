package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class EventPostDTO {
  private String eventTitle;
  private LocalDate dayDate;
  private LocalTime time;
  private String notes;

  private String placeId;
  private String placeName;
  private Double lat;
  private Double lng;

  public String getEventTitle() { return eventTitle; }
  public void setEventTitle(String eventTitle) { this.eventTitle = eventTitle; }

  public LocalDate getDayDate() { return dayDate; }
  public void setDayDate(LocalDate dayDate) { this.dayDate = dayDate; }

  public LocalTime getTime() { return time; }
  public void setTime(LocalTime time) { this.time = time; }

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