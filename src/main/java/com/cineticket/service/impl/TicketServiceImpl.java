package com.cineticket.service.impl;

import com.cineticket.entity.Order;
import com.cineticket.entity.OrderItem;
import com.cineticket.entity.Ticket;
import com.cineticket.exception.ResourceNotFoundException;
import com.cineticket.exception.TicketException;
import com.cineticket.repository.TicketRepository;
import com.cineticket.service.EmailService;
import com.cineticket.service.TicketService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@Transactional
public class TicketServiceImpl implements TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    @Value("${app.ticket.storage.path:tickets}")
    private String ticketStoragePath;

    @Value("${app.ticket.verify.url:http://localhost:4200/verify-ticket}")
    private String ticketVerifyUrl;

    public TicketServiceImpl(TicketRepository ticketRepository, EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.emailService = emailService;
    }

    @Override
    // Runs in its own transaction so a failure here (e.g. email delivery) can't mark the
    // caller's transaction (payment/order confirmation) as rollback-only.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Ticket generateAndSendTicket(Order order) {
        log.info("Generating and sending ticket for order ID: {}", order.getId());
        Ticket ticket = generateTicketPDF(order);
        sendTicketEmail(order, ticket);
        return ticket;
    }

    @Override
    public Ticket generateTicketPDF(Order order) {
        try {
            log.info("Starting PDF generation for order ID: {}", order.getId());

            // Create storage directory if it doesn't exist
            File storageDir = new File(ticketStoragePath);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
                log.info("Created ticket storage directory: {}", ticketStoragePath);
            }

            // Check if ticket already exists
            Ticket existingTicket = ticketRepository.findByOrderId(order.getId()).orElse(null);
            if (existingTicket != null) {
                log.info("Ticket already exists for order ID: {}", order.getId());
                return existingTicket;
            }

            // Generate QR code
            String qrData = ticketVerifyUrl + "?ticketId=" + order.getId();
            byte[] qrCodeImage = generateQRCode(qrData, 300, 300);

            // Create PDF
            String fileName = "ticket-" + order.getId() + "-" + System.currentTimeMillis() + ".pdf";
            String filePath = ticketStoragePath + File.separator + fileName;

            // Save the Ticket entity first so its real ticket number (TKT-...) is generated
            // before it needs to be printed on the PDF, instead of falling back to the payment intent ID.
            Ticket ticket = new Ticket();
            ticket.setOrder(order);
            ticket.setFileName(fileName);
            ticket.setFilePath(filePath);
            ticket.setQrCodeData(Base64.getEncoder().encodeToString(qrCodeImage));
            ticket.setEmailSent(false);
            ticket = ticketRepository.save(ticket);

            ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(pdfOutputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4);

            // Add header
            Paragraph header = new Paragraph("MOVIE TICKET")
                    .setFontSize(24)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            // Add ticket number
            Paragraph ticketNum = new Paragraph("Ticket Number: " + ticket.getTicketNumber())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(12);
            document.add(ticketNum);

            document.add(new Paragraph("\n"));

            // User info
            Table userTable = new Table(new float[]{1, 1});
            userTable.setWidth(UnitValue.createPercentValue(100));
            userTable.addCell(new Cell().add(new Paragraph("Customer Name:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            userTable.addCell(new Cell().add(new Paragraph(order.getUser().getFirstName() + " " + order.getUser().getLastName())));
            userTable.addCell(new Cell().add(new Paragraph("Email:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            userTable.addCell(new Cell().add(new Paragraph(order.getUser().getEmail())));
            document.add(userTable);

            document.add(new Paragraph("\n"));

            // Movie and screening details
            Table movieTable = new Table(new float[]{1, 1});
            movieTable.setWidth(UnitValue.createPercentValue(100));

            if (!order.getOrderItems().isEmpty()) {
                OrderItem firstItem = order.getOrderItems().get(0);
                movieTable.addCell(new Cell().add(new Paragraph("Movie Title:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                movieTable.addCell(new Cell().add(new Paragraph(firstItem.getScreening().getMovie().getTitle())));
                movieTable.addCell(new Cell().add(new Paragraph("Theater:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                movieTable.addCell(new Cell().add(new Paragraph(firstItem.getScreening().getHall().getName())));
                movieTable.addCell(new Cell().add(new Paragraph("Date:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                movieTable.addCell(new Cell().add(new Paragraph(firstItem.getScreening().getScreeningDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))));
                movieTable.addCell(new Cell().add(new Paragraph("Time:").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
                movieTable.addCell(new Cell().add(new Paragraph(firstItem.getScreening().getScreeningTime().format(DateTimeFormatter.ofPattern("HH:mm")))));
            }

            document.add(movieTable);

            document.add(new Paragraph("\n"));

            // Seats details
            Paragraph seatsHeader = new Paragraph("RESERVED SEATS")
                    .setBold()
                    .setFontSize(12);
            document.add(seatsHeader);

            Table seatsTable = new Table(new float[]{0.33f, 0.33f, 0.34f});
            seatsTable.setWidth(UnitValue.createPercentValue(100));
            seatsTable.addCell(new Cell().add(new Paragraph("Row").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            seatsTable.addCell(new Cell().add(new Paragraph("Seat").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));
            seatsTable.addCell(new Cell().add(new Paragraph("Price").setBold()).setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY));

            for (OrderItem item : order.getOrderItems()) {
                seatsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getSeat().getRowNumber()))));
                seatsTable.addCell(new Cell().add(new Paragraph(String.valueOf(item.getSeat().getSeatNumber()))));
                seatsTable.addCell(new Cell().add(new Paragraph("$" + item.getPrice())));
            }

            document.add(seatsTable);

            document.add(new Paragraph("\n"));

            // Total price
            Paragraph totalPara = new Paragraph("TOTAL: $" + order.getTotalPrice())
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT);
            document.add(totalPara);

            document.add(new Paragraph("\n\n"));

            // QR Code
            ImageData imageData = ImageDataFactory.create(qrCodeImage);
            Image qrImage = new Image(imageData).setWidth(150).setHeight(150);
            Paragraph qrPara = new Paragraph()
                    .add(qrImage)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(qrPara);

            Paragraph qrText = new Paragraph("Scan QR code to verify ticket")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10);
            document.add(qrText);

            document.add(new Paragraph("\n"));

            // Footer
            Paragraph footer = new Paragraph("This is your official movie ticket. Please bring it with you to the cinema.")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic()
                    .setFontSize(10);
            document.add(footer);

            document.close();

            // Save PDF to file
            byte[] pdfBytes = pdfOutputStream.toByteArray();
            Files.write(Paths.get(filePath), pdfBytes);
            log.info("PDF saved to: {}", filePath);

            log.info("Ticket created with ID: {}, ticket number: {}", ticket.getId(), ticket.getTicketNumber());

            return ticket;

        } catch (Exception e) {
            log.error("Error generating PDF for order ID: {}", order.getId(), e);
            throw new TicketException("Failed to generate ticket PDF: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendTicketEmail(Order order, Ticket ticket) {
        log.info("Sending ticket email for order ID: {}", order.getId());

        byte[] pdfBytes;
        try {
            pdfBytes = Files.readAllBytes(Paths.get(ticket.getFilePath()));
        } catch (Exception e) {
            log.error("Error reading ticket file for order ID: {}", order.getId(), e);
            throw new TicketException("Failed to read ticket file: " + e.getMessage(), e);
        }

        String subject = "Your Movie Ticket - " + order.getId();
        String body = "Dear " + order.getUser().getFirstName() + ",\n\n" +
                "Your movie ticket is attached to this email.\n" +
                "Ticket Number: " + ticket.getTicketNumber() + "\n\n" +
                "Please find your ticket attached and bring it to the cinema.\n\n" +
                "Best regards,\n" +
                "CineTicket Team";

        // Let send failures propagate: generateAndSendTicket's caller (PaymentServiceImpl)
        // already catches so payment confirmation isn't blocked, and resendTicketEmail's
        // caller needs to know the send actually failed instead of reporting success.
        emailService.sendEmailWithAttachment(
                order.getUser().getEmail(),
                subject,
                body,
                pdfBytes,
                ticket.getFileName()
        );

        ticket.setEmailSent(true);
        ticketRepository.save(ticket);
        log.info("Ticket email sent successfully for order ID: {}", order.getId());
    }

    @Override
    public void resendTicketEmail(Long orderId) {
        try {
            Ticket ticket = ticketRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket", "orderId", orderId));

            Order order = ticket.getOrder();
            sendTicketEmail(order, ticket);
            log.info("Ticket email resent for order ID: {}", orderId);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resending ticket email for order ID: {}", orderId, e);
            throw new TicketException("Failed to resend ticket email: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Ticket getTicketByOrderId(Long orderId) {
        return ticketRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "orderId", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] getTicketFileBytes(Long orderId) {
        try {
            Ticket ticket = getTicketByOrderId(orderId);
            return Files.readAllBytes(Paths.get(ticket.getFilePath()));
        } catch (Exception e) {
            log.error("Error reading ticket file for order ID: {}", orderId, e);
            throw new TicketException("Failed to retrieve ticket file: " + e.getMessage(), e);
        }
    }

    /**
     * Generates a QR code image as byte array
     */
    private byte[] generateQRCode(String data, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }
}
