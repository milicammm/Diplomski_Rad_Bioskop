package com.cineticket.service;

public interface EmailService {
    
    /**
     * Sends a simple text email
     */
    void sendEmail(String to, String subject, String body);
    
    /**
     * Sends an email with file attachment
     */
    void sendEmailWithAttachment(String to, String subject, String body, byte[] fileBytes, String fileName);
    
    /**
     * Sends an HTML email
     */
    void sendHtmlEmail(String to, String subject, String htmlBody);
}
