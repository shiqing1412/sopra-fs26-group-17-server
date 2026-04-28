package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;
import java.io.Serializable;

import ch.uzh.ifi.hase.soprafs26.constant.ParticipationStatus;

@Entity
@Table(name = "event_members",
       uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
       
public class EventMember implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue
  private Long eventMemberId;

  @ManyToOne
  @JoinColumn(name = "event_id", nullable = false)
  private Event event;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ParticipationStatus participationStatus;

  public Long getEventMemberId() { return eventMemberId; }
  public void setEventMemberId(Long eventMemberId) { this.eventMemberId = eventMemberId; }

  public Event getEvent() { return event; }
  public void setEvent(Event event) { this.event = event; }

  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }

  public ParticipationStatus getParticipationStatus() { return participationStatus; }
  public void setParticipationStatus(ParticipationStatus participationStatus) { this.participationStatus = participationStatus; }
}