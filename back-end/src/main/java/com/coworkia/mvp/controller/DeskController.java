package com.coworkia.mvp.controller;

import com.coworkia.mvp.repository.DeskRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/desks")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class DeskController {

    private final DeskRepository deskRepository;

    public DeskController(DeskRepository deskRepository) {
        this.deskRepository = deskRepository;
    }

    @GetMapping
    public List<Map<String, Object>> getAllDesks() {
        List<Map<String, Object>> result = new ArrayList<>();
        deskRepository.findAll().forEach(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            map.put("code", d.getCode());
            if (d.getZone() != null) {
                map.put("zone", d.getZone().getName() + " (" + d.getZone().getType() + ")");
            } else {
                map.put("zone", "Zone Inconnue");
            }
            result.add(map);
        });
        return result;
    }
}
