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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Async
    public void sendBookingMail(ByteArrayOutputStream baos, String to, Booking booking) throws MessagingException {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        String time = "Time: " + hours + " hours " + minutes + " minutes";
        //Date Formatter.
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String bookingDate = booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Seat Booking Awaiting Confirmation");
        helper.setText(
                "Dear Student,\n\n" +
                        "Your seat has been successfully booked in the library.\n" +
                        "Please find your unique QR code attached to this email. You will need to show this QR code at the library entrance for verification.\n" +
                        "Booking Details:\n" +
                        "- Seat: " + booking.getSeat().getSeatNumber() + "\n" +
                        "- Date: " + bookingDate + "\n" +
                        "- Status: " + booking.getStatus() + "\n" +
                        "- Time: " + time + "\n" +
                        "Note:\n" +
                        "- Please keep your QR code ready for entrance verification.\n" +
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
    @Async
    public void sendConformationMail(User user, Booking booking) throws MessagingException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String bookingDate = booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
        //Time Formatter
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String startTime = booking.getStartTime().format(timeFormatter);
        String endTime = booking.getEndTime().format(timeFormatter);
        String text = "Dear " + user.getUsername() + "," + "\n " +
                "\n" +
                "Congratulations! Your library seat booking has been CONFIRMED by the librarian.\n" +
                "\n" +
                "Here are your booking details:\n" +
                "\n" +
                "- Seat Number: " + booking.getSeat().getSeatNumber() + "\n" +
                "- Booking Date: " + bookingDate + "\n" +
                "- Start Time: " + startTime + "\n" +
                "- End Time: " + endTime + "\n" +
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
        helper.setText(text, false);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }
    }
    @Async
    public void sendCancellationMail(User user, Booking booking) throws MessagingException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String bookingDate = booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
        //Time Formatter
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        String startTime = booking.getStartTime().format(timeFormatter);
        String endTime = booking.getEndTime().format(timeFormatter);

        String text = "Dear " + user.getUsername() + "," + "\n " +
                "\n" +
                "This is to confirm that your library seat booking has been successfully cancelled as per your request.\n" +
                "\n" +
                "Here are your booking details:\n" +
                "\n" +
                "- Seat Number: " + booking.getSeat().getSeatNumber() + "\n" +
                "- Booking Date: " + bookingDate + "\n" +
                "- Start Time: " + startTime + "\n" +
                "- End Time: " + endTime + "\n" +
                "- Booking Status: " + booking.getStatus() + "\n" +
                "\n" +
                "If any refund applies, the amount will be processed within 2 business days.\n" +
                "\n" +
                "We’re sorry to see you cancel your booking, but hope to welcome you back in the library soon.\n" +
                "If you need to reserve a seat at another time or have any questions, please do not hesitate to contact us at ilibrarymanagementteam@gmail.com.\n" +
                "\n" +
                "Thank you for using our library booking system.\n" +
                "Best regards, \n" +
                "iLibrary Management Team";
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Library Seat Booking Cancelled");
        helper.setText(text, false);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;  // or handle accordingly
        }
    }
    @Async
    public void sendSignupMail(User user) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("iLibrary System Signup");
        helper.setText(
                "Dear " + user.getUsername() + ",\n\n" +
                        "Congratulations! Your account has been successfully created in the Library Management System.\n" +
                        "You can now log in and reserve seats, manage your bookings, and make full use of our library resources.\n\n" +
                        "If you did not register for this account, please contact library staff immediately.\n\n" +
                        "Best regards,\niLibrary Management Team"
                , false);

        mailSender.send(message);
    }
    @Async
    public void sendSubscriptionMail(User user, Subscription subs) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String endDate = subs.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
        String startDate = subs.getStartDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));


        helper.setTo(user.getEmail());
        helper.setSubject(String.valueOf(subs.getType()) + " Library Subscription Confirmed");

        String body =
                "Dear " + user.getUsername() + ",\n\n" +
                        "Thank you for purchasing a " + String.valueOf(subs.getType()).toLowerCase() + " subscription for the Library.\n\n" +
                        "Subscription Details:\n" +
                        "  - Subscription Type: " + String.valueOf(subs.getType()) + "\n" +
                        "  - Valid From: " + startDate + "\n" +
                        "  - Valid Until: " + endDate + "\n" +
                        "  - Total Price: ₹" + subs.getPrice() + "\n\n" +
                        "You now have full access to all member benefits during the active period of your subscription.\n" +
                        "If you have any questions, please reply to this email or contact the library.\n\n" +
                        "Thank you for choosing our library services!\n\n" +
                        "Best regards,\niLibrary Management Team";

        helper.setText(body, false);
        mailSender.send(message);
    }
    @Async
    public void renewSubscriptionMail(User user, Subscription subscription) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String endDate = subscription.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));

        helper.setTo(user.getEmail());
        helper.setSubject("Your Subscription Has Been Renewed Successfully!");

        String body =
                "Dear " + user.getUsername() + ",\n" +
                        "\n" +
                        "Thank you for renewing your subscription with iLibrary Management System.\n" +
                        "We're pleased to inform you that your subscription has been successfully extended. Your new subscription end date is " + endDate + ".\n" +
                        "You can continue enjoying full access to our library resources, seat reservations, and all other membership benefits without interruption.\n" +
                        "If you have any questions or need assistance, please feel free to reach out to us at ilibrarymanagementteam@gmail.com.\n" +
                        "Thank you for being a valued member of our community. We look forward to supporting your learning and exploration for another subscription period!\n" +
                        "\n" +
                        "Best regards,\n" +
                        "iLibrary Management Team";

        helper.setText(body, false);
        mailSender.send(message);
    }
    @Async
    public void cancelSubscriptionMail(User user, Subscription subscription) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(user.getEmail());
        helper.setSubject("Your Subscription Cancellation Confirmation");

        // Format current date/time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm");
        String cancelDate = LocalDateTime.now(ZoneId.systemDefault()).format(formatter);

        // Format subscription end date, assuming subscription.getEndDate() is java.time.LocalDate or compatible
        String endDate = subscription.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        String body = String.format(
                "Dear %s,\n" +
                        "We’re sorry to see you go.\n" +
                        "This email is to confirm that your subscription with iLibrary Management System has been successfully canceled as of %s. " +
                        "You will continue to have access to our services until %s, after which your access will be discontinued.\n" +
                        "If you have any questions or need assistance, please don’t hesitate to contact us at ilibrarymanagementteam@gmail.com.\n" +
                        "We value your feedback. If you have a moment, please let us know the reason for your cancellation—it helps us improve our services.\n" +
                        "Thank you for having been part of our community, and we hope to welcome you back in the future.\n" +
                        "Best regards,\n" +
                        "iLibrary Management Team\n",
                user.getUsername(), cancelDate, endDate
        );

        helper.setText(body, false);
        mailSender.send(message);
    }

}
