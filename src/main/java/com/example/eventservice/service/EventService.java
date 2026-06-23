package com.example.eventservice.service;

import com.example.eventservice.exception.ResourceNotFoundException;
import com.example.eventservice.model.Event;
import com.example.eventservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

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
}