package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.util.List;

public class DayDTO {
  private LocalDate date;
  private List<EventGetDTO> events;

  public DayDTO(LocalDate date, List<EventGetDTO> events) {
      this.date = date;
      this.events = events;
  }

  public LocalDate getDate() { return date; }
  public void setDate(LocalDate date) { this.date = date; }

  public List<EventGetDTO> getEvents() { return events; }
  public void setEvents(List<EventGetDTO> events) { this.events = events; }
}