package com.yash.resume_builder.controller;

import com.yash.resume_builder.service.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.yash.resume_builder.util.AppConstants.EMAILS;
import static com.yash.resume_builder.util.AppConstants.SEND_RESUME;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(EMAILS)
public class EmailController {

    private final EmailService emailService;

    @PostMapping(value = SEND_RESUME, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> sendResumeByEmail(
            @RequestPart("recipientEmail") String recipientEmail,
            @RequestPart("subject") String subject,
            @RequestPart("message") String message,
            @RequestPart("pdfFile") MultipartFile pdfFile,
            Authentication authentication
    ) throws IOException, MessagingException {

        // Validate the inputs
        Map<String, Object> response = new HashMap<>();
        if (Objects.isNull(recipientEmail) || Objects.isNull(pdfFile)) {
            response.put("success", false);
            response.put("message", "Missing required fields");
            return ResponseEntity.badRequest().body(response);
        }

        // Get the file data
        byte[] pdfBytes = pdfFile.getBytes();
        String originalFilename = pdfFile.getOriginalFilename();
        String filename = Objects.nonNull(originalFilename) ? originalFilename : "resume.pdf";

        // Prepare the email content
        String emailSubject = Objects.nonNull(subject) ? subject : "Resume Application";
        String emailBody = Objects.nonNull(message) ? message : "Please find my resume attached. \n\n Best Regards";

        // Call the service method
        emailService.sendEmailWithAttachment(recipientEmail, emailSubject, emailBody, pdfBytes, filename);

        // return response
        response.put("success", true);
        response.put("message", "Resume send successfully to " + recipientEmail);
        return ResponseEntity.ok(response);

    }
}
