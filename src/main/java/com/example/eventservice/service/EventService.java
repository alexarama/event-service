package com.example.eventservice.service;

import com.example.eventservice.client.NotificationServiceClient;
import com.example.eventservice.client.UserServiceClient;
import com.example.eventservice.exception.ResourceNotFoundException;
import com.example.eventservice.model.Event;
import com.example.eventservice.repository.EventRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RefreshScope
@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public Page<Event> findAll(Pageable pageable) {
        log.debug("Fetching all events");
        return eventRepository.findAll(pageable);
    }

    public Event findById(Long id) {
        log.debug("Fetching event with id: {}", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evenimentul cu id " + id + " nu a fost găsit"));
    }

    public Event save(Event event) {
        log.info("Saving event: {}", event.getName());
        return eventRepository.save(event);
    }

    public Event update(Long id, Event event) {
        Event existing = findById(id);
        existing.setName(event.getName());
        existing.setDescription(event.getDescription());
        existing.setStartDate(event.getStartDate());
        existing.setEndDate(event.getEndDate());
        existing.setAvailableSpots(event.getAvailableSpots());
        existing.setStatus(event.getStatus());
        log.info("Updating event with id: {}", id);
        return eventRepository.save(existing);
    }

    public void delete(Long id) {
        log.info("Deleting event with id: {}", id);
        findById(id);
        eventRepository.deleteById(id);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "registerUserFallback")
    @Retry(name = "userService")
    public Event registerUser(Long eventId, Long userId) {
        Event event = findById(eventId);

        UserServiceClient.UserResponse user = userServiceClient.findById(userId);
        log.info("User {} found, registering to event {}", user.username(), event.getName());

        if (event.getAvailableSpots() <= 0) {
            throw new RuntimeException("Nu mai sunt locuri disponibile");
        }

        event.setAvailableSpots(event.getAvailableSpots() - 1);
        Event saved = eventRepository.save(event);

        try {
            notificationServiceClient.sendNotification(
                    userId,
                    "Te-ai înregistrat cu succes la evenimentul: " + event.getName(),
                    "CONFIRMATION"
            );
            log.info("Notification sent to user {}", userId);
        } catch (Exception e) {
            log.warn("Could not send notification to user {}: {}", userId, e.getMessage());
        }

        return saved;
    }

    public Event registerUserFallback(Long eventId, Long userId, Exception ex) {
        log.error("Circuit breaker activated for registerUser: {}", ex.getMessage());
        throw new RuntimeException("Serviciul de utilizatori nu este disponibil momentan. Încearcă mai târziu.");
    }
}