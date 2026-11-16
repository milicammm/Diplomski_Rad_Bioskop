package com.cineticket.controller;

import com.cineticket.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger log = LoggerFactory.getLogger(TicketController.class);

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * GET /api/tickets/{orderId}
     * Download ticket PDF for an order
     */
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<byte[]> downloadTicket(@PathVariable Long orderId) {
        log.info("Download ticket requested for order ID: {}", orderId);
        
        try {
            byte[] pdfBytes = ticketService.getTicketFileBytes(orderId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "ticket-" + orderId + ".pdf");
            headers.setContentLength(pdfBytes.length);

            log.info("Ticket PDF downloaded successfully for order ID: {}", orderId);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            log.error("Error downloading ticket for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * GET /api/tickets/{orderId}/info
     * Get ticket information (metadata) for an order
     */
    @GetMapping("/{orderId}/info")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getTicketInfo(@PathVariable Long orderId) {
        log.info("Ticket info requested for order ID: {}", orderId);
        
        try {
            var ticket = ticketService.getTicketByOrderId(orderId);
            
            var response = new java.util.HashMap<>();
            response.put("id", ticket.getId());
            response.put("ticketNumber", ticket.getTicketNumber());
            response.put("emailSent", ticket.getEmailSent());
            response.put("createdAt", ticket.getCreatedAt());
            response.put("qrCode", ticket.getQrCodeData() != null ? "available" : "unavailable");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving ticket info for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * POST /api/tickets/{orderId}/resend-email
     * Resend ticket email to user
     */
    @PostMapping("/{orderId}/resend-email")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> resendTicketEmail(@PathVariable Long orderId) {
        log.info("Resend ticket email requested for order ID: {}", orderId);
        
        try {
            ticketService.resendTicketEmail(orderId);

            var response = new java.util.HashMap<>();
            response.put("message", "Ticket email sent successfully");
            response.put("orderId", orderId);

            log.info("Ticket email resent successfully for order ID: {}", orderId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error resending ticket email for order ID: {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new java.util.HashMap<String, String>() {{
                        put("error", "Failed to resend ticket email");
                    }});
        }
    }
}
