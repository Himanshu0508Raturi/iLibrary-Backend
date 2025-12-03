package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Seat;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatRepo;
import com.DBMS.iLibrary.repository.SubscriptionRepo;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static com.DBMS.iLibrary.entity.Subscription.SubscriptionStatus.EXPIRED;

@Service
public class SchedulerService {

    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private MailService mailService;
    @Autowired
    private SeatRepo seatRepo;
    @Autowired
    private SubscriptionRepo subscriptionRepo;

    @Scheduled(fixedRate = 10 * 60 * 1000) // 10 minutes= 600,000 ms
    public void checkAndRelease() {
        LocalDateTime now = LocalDateTime.now();
        // Find all booked seats whose booking period ended.
        List<Booking> expiredBookings = bookingRepo.findAllByStatusAndEndTimeBefore(Booking.BookingStatus.CONFIRMED, now);

        for (Booking booking : expiredBookings) {
            Seat seat = booking.getSeat();
            seat.setStatus(Seat.SeatStatus.AVAILABLE);
            booking.setStatus(Booking.BookingStatus.EXPIRED);
            bookingRepo.save(booking);
            seatRepo.save(seat);
            try {
                mailService.sendSeatExpiryMail(booking);
            } catch (MessagingException e) {
                System.out.println("Error while Sending seat Expiry mail in scheduler");
            }
        }
        // like a debugging monitor.
        if (!expiredBookings.isEmpty()) {
            System.out.println("Release " + expiredBookings.size() + " expires seats at " + now.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
        }
    }

    // expire old subscription.
    @Scheduled(fixedRate = 86_400_000) // every 24 hours
    public void expireOldSubscriptions() {

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> expired = subscriptionRepo.findByEndDateBeforeAndStatus(now, Subscription.SubscriptionStatus.valueOf("ACTIVE"));

        for (Subscription subs : expired) {
            subs.setStatus(Subscription.SubscriptionStatus.valueOf(String.valueOf(EXPIRED)));
            subscriptionRepo.save(subs);

            try {
                mailService.sendSubscriptionExpiryMail(subs);
            } catch (MessagingException e) {
                System.out.println("Error while Sending subscription Expiry mail in scheduler");
            }
        }
        if (!expired.isEmpty()) {
            System.out.println("Expired " + expired.size() + " subscriptions at " + now);
        }
    }

    // Delete expired subscription more than 30 days
    @Scheduled(cron = "0 0 2 * * ?")  // runs every day at 2 AM
    public void deleteExpiredSubscription() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        subscriptionRepo.deleteByEndDateBefore(cutoff);
        System.out.println("Deleted expired subscriptions older than 30 days.");
    }

    // Delete expired seat bookings more than 30 days.
    @Scheduled(cron = "0 0 2 * * ?")
    public void deleteExpiredSeatBooking() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        bookingRepo.deleteByEndTimeBefore(cutoff);
        System.out.println("Deleted expired seat bookings older than 30 days.");
    }
}
