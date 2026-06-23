package com.example.eventservice.controller;

import com.example.eventservice.model.Event;
import com.example.eventservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<Event>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startDate") String sortBy) {
        return ResponseEntity.ok(eventService.findAll(
                PageRequest.of(page, size, Sort.by(sortBy))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> findById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Event> create(@Valid @RequestBody Event event) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.save(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> update(@PathVariable Long id, @Valid @RequestBody Event event) {
        return ResponseEntity.ok(eventService.update(id, event));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}