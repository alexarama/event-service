package com.example.eventservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/participant/{participantId}")
    void sendNotification(
            @PathVariable Long participantId,
            @RequestParam String message,
            @RequestParam String type);
}