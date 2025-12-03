package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.DBMS.iLibrary.entity.SeatPayment;
import com.DBMS.iLibrary.entity.Subscription;
import com.DBMS.iLibrary.entity.User;
import com.DBMS.iLibrary.repository.BookingRepo;
import com.DBMS.iLibrary.repository.SeatPaymentRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.stripe.model.checkout.Session;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private UserService userService;
    @Autowired
    private BookingRepo bookingRepo;
    @Autowired
    private SeatPaymentRepository seatPaymentRepository;

    @Async
    public void sendBookingMail(ByteArrayOutputStream baos, String to, Booking booking) throws MessagingException {
        Duration duration = Duration.between(booking.getStartTime(), booking.getEndTime());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        String time = "Time: " + hours + " hours " + minutes + " minutes";
        //Date Formatter.
        String bookingDate = booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
        User user = booking.getUser();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Seat Booking Awaiting Confirmation");
        helper.setText(
                "Dear " + user.getUsername() + ",\n\n" +
                        "Your seat has been successfully booked in the library.\n" +
                        "Please find your unique QR code attached to this email. You will need to show this QR code at the library entrance for verification.\n" +
                        "Booking Details:\n" +
                        "- Seat: " + booking.getSeat().getSeatNumber() + "\n" +
                        "- Date: " + bookingDate + "\n" +
                        "- Status: " + booking.getStatus() + "\n" +
                        "- Time: " + time + "\n" +
                        "- Amount: ₹" + booking.getAmount() + "\n" +
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
            throw new RuntimeException("Failed to send booking mail.");
        }
    }

    @Async
    public void sendPaymentConformMail(Event event) throws MessagingException {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = (Session) deserializer.getObject().get();
        String username = session.getMetadata().get("username");
        User user = userService.findByUsername(username).get();
        List<Booking> allBookings = bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.PENDING);
        if (allBookings == null || allBookings.isEmpty()) {
            System.out.printf("No pending bookings found for user: %s", user.getUsername());
            return; // or throw custom exception if appropriate
        }
        Booking booking = new Booking();
        if (allBookings.size() > 1)
            booking = allBookings.get(allBookings.size() - 1);
        if (allBookings.size() == 1)
            booking = allBookings.get(0);

        List<SeatPayment> payments = seatPaymentRepository.findAllByUserId(user.getId());
        SeatPayment seatPayment = new SeatPayment();
        if (!payments.isEmpty()) {
            seatPayment = payments.get(0); // Choose the first or latest entry
            // Use seatPayment safely now
        }
        byte[] pdfBytes = createInvoicePdfBytes(booking, seatPayment);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Seat Payment Successful");
        helper.setText(
                "Dear " + user.getUsername() + ",\n\n" +
                        "We are pleased to confirm that your payment has been successfully processed. Your transaction is complete, please verify the earlier received QR at entrance to our librarian and officially confirmed your seat.\n" +

                        "Here are the details of your payment for your reference:\n" +

                        "Payment Status: Successful\n" +

                        "Payment Date: " + seatPayment.getTransactionDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy")) + "\n" +

                        "Transaction ID: " + seatPayment.getSessionId() + "\n" +

                        "Amount Paid: ₹" + booking.getAmount() + "\n" +

                        "Payment Method: Stripe Credit Card" + "\n" +

                        "Please keep this email as your official receipt. Your invoice is attached for your convenience. Should you have any questions, require assistance, or wish to make modifications, please do not hesitate to reach out to our support team at ilibrarymanagementteam@gmail.com." + "\n\n" +

                        "Thank you for choosing iLibrary. We appreciate your trust and look forward to providing you with an excellent experience.\n" +

                        "Best regards," + "\n" +
                        "iLibrary Management Team",
                false
        );
        // Attach PDF invoice
        helper.addAttachment("iLibrary_Invoice.pdf", new ByteArrayResource(pdfBytes));
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send payment conform mail.");
        }
    }

    public static byte[] createInvoicePdfBytes(Booking booking, SeatPayment seatPayment) {

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // (Same content creation code as before - add paragraphs, tables etc)
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            Paragraph title = new Paragraph("iLibrary Seat Booking Invoice")
                    .setFont(boldFont)
                    .setFontSize(18)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);
            document.add(new Paragraph(" "));
            // proper date format
            String bookingDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy"));
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{3, 3})).useAllAvailableWidth();
            headerTable.addCell(new Cell().add(new Paragraph("Invoice No: " + booking.getUser().getId())).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Date: " + bookingDate)).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("Name: " + booking.getUser().getUsername())).setBorder(Border.NO_BORDER));
            headerTable.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
            document.add(headerTable);

            document.add(new Paragraph(" "));
            double totalAmountPaid = 50.00 * booking.getHrs();
            double gst = totalAmountPaid * 0.18;
            double grandTotal = gst + totalAmountPaid;
            Paragraph billToTitle = new Paragraph("Bill To:").setFont(boldFont);
            document.add(billToTitle);
            document.add(new Paragraph(booking.getUser().getUsername() + "\n" + booking.getUser().getEmail()));
            document.add(new Paragraph(" "));

            Table bookingTable = new Table(UnitValue.createPercentArray(new float[]{6, 2, 2, 2})).useAllAvailableWidth();
            bookingTable.addHeaderCell(new Cell().add(new Paragraph("Description")).setFont(boldFont));
            bookingTable.addHeaderCell(new Cell().add(new Paragraph("Time")).setFont(boldFont));
            bookingTable.addHeaderCell(new Cell().add(new Paragraph("Rate (per hour)")).setFont(boldFont));
            bookingTable.addHeaderCell(new Cell().add(new Paragraph("Amount (INR)")).setFont(boldFont));

            bookingTable.addCell(new Cell().add(new Paragraph(booking.getSeat().getId() + ", " + booking.getSeat().getLocation() + ", " + booking.getSeat().getSeatNumber() + ".")));
            bookingTable.addCell(new Cell().add(new Paragraph(String.valueOf(booking.getHrs()))));
            bookingTable.addCell(new Cell().add(new Paragraph("50.00")));
            bookingTable.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", totalAmountPaid))));
            document.add(bookingTable);

            document.add(new Paragraph(" "));


            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{6, 6})).useAllAvailableWidth();

            totalsTable.addCell(new Cell().add(new Paragraph("Total Amount Paid:")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            totalsTable.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", totalAmountPaid))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(new Cell().add(new Paragraph("Payment Method:")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            totalsTable.addCell(new Cell().add(new Paragraph("Online via card")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            totalsTable.addCell(new Cell().add(new Paragraph("GST/Taxes Applied:")).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            totalsTable.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", gst))).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            // Bold for grand total
            totalsTable.addCell(new Cell().add(new Paragraph("Grand Total:").setFont(boldFont)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.LEFT));
            totalsTable.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", grandTotal)).setFont(boldFont)).setBorder(Border.NO_BORDER).setTextAlignment(TextAlignment.RIGHT));

            document.add(totalsTable);


            document.add(new Paragraph(" "));
            PdfFont italicFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);
            document.add(new Paragraph("Thank you for choosing iLibrary.").setTextAlignment(TextAlignment.CENTER).setFont(italicFont));
            document.add(new Paragraph("Your seat booking is confirmed. If you have any questions, contact ilibrarymanagementteam@gmail.com").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("iLibrary").setTextAlignment(TextAlignment.CENTER).setFont(boldFont));
            document.add(new Paragraph("Customer Care: ilibrarymanagementteam@gmail.com").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Website: To be launched soon.").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("This invoice is system-generated and valid as a tax/commercial invoice.").setFontSize(8).setTextAlignment(TextAlignment.CENTER));

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF invoice.");
        }
    }

    @Async
    public void sendPaymentCancelMail(Event event) throws MessagingException {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = (Session) deserializer.getObject().get();
        String username = session.getMetadata().get("username");
        User user = userService.findByUsername(username).get();
        List<Booking> allBookings = bookingRepo.findAllByUserIdAndStatus(user.getId(), Booking.BookingStatus.PENDING);
        Booking booking = allBookings.get(0);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setSubject("Payment Unsuccessful – Seat Booking Cancelled at iLibrary");
        helper.setText(
                "Dear " + user.getUsername() + ",\n" +
                        "\n" +
                        "We noticed that your recent payment attempt for seat booking on iLibrary was unsuccessful or cancelled.  \n" +
                        "Unfortunately, your seat has not been confirmed due to the incomplete payment process.\n" +
                        "Here are the details of your attempted booking:\n" +
                        "\n" +
                        "• Booking Date: " + booking.getBookingDate().format(DateTimeFormatter.ofPattern("MMMM dd,yyyy")) + "\n" +
                        "• Seat Number: " + booking.getSeat().getSeatNumber() + "\n" +
                        "• Hours Selected: " + booking.getHrs() + "\n" +
                        "• Amount: ₹" + booking.getAmount() + "\n" +
                        "• Payment Status: Cancelled / Failed  \n" +
                        "\n" +
                        "If this was a mistake or an interruption during checkout, don’t worry — you can try completing the payment again through your iLibrary dashboard.  \n" +
                        "Please note that your selected seat has been released back to the available pool, and you’ll need to re-book if you still wish to reserve it.  \n" +

                        "For any issues or assistance, feel free to reach out to our support team at ilibrarymanagementteam@gmail.com.  \n" +

                        "We appreciate your interest in iLibrary and hope to see you back soon.\n" +

                        "Warm regards,  \n" +
                        "iLibrary Management Team  \n" +
                        "_“Read. Learn. Grow.”_",
                false
        );
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send payment conform mail.");
        }


    }

    @Async
    public void sendConformationMail(User user, Booking booking) throws MessagingException {
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

            throw new RuntimeException("Failed to send seat booking mail.");
        }
    }

    @Async
    public void sendCancellationMail(User user, Booking booking) throws MessagingException {
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
            throw new RuntimeException("Failed to send cancellation mail.");
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
                        "  - Total Price: ₹" + subs.getAmount() + "\n\n" +
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
                """
                        Dear %s,
                        We’re sorry to see you go.
                        This email is to confirm that your subscription with iLibrary Management System has been successfully canceled as of %s. \
                        You will continue to have access to our services until %s, after which your access will be discontinued.
                        If you have any questions or need assistance, please don’t hesitate to contact us at ilibrarymanagementteam@gmail.com.
                        We value your feedback. If you have a moment, please let us know the reason for your cancellation—it helps us improve our services.
                        Thank you for having been part of our community, and we hope to welcome you back in the future.
                        Best regards,
                        iLibrary Management Team
                        """,
                user.getUsername(), cancelDate, endDate
        );

        helper.setText(body, false);
        mailSender.send(message);
    }

    @Async
    public void sendSubscriptionPaymentConformMail(Event event) throws MessagingException {
        // Deserialize Stripe event data safely
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = (Session) deserializer.getObject()
                .orElseThrow(() -> new IllegalArgumentException("Unable to deserialize Stripe session"));

        String username = session.getMetadata().get("username");

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        String body = """
                Hello %s,<br><br>
                Your subscription payment has been <b>successfully processed</b>.<br>
                <b>Amount Paid:</b> ₹%.2f<br>
                <b>Payment ID:</b> %s<br><br>
                Thank you for your subscription!<br>
                - The iLibrary Management Team
                """.formatted(user.getUsername(), session.getAmountTotal() / 100.0, session.getId());
        helper.setSubject("Subscription Payment Successful");
        helper.setTo(user.getEmail());
        helper.setText(body, true);
        mailSender.send(message);
    }

    @Async
    public void sendSubscriptionPaymentCancelMail(Event event) throws MessagingException {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        Session session = (Session) deserializer.getObject()
                .orElseThrow(() -> new IllegalArgumentException("Unable to deserialize Stripe session"));

        String username = session.getMetadata().get("username");
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String subject = "Subscription Payment Canceled";
        String body = """
                Hello %s,<br><br>
                Unfortunately, your subscription payment was <b>not completed</b> or was <b>canceled</b>.<br><br>
                <b>Payment ID:</b> %s<br>
                <b>Reason:</b> Payment failed or was canceled before completion.<br><br>
                Don’t worry — you can retry your payment anytime to continue your subscription.<br>
                <br>
                — The iLibrary Management Team
                """.formatted(user.getUsername(), session.getId());

        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);

        mailSender.send(message);
    }

    @Async
    public void sendSeatExpiryMail(Booking booking) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        String username = booking.getUser().getUsername();
        String seatNumber = booking.getSeat().getSeatNumber();
        String startTime = booking.getStartTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String endTime = booking.getEndTime().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
        String subject = "Your seat booking has ended";
        String body = """
                Hi %s,
                
                We hope you had a productive time at iLibrary.
                
                This is to inform you that your seat booking (Seat No. %s)\s
                from %s to %s has now ended.
                
                The seat has been automatically released and is now available for new bookings.
                
                If you’d like to continue using the same seat,\s
                please make a new booking through your iLibrary dashboard.
                
                Thank you for using iLibrary!
                Keep learning and exploring. 📚
                
                Warm regards,
                Team iLibrary
                """.formatted(username, seatNumber, startTime, endTime);
        helper.setTo(booking.getUser().getEmail());
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
    }

    @Async
    public void sendSubscriptionExpiryMail(Subscription subscription) throws MessagingException{
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message,true);

        String username = subscription.getUser().getUsername();
        String planType = String.valueOf(subscription.getType());
        String expiryDate = subscription.getEndDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));

        String subject = "Your iLibrary Subscription Has Expired — Renew to Stay Connected!";
        String body = """
                Hi %s,
                
                We wanted to let you know that your iLibrary subscription\s
                Plan: %s has expired on %s.
                
                To continue accessing premium features like online seat booking,
                digital resources, and priority support, please renew your subscription today.
                
                You can renew easily by visiting your account’s Subscription page or by clicking the link below:
                {{renewalLink}}
                
                We appreciate your continued support of iLibrary.\s
                Stay connected to knowledge and community learning!
                
                Best regards,
                Team iLibrary
                """.formatted(username,planType,expiryDate);

        helper.setTo(subscription.getUser().getEmail());
        helper.setSubject(subject);
        helper.setText(body,true);
        mailSender.send(message)    ;
    }
}
