package com.cineticket.service;

import com.cineticket.entity.Order;
import com.cineticket.entity.Ticket;

public interface TicketService {
    
    /**
     * Generates a ticket PDF for the given order and sends it via email
     */
    Ticket generateAndSendTicket(Order order);
    
    /**
     * Generates only the PDF file without sending email
     */
    Ticket generateTicketPDF(Order order);
    
    /**
     * Sends the ticket PDF to the user's email
     */
    void sendTicketEmail(Order order, Ticket ticket);
    
    /**
     * Resends the ticket email for an existing ticket
     */
    void resendTicketEmail(Long orderId);
    
    /**
     * Retrieves a ticket by order ID
     */
    Ticket getTicketByOrderId(Long orderId);
    
    /**
     * Gets the ticket PDF file as byte array for download
     */
    byte[] getTicketFileBytes(Long orderId);
}
