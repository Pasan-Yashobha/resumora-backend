//package com.yash.resume_builder.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    @Value("${spring.mail.properties.mail.smtp.from}")
//    private String fromEmail;
//
//    private final JavaMailSender mailSender;
//
//    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
//        log.info("Inside EmailService - sendHtmlEmail(): {}, {}, {}", to, subject, htmlContent);
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//        helper.setFrom(fromEmail);
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(htmlContent, true);
//        mailSender.send(message);
//    }
//
//    public void sendEmailWithAttachment(String to, String subject, String body, byte[] attachment, String filename) throws MessagingException {
//
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setFrom(fromEmail);
//        helper.setTo(to);
//        helper.setSubject(subject);
//        helper.setText(body);
//        helper.addAttachment(filename, new ByteArrayResource(attachment));
//        mailSender.send(message);
//    }
//}


package com.yash.resume_builder.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.email.from}")
    private String fromEmail;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        log.info("Inside EmailService - sendHtmlEmail(): {}, {}", to, subject);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Resumora", "email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        sendToBrevo(body);
    }

    public void sendEmailWithAttachment(String to, String subject, String content,
                                        byte[] attachment, String filename) {
        log.info("Inside EmailService - sendEmailWithAttachment(): {}, {}", to, subject);

        String base64File = Base64.getEncoder().encodeToString(attachment);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Resumora", "email", fromEmail));
        body.put("to", List.of(Map.of("email", to)));
        body.put("subject", subject);
        body.put("htmlContent", "<p>" + content + "</p>");
        body.put("attachment", List.of(Map.of(
                "content", base64File,
                "name", filename
        )));

        sendToBrevo(body);
    }

    private void sendToBrevo(Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);
            log.info("Brevo email sent successfully: {}", response.getStatusCode());
        } catch (Exception e) {
            log.error("Failed to send email via Brevo: {}", e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }
}