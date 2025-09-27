package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Duration;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    public void sendMail(ByteArrayOutputStream baos , String to, Booking booking) throws MessagingException {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        String time = "Time: " + hours + " hours " + minutes + " minutes";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);
        helper.setTo(to);

        helper.setSubject("Library Seat Booking Confirmation");
        helper.setText(
                "Dear Student,\n\n" +
                        "Your seat has been successfully booked in the library.\n\n" +
                        "Please find your unique QR code attached to this email. You will need to show this QR code at the library entrance for verification.\n\n" +
                        "Booking Details:\n" +
                        "- Seat: " + booking.getSeat().getSeatNumber() + "\n" +
                        "- Date: " + booking.getBookingDate() + "\n" +
                        "- Status: " + booking.getStatus() + "\n" +
                        "- Time: " + time + "\n" +
                        "Note:\n" +
                        "- Do not share this QR code with others.\n" +
                        "- The QR code is valid only for the above booking.\n\n" +
                        "Thank you,\n" +
                        "iLibrary Management System",
                false
        );
        helper.addAttachment("booking_qrcode.jpg", new ByteArrayResource(baos.toByteArray()));
        try {
            mailSender.send(message);
        } catch(Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }

    }
}
