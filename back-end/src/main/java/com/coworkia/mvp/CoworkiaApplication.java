package com.coworkia.mvp;

import com.coworkia.mvp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class CoworkiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoworkiaApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(UserRepository userRepository,
            com.coworkia.mvp.repository.ZoneRepository zoneRepository,
            com.coworkia.mvp.repository.DeskRepository deskRepository) {
        return args -> {
            // 1. Password fix
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String securePassword = encoder.encode("password");
            userRepository.findAll().forEach(user -> {
                user.setPassword(securePassword);
                userRepository.save(user);
            });

            // 2. Initialisation des Zones & Desks (Rule: Nom Zone + Lettre)
            if (zoneRepository.count() == 0) {
                com.coworkia.mvp.entity.Zone atlas = new com.coworkia.mvp.entity.Zone();
                atlas.setName("Espace Atlas");
                atlas.setCode("OPN1");
                atlas.setCapacity(20);
                zoneRepository.save(atlas);

                com.coworkia.mvp.entity.Zone gaia = new com.coworkia.mvp.entity.Zone();
                gaia.setName("Salle Gaia");
                gaia.setCode("MEET1");
                gaia.setCapacity(10);
                zoneRepository.save(gaia);

                // Création de quelques postes
                createDesk(deskRepository, atlas, "OPN1-A");
                createDesk(deskRepository, atlas, "OPN1-B");
                createDesk(deskRepository, gaia, "MEET1-A");
            }
        };
    }

    private void createDesk(com.coworkia.mvp.repository.DeskRepository repo,
            com.coworkia.mvp.entity.Zone zone, String code) {
        com.coworkia.mvp.entity.Desk desk = new com.coworkia.mvp.entity.Desk();
        desk.setZone(zone);
        desk.setCode(code);
        repo.save(desk);
    }
}
