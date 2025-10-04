package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Duration;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingMail(ByteArrayOutputStream baos, String to, Booking booking) throws MessagingException {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        String time = "Time: " + hours + " hours " + minutes + " minutes";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Seat Booking Awaiting Confirmation");
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
                        "- Please keep your QR code ready for entrance verification.\n"+
                        "- Do not share this QR code with others.\n" +
                        "- The QR code is valid only for the above booking.\n\n" +
                        "Thank you,\n" +
                        "iLibrary Management System",
                false
        );
        helper.addAttachment("booking_qrcode.jpg", new ByteArrayResource(baos.toByteArray()));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }


    }
    public void sendConformationMail(User user, Booking booking) throws MessagingException {
        String text = "Dear " + user.getUsername() + "," + "\n " +
                "\n" +
                "Congratulations! Your library seat booking has been CONFIRMED by the librarian.\n" +
                "\n" +
                "Here are your booking details:\n" +
                "\n" +
                "- Seat Number: " + booking.getSeat().getSeatNumber() + "\n" +
                "- Booking Date: " + booking.getBookingDate() + "\n" +
                "- Start Time: " + booking.getStartTime() + "\n" +
                "- End Time: " + booking.getEndTime() + "\n" +
                "- Booking Status: " + booking.getStatus() + "\n" +
                "\n" +
                "If you have any questions or need further assistance, feel free to reply to this email or contact the library staff.\n" +
                "\n" +
                "Thank you for choosing our library services!\n" +
                "We wish you a productive study session.\n" +
                "\n" +
                "Best regards,\n" +
                "iLibrary Management Team";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Library Seat Booking Confirmation");
        helper.setText(text,false);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }
    }
    public void sendCancellationMail(User user, Booking booking) throws MessagingException {
        String text = "Dear " + user.getUsername() + "," + "\n " +
                "\n" +
                "This is to confirm that your library seat booking has been successfully cancelled as per your request.\n" +
                "\n" +
                "Here are your booking details:\n" +
                "\n" +
                "- Seat Number: " + booking.getSeat().getSeatNumber() + "\n" +
                "- Booking Date: " + booking.getBookingDate() + "\n" +
                "- Start Time: " + booking.getStartTime() + "\n" +
                "- End Time: " + booking.getEndTime() + "\n" +
                "- Booking Status: " + booking.getStatus() + "\n" +
                "\n" +
                "If any refund applies, the amount will be processed within 2 business days.\n" +
                "\n" +
                "We’re sorry to see you cancel your booking, but hope to welcome you back in the library soon.\n" +
                "If you need to reserve a seat at another time or have any questions, please do not hesitate to contact us.\n" +
                "\n" +
                "Thank you for using our library booking system.\n" +
                "Best regards, \n" +
                "iLibrary Management Team";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Library Seat Booking Cancelled");
        helper.setText(text,false);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }
    }
    public void sendSignupMail(String to, String userName) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        String otp = generateRandom();
        helper.setTo(to);
        helper.setSubject("Welcome to the Library System!");
        helper.setText(
                "Dear " + userName + ",\n\n" +
                        "Congratulations! Your account has been successfully created in the Library Management System.\n" +
                        "You can now log in and reserve seats, manage your bookings, and make full use of our library resources.\n\n" +
                        "If you did not register for this account, please contact library staff immediately.\n\n" +
                        "Best regards,\niLibrary Management Team"
                , false);

        mailSender.send(message);
    }
    public void sendSubscriptionMail(User user , Subscription subs) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject(String.valueOf(subs.getType()) + " Library Subscription Confirmed");

        String body =
                "Dear " + user.getUsername() + ",\n\n" +
                        "Thank you for purchasing a " + String.valueOf(subs.getType()).toLowerCase() + " subscription for the Library.\n\n" +
                        "Subscription Details:\n" +
                        "  - Subscription Type: " + String.valueOf(subs.getType()) + "\n" +
                        "  - Valid From: " + subs.getStartDate() + "\n" +
                        "  - Valid Until: " + subs.getEndDate() + "\n" +
                        "  - Total Price: ₹" + subs.getPrice() + "\n\n" +
                        "You now have full access to all member benefits during the active period of your subscription.\n" +
                        "If you have any questions, please reply to this email or contact the library.\n\n" +
                        "Thank you for choosing our library services!\n\n" +
                        "Best regards,\niLibrary Management Team";

        helper.setText(body, false);
        mailSender.send(message);
    }
    public String generateRandom()
    {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(1_000_000);
        return String.format("%06d",otp);
    }

}
