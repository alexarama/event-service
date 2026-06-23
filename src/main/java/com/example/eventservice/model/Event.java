package com.example.eventservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Numele evenimentului este obligatoriu")
    @Column(nullable = false)
    private String name;

    private String description;

    @NotNull(message = "Data de start este obligatorie")
    @Column(nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "Data de final este obligatorie")
    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Integer availableSpots;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.OPEN;

    public enum EventStatus {
        OPEN, CLOSED, CANCELLED
    }
}