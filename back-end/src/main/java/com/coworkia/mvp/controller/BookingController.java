package com.coworkia.mvp.controller;

import com.coworkia.mvp.entity.Booking;
import com.coworkia.mvp.entity.User;
import com.coworkia.mvp.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "http://localhost:4200")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> book(@RequestBody Booking booking, Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (!user.getId().equals(booking.getUser().getId()) && user.getRole() != User.Role.ROLE_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Vous ne pouvez réserver que pour vous-même.");
        }
        return ResponseEntity.ok(bookingService.createBooking(booking));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getMyBookings(@PathVariable Long userId, Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (!user.getId().equals(userId) && user.getRole() == User.Role.ROLE_USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé.");
        }
        return ResponseEntity.ok(bookingService.getUserBookings(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings(Authentication auth) {
        User user = (User) auth.getPrincipal();
        if (user.getRole() == User.Role.ROLE_USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Réservé aux managers.");
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Booking b = bookingService.getBookingById(id);
        if (b == null) return ResponseEntity.notFound().build();
        
        if (!b.getUser().getId().equals(user.getId()) && user.getRole() == User.Role.ROLE_USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée.");
        }
        
        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking booking, Authentication auth) {
        User user = (User) auth.getPrincipal();
        Booking existing = bookingService.getBookingById(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (!existing.getUser().getId().equals(user.getId()) && user.getRole() == User.Role.ROLE_USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée.");
        }

        try {
            return ResponseEntity.ok(bookingService.updateBooking(id, booking));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
