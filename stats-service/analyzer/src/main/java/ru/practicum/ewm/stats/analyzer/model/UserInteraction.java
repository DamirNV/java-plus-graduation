package ru.practicum.ewm.stats.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "user_interactions")
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "rating", nullable = false)
    private double rating;

    @Column(name = "last_interaction_at", nullable = false)
    private Instant lastInteractionAt;

    public UserInteraction() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Instant getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Instant lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }
}
