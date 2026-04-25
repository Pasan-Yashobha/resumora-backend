package com.yash.resume_builder.controller;

import com.yash.resume_builder.service.TemplatesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.yash.resume_builder.util.AppConstants.TEMPLATES;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(TEMPLATES)
public class TemplatesController {

    private final TemplatesService templatesService;

    @GetMapping
    public ResponseEntity<?> getTemplates(Authentication authentication) {
        // Call the service method
        Map<String, Object> response = templatesService.getTemplates(authentication.getPrincipal());

        // Return the response
        return ResponseEntity.ok(response);
    }

}
