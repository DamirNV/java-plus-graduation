package ru.practicum.ewm.stats.analyzer.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "event_similarities")
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_a", nullable = false)
    private Long eventA;

    @Column(name = "event_b", nullable = false)
    private Long eventB;

    @Column(name = "score", nullable = false)
    private double score;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public EventSimilarity() {
    }

    public Long getId() {
        return id;
    }

    public Long getEventA() {
        return eventA;
    }

    public void setEventA(Long eventA) {
        this.eventA = eventA;
    }

    public Long getEventB() {
        return eventB;
    }

    public void setEventB(Long eventB) {
        this.eventB = eventB;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
