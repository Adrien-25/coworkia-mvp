package com.coworkia.mvp.service;

import com.coworkia.mvp.entity.Booking;
import com.coworkia.mvp.entity.Invoice;
import com.coworkia.mvp.repository.BookingRepository;
import com.coworkia.mvp.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final InvoiceRepository invoiceRepository;

    public BookingService(BookingRepository bookingRepository, InvoiceRepository invoiceRepository) {
        this.bookingRepository = bookingRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        List<Booking> overlapping = bookingRepository.findOverlappingBookings(
                booking.getDesk().getId(),
                booking.getStartTime(),
                booking.getEndTime());

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Espace déjà réservé pour ce créneau.");
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Génération automatique de la facture
        Invoice invoice = new Invoice();
        invoice.setBooking(savedBooking);
        invoice.setAmount(savedBooking.getPrice());
        invoice.setIsPaid(false);
        invoiceRepository.save(invoice);

        return savedBooking;
    }

    public List<Booking> getUserBookings(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id).orElse(null);
    }

    @Transactional
    public Booking updateBooking(Long bookingId, Booking updatedBooking) {
        Booking existing = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée."));

        if (existing.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new RuntimeException("Impossible de modifier une réservation annulée.");
        }

        if (updatedBooking.getDesk() == null || updatedBooking.getDesk().getId() == null) {
            throw new RuntimeException("Le poste est obligatoire.");
        }
        if (updatedBooking.getStartTime() == null || updatedBooking.getEndTime() == null) {
            throw new RuntimeException("Les dates de début et de fin sont obligatoires.");
        }
        if (!updatedBooking.getEndTime().isAfter(updatedBooking.getStartTime())) {
            throw new RuntimeException("L'heure de fin doit être après l'heure de début.");
        }

        List<Booking> overlapping = bookingRepository.findOverlappingBookingsExcludingId(
                updatedBooking.getDesk().getId(),
                updatedBooking.getStartTime(),
                updatedBooking.getEndTime(),
                bookingId);

        if (!overlapping.isEmpty()) {
            throw new RuntimeException("Espace déjà réservé pour ce créneau.");
        }

        existing.setDesk(updatedBooking.getDesk());
        existing.setStartTime(updatedBooking.getStartTime());
        existing.setEndTime(updatedBooking.getEndTime());
        existing.setPrice(calculatePrice(updatedBooking.getStartTime(), updatedBooking.getEndTime()));

        Booking saved = bookingRepository.save(existing);

        invoiceRepository.findByBookingId(bookingId).ifPresent(invoice -> {
            invoice.setAmount(saved.getPrice());
            invoiceRepository.save(invoice);
        });

        return saved;
    }

    @Transactional
    public void cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Réservation non trouvée."));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private Double calculatePrice(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        long billableHours = Math.max(1, (long) Math.ceil(minutes / 60.0));
        return billableHours * 10.0;
    }
}
