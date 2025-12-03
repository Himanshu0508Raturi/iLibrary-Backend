package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.SeatPayment;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatPaymentRepository;
import com.stripe.model.checkout.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static com.DBMS.iLibrary.entity.SeatPayment.PaymentStatus.PENDING;


@Service
public class SeatPaymentService {
    @Autowired
    private SeatPaymentRepository seatPaymentRepository;
    @Autowired
    private BookingRepo bookingRepo;

    //method to call before webhook
    public void saveDataBeforePayment(Booking booking, String sessionId) {
        try {
            SeatPayment seatPayment = new SeatPayment();
            seatPayment.setUser(booking.getUser());
            seatPayment.setSessionId(sessionId);
            double totalAmountPaid = 50.00 * booking.getHrs();
            double gst = totalAmountPaid * 0.18;
            double grandTotal = gst + totalAmountPaid;
            seatPayment.setAmount(BigDecimal.valueOf((grandTotal)));
            seatPayment.setTransactionDate(LocalDateTime.now());
            seatPayment.setSeat(booking.getSeat());
            seatPayment.setStatus(PENDING);
            seatPaymentRepository.save(seatPayment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //method to call after webhook proceed.(called from webhook controller)
    public void saveDataAfterPayment(Session session) {
        String sessionId = session.getId();
        Optional<SeatPayment> opPayment = seatPaymentRepository.findBySessionId(sessionId);
        if (opPayment.isPresent()) {
            SeatPayment seatPayment = opPayment.get();
            seatPayment.setStatus(SeatPayment.PaymentStatus.COMPLETED); // or CONFIRMED
            seatPaymentRepository.save(seatPayment);

            //marking isPaymentDone field in booking to 1 (Done).
            User user = seatPayment.getUser();
            List<Booking> allBooking = bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.valueOf("PENDING"));
            Booking booking = allBooking.get(allBooking.size() -1);
            booking.setIsPaymentDone(1);
            bookingRepo.save(booking);
        } else {
            // Optionally handle error/throw/log if no matching record is found
            System.err.println("No SeatPayment found with sessionId: " + sessionId);
        }
    }

    public void handleBookingPaymentFailure(Session session) {
        String sessionId = session.getId();
        Optional<SeatPayment> opPayment = seatPaymentRepository.findBySessionId(sessionId);
        if (opPayment.isPresent()) {
            SeatPayment seatPayment = opPayment.get();
            // Direct Cancel booking if there is any error in payment or no payment was made.
            seatPayment.setStatus(SeatPayment.PaymentStatus.FAILED); // or CONFIRMED
            seatPaymentRepository.save(seatPayment);
        } else {
            // Optionally handle error/throw/log if no matching record is found
            System.err.println("No SeatPayment found with sessionId: " + sessionId);
        }
    }
}