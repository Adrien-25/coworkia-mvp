package com.coworkia.mvp.repository;

import com.coworkia.mvp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId AND b.status = 'CONFIRMED' AND " +
            "((b.startTime < :end AND b.endTime > :start))")
    List<Booking> findOverlappingBookings(@Param("deskId") Long deskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.desk.id = :deskId AND b.status = 'CONFIRMED' AND b.id <> :bookingId AND " +
            "((b.startTime < :end AND b.endTime > :start))")
    List<Booking> findOverlappingBookingsExcludingId(@Param("deskId") Long deskId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("bookingId") Long bookingId);

    List<Booking> findByUserId(Long userId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.desk.zone.id = :zoneId AND b.status = 'CONFIRMED' " +
           "AND b.startTime <= CURRENT_TIMESTAMP AND b.endTime >= CURRENT_TIMESTAMP")
    long countActiveRightNowByZone(@Param("zoneId") Long zoneId);

    @Query(value = """
            SELECT
                z.id AS zone_id,
                z.name AS zone_name,
                z.code AS zone_code,
                z.capacity AS zone_capacity,
                COALESCE(
                    SUM(
                        EXTRACT(EPOCH FROM (
                            LEAST(b.end_time, :toTs) - GREATEST(b.start_time, :fromTs)
                        )) / 3600.0
                    ),
                    0
                ) AS occupied_hours
            FROM zones z
            LEFT JOIN desks d ON d.zone_id = z.id
            LEFT JOIN bookings b ON b.desk_id = d.id
                AND b.status = 'CONFIRMED'
                AND b.start_time < :toTs
                AND b.end_time > :fromTs
            GROUP BY z.id, z.name, z.code, z.capacity
            ORDER BY z.name
            """, nativeQuery = true)
    List<Object[]> getZoneOccupationHistory(@Param("fromTs") LocalDateTime fromTs,
                                            @Param("toTs") LocalDateTime toTs);
}
