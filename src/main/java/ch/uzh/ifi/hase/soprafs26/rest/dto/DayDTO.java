package ch.uzh.ifi.hase.soprafs26.rest.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DayDTO)) return false;
    DayDTO that = (DayDTO) o;
    return Objects.equals(date, that.date)
      && Objects.equals(events, that.events);
  }

  @Override
  public int hashCode() {
    return Objects.hash(date, events);
  }
}