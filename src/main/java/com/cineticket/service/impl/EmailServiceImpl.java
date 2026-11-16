package com.cineticket.service.impl;

import com.cineticket.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@cineticket.com}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            // Don't throw - email failures shouldn't break the flow
        }
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, byte[] fileBytes, String fileName) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body);

            // Add attachment
            helper.addAttachment(fileName, new ByteArrayResource(fileBytes));

            mailSender.send(mimeMessage);
            log.info("Email with attachment sent successfully to: {} (filename: {})", to, fileName);
        } catch (Exception e) {
            log.error("Error sending email with attachment to: {}", to, e);
            // Propagate: callers (e.g. resend-ticket-email) need to know the send actually failed
            // instead of reporting success to the user.
            throw new RuntimeException("Failed to send email with attachment to " + to, e);
        }
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML content

            mailSender.send(mimeMessage);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Error sending HTML email to: {}", to, e);
            // Don't throw - email failures shouldn't break the flow
        }
    }

    /**
     * Helper class for byte array resources
     */
    private static class ByteArrayResource extends org.springframework.core.io.ByteArrayResource {
        private final String filename;

        public ByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename;
        }

        public ByteArrayResource(byte[] byteArray) {
            super(byteArray);
            this.filename = "attachment";
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
