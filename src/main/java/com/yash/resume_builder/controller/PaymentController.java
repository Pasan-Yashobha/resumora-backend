package com.yash.resume_builder.controller;

import com.stripe.exception.StripeException;
import com.yash.resume_builder.document.Payment;
import com.yash.resume_builder.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.yash.resume_builder.util.AppConstants.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(PAYMENTS)
public class PaymentController {

    private final PaymentService paymentService;

    //Frontend calls this to get a clientSecret to initialize Stripe.js
    @PostMapping(PAYMENT_CREATE_ORDER)
    public ResponseEntity<?> createOrder(@RequestBody Map<String, String> request,
                                         Authentication authentication) throws StripeException {

        String planType = request.get("planType");
        if (!PREMIUM.equalsIgnoreCase(planType)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid plan type!"));
        }

        Payment payment = paymentService.createOrder(authentication.getPrincipal(), planType);

        // Return clientSecret - the frontend uses this to confirm payment with Stripe.js
        Map<String, Object> response = Map.of(
                "clientSecret", payment.getStripeClientSecret(),
                "paymentIntentId", payment.getStripePaymentIntentId(),
                "amount", payment.getAmount(),
                "currency", payment.getCurrency()
        );

        return ResponseEntity.ok(response);
    }

   //After Stripe.js confirms payment on the frontend,
   //verify server-side and activate the subscription.
    @PostMapping(VERIFY_PAYMENT)
    public ResponseEntity<?> verifyPayment(@RequestBody Map<String, String> request) throws StripeException {

        String paymentIntentId = request.get("paymentIntentId");

        if (Objects.isNull(paymentIntentId) || paymentIntentId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "paymentIntentId is required!"));
        }

        boolean isValid = paymentService.verifyPayment(paymentIntentId);

        if (isValid) {
            return ResponseEntity.ok(Map.of(
                    "message", "Payment verified successfully!",
                    "status", "success"
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", "Payment verification failed!",
                    "status", "failed"
            ));
        }
    }

    // Stripe webhook endpoint
    //Path: POST /api/payment/webhook
    @PostMapping(WEBHOOK)
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        try {
            paymentService.handleWebhook(payload, sigHeader);
            return ResponseEntity.ok(Map.of("received", true));
        } catch (RuntimeException e) {
            log.error("Webhook error: ", e);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping(PAYMENTS_HISTORY)
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        List<Payment> payments = paymentService.getUserPayments(authentication.getPrincipal());
        return ResponseEntity.ok(payments);
    }

    @GetMapping(PAYMENT_ORDER_DETAILS)
    public ResponseEntity<?> getOrderDetails(@PathVariable String paymentIntentId) {
        Payment payment = paymentService.getPaymentDetails(paymentIntentId);
        return ResponseEntity.ok(payment);
    }
}