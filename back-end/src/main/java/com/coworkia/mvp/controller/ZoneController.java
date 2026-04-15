package com.coworkia.mvp.controller;

import com.coworkia.mvp.repository.ZoneRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "http://localhost:4200")
public class ZoneController {

    private final ZoneRepository zoneRepository;
    private final com.coworkia.mvp.repository.BookingRepository bookingRepository;

    public ZoneController(ZoneRepository zoneRepository, com.coworkia.mvp.repository.BookingRepository bookingRepository) {
        this.zoneRepository = zoneRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/availability")
    public List<Map<String, Object>> getAvailability() {
        List<Map<String, Object>> result = new ArrayList<>();
        zoneRepository.findAll().forEach(z -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", z.getName());
            map.put("code", z.getCode());
            map.put("capacity", z.getCapacity());
            map.put("occupied", bookingRepository.countActiveRightNowByZone(z.getId()));
            result.add(map);
        });
        return result;
    }

    @GetMapping("/history")
    public ResponseEntity<?> getOccupationHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication auth) {

        boolean isManagerOrAdmin = auth != null && auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> "ROLE_MANAGER".equals(role) || "ROLE_ADMIN".equals(role));
        if (!isManagerOrAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Réservé à la vue manager.");
        }

        if (to.isBefore(from)) {
            return ResponseEntity.badRequest().body("La date de fin doit être supérieure ou égale à la date de début.");
        }

        LocalDateTime fromTs = from.atStartOfDay();
        LocalDateTime toTs = to.plusDays(1).atStartOfDay(); // inclusif jusqu'à la fin de la journée "to"
        double periodHours = Math.max(1.0, Duration.between(fromTs, toTs).toHours());

        List<Object[]> rows = bookingRepository.getZoneOccupationHistory(fromTs, toTs);
        List<Map<String, Object>> zonesHistory = new ArrayList<>();

        double globalOccupiedHours = 0.0;
        double globalCapacityHours = 0.0;

        for (Object[] row : rows) {
            String zoneName = String.valueOf(row[1]);
            String zoneCode = String.valueOf(row[2]);
            int capacity = ((Number) row[3]).intValue();
            double occupiedHours = ((Number) row[4]).doubleValue();
            double occupationRate = capacity > 0 ? (occupiedHours / (capacity * periodHours)) * 100.0 : 0.0;

            Map<String, Object> map = new HashMap<>();
            map.put("name", zoneName);
            map.put("code", zoneCode);
            map.put("capacity", capacity);
            map.put("occupiedDeskHours", round2(occupiedHours));
            map.put("occupationRate", round2(Math.min(100.0, occupationRate)));
            zonesHistory.add(map);

            globalOccupiedHours += occupiedHours;
            globalCapacityHours += capacity * periodHours;
        }

        double globalRate = globalCapacityHours > 0 ? (globalOccupiedHours / globalCapacityHours) * 100.0 : 0.0;
        Map<String, Object> result = new HashMap<>();
        result.put("fromDate", from.toString());
        result.put("toDate", to.toString());
        result.put("periodHours", periodHours);
        result.put("globalOccupiedDeskHours", round2(globalOccupiedHours));
        result.put("globalOccupationRate", round2(Math.min(100.0, globalRate)));
        result.put("zones", zonesHistory);

        return ResponseEntity.ok(result);
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
