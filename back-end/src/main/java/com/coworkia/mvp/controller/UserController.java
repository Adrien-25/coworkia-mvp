package com.coworkia.mvp.controller;

import com.coworkia.mvp.entity.User;
import com.coworkia.mvp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Réponse JSON dédiée (sans sérialiser toute l'interface UserDetails).
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findByEmail(auth.getName())
                .map(UserController::toProfileMap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private static Map<String, Object> toProfileMap(User u) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", u.getId());
        m.put("email", u.getEmail());
        m.put("firstName", u.getFirstName() != null ? u.getFirstName() : "");
        m.put("lastName", u.getLastName() != null ? u.getLastName() : "");
        m.put("role", u.getRole().name());
        m.put("fidelityPoints", u.getFidelityPoints() != null ? u.getFidelityPoints() : 0);
        return m;
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@RequestBody Map<String, String> body, Authentication auth) {
        if (auth == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utilisateur non authentifié."));
        }

        User user = userRepository.findByEmail(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Utilisateur introuvable."));
        }

        String firstName = body.get("firstName");
        String lastName = body.get("lastName");

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName.trim());
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName.trim());
        }

        User saved = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "id", saved.getId(),
                "email", saved.getEmail(),
                "firstName", saved.getFirstName() == null ? "" : saved.getFirstName(),
                "lastName", saved.getLastName() == null ? "" : saved.getLastName(),
                "role", saved.getRole().name(),
                "fidelityPoints", saved.getFidelityPoints()
        ));
    }
}
