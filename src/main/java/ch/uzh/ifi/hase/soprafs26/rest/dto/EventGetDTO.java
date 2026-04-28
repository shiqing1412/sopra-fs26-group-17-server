package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class EventGetDTO {
  private Long eventId;
  private String eventTitle;
  private LocalDate date;
  private LocalTime time;
  private String notes;
  private String placeId;
  private String placeName;
  private Double lat;
  private Double lng;
  private String createdBy;
  private LocalTime endTime;

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

  public String getPlaceId() { return placeId; }
  public void setPlaceId(String placeId) { this.placeId = placeId; }

  public String getPlaceName() { return placeName; }
  public void setPlaceName(String placeName) { this.placeName = placeName; }

  public Double getLat() { return lat; }
  public void setLat(Double lat) { this.lat = lat; }

  public Double getLng() { return lng; }
  public void setLng(Double lng) { this.lng = lng; }

  public String getCreatedBy() { return createdBy; }
  public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

  public LocalTime getEndTime() { return endTime; }
  public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EventGetDTO)) return false;
    EventGetDTO that = (EventGetDTO) o;
    return Objects.equals(eventId, that.eventId)
      && Objects.equals(eventTitle, that.eventTitle)
      && Objects.equals(date, that.date)
      && Objects.equals(time, that.time)
      && Objects.equals(notes, that.notes)
      && Objects.equals(placeId, that.placeId)
      && Objects.equals(placeName, that.placeName)
      && Objects.equals(lat, that.lat)
      && Objects.equals(lng, that.lng)
      && Objects.equals(createdBy, that.createdBy)
      && Objects.equals(endTime, that.endTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(eventId, eventTitle, date, time, endTime, notes,
      placeId, placeName, lat, lng, createdBy);
  }
}