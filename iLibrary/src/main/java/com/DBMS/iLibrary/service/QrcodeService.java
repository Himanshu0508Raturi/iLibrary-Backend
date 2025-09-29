package com.DBMS.iLibrary.service;

import com.DBMS.iLibrary.entity.Booking;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import io.jsonwebtoken.io.IOException;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.file.Paths;

@Service
public class QrcodeService {
    @Autowired
    private MailService mailService;

    public void createQrCode(String data, String to , Booking booking) throws WriterException, java.io.IOException, MessagingException {
        BitMatrix matrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 500, 500);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "jpg", baos);
        mailService.sendBookingMail(baos,to,booking);

    }
}
