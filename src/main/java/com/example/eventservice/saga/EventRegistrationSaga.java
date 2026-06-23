package com.example.eventservice.saga;

import com.example.eventservice.client.NotificationServiceClient;
import com.example.eventservice.client.UserServiceClient;
import com.example.eventservice.model.Event;
import com.example.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventRegistrationSaga {

    private final EventRepository eventRepository;
    private final UserServiceClient userServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public Event execute(Long eventId, Long userId) {
        log.info("SAGA START: Registering user {} to event {}", userId, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // Step 1: Verify user exists
        try {
            UserServiceClient.UserResponse user = userServiceClient.findById(userId);
            log.info("SAGA STEP 1 SUCCESS: User {} verified", user.username());
        } catch (Exception e) {
            log.error("SAGA STEP 1 FAILED: User verification failed - compensating");
            throw new RuntimeException("Saga failed at step 1: " + e.getMessage());
        }

        // Step 2: Reserve spot
        if (event.getAvailableSpots() <= 0) {
            log.error("SAGA STEP 2 FAILED: No spots available - compensating");
            throw new RuntimeException("Saga failed at step 2: No spots available");
        }
        event.setAvailableSpots(event.getAvailableSpots() - 1);
        eventRepository.save(event);
        log.info("SAGA STEP 2 SUCCESS: Spot reserved, {} spots remaining", event.getAvailableSpots());

        // Step 3: Send notification
        try {
            notificationServiceClient.sendNotification(
                    userId,
                    "Înregistrare confirmată la: " + event.getName(),
                    "CONFIRMATION"
            );
            log.info("SAGA STEP 3 SUCCESS: Notification sent");
        } catch (Exception e) {
            log.warn("SAGA STEP 3 FAILED: Notification failed but not compensating - {}", e.getMessage());
        }

        log.info("SAGA COMPLETE: User {} registered to event {}", userId, eventId);
        return event;
    }
}