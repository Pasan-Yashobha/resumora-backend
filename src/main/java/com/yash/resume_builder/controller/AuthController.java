package com.yash.resume_builder.controller;

import com.yash.resume_builder.dto.AuthResponse;
import com.yash.resume_builder.dto.LoginRequest;
import com.yash.resume_builder.dto.RegisterRequest;
import com.yash.resume_builder.service.AuthService;
import com.yash.resume_builder.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.yash.resume_builder.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {

    private final AuthService authService;
    private final FileUploadService fileUploadService;

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Inside AuthController - register(): {}", request);
        AuthResponse response = authService.register(request);
        log.info("Response from service: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        log.info("Inside AuthController - verifyEmail(): {}", token);
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Email verified successfully!"));
    }

    @PostMapping(UPLOAD_PROFILE)
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file) throws IOException {
        log.info("Inside AuthController - uploadImage(): {}", file);
        Map<String, String> response = fileUploadService.uploadSingleImage(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Inside AuthController - login(): {}", request);
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/validate")
//    public String testValidationToken() {
//        return "Token validation successfully!";
//    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        log.info("Inside AuthController - resendVerification(): {}", body);

        //Get the email from request
        String email = body.get("email");

        //Add the validations
        if (Objects.isNull(email)) {
            return ResponseEntity.badRequest().body(Map.of("message","Email is required"));
        }

        //Call the service method to resend verification link
        authService.resendVerification(email);

        //Return response
        return ResponseEntity.ok(Map.of("success", true, "message", "Verification email has been sent"));
    }

    @GetMapping(PROFILE)
    public ResponseEntity<?> getProfile(Authentication authentication) {
        log.info("Inside AuthController - getProfile(): {}", authentication);

        //Get the principal object
        Object principalObject = authentication.getPrincipal();

        //Call the service method
        AuthResponse currentProfile = authService.getProfile(principalObject);

        //return the response
        return ResponseEntity.ok(currentProfile);
    }
}
