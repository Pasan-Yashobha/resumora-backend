package com.yash.resume_builder.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.yash.resume_builder.document.Payment;
import com.yash.resume_builder.document.User;
import com.yash.resume_builder.dto.AuthResponse;
import com.yash.resume_builder.repository.PaymentRepository;
import com.yash.resume_builder.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.yash.resume_builder.util.AppConstants.PREMIUM;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String stripeWebhookSecret;

    // Initialize Stripe globally once the bean is created
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    //Creates a Stripe PaymentIntent and saves a pending Payment record.
    //Returns the Payment so the controller can send clientSecret to the frontend.

    public Payment createOrder(Object principal, String planType) throws StripeException {

        AuthResponse authResponse = authService.getProfile(principal);

        long amount = 99900L;       // Amount in cents ($999.00)
        String currency = "usd";
        String receipt = PREMIUM + "_" + UUID.randomUUID().toString().substring(0, 8);

        // Build the PaymentIntent params
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription("Resume Builder - " + planType + " Plan")
                .putMetadata("userId", authResponse.getId())
                .putMetadata("planType", planType)
                .putMetadata("receipt", receipt)
                // Automatic payment methods covers cards, wallets, etc.
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        // Create the PaymentIntent via Stripe API
        PaymentIntent paymentIntent = PaymentIntent.create(params);

        // Save to DB
        Payment newPayment = Payment.builder()
                .userId(authResponse.getId())
                .stripePaymentIntentId(paymentIntent.getId())
                .stripeClientSecret(paymentIntent.getClientSecret())
                .amount((int) amount)
                .currency(currency)
                .planType(planType)
                .status("created")
                .receipt(receipt)
                .build();

        return paymentRepository.save(newPayment);
    }

    //Verifies payment by fetching the PaymentIntent from Stripe directly.
    //More secure than trusting client-side confirmation alone.
    public boolean verifyPayment(String paymentIntentId) throws StripeException {

        try {
            // Retrieve the PaymentIntent from Stripe to check its real status
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {

                Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                        .orElseThrow(() -> new RuntimeException("Payment not found for id: " + paymentIntentId));

                // Capture the charge ID if available
                String chargeId = paymentIntent.getLatestCharge();

                payment.setStripeChargeId(chargeId);
                payment.setStatus("paid");
                paymentRepository.save(payment);

                upgradeUserSubscription(payment.getUserId(), payment.getPlanType());
                return true;
            }

            log.warn("PaymentIntent {} status is: {}", paymentIntentId, paymentIntent.getStatus());
            return false;

        } catch (Exception e) {
            log.error("Error verifying payment for intentId {}: ", paymentIntentId, e);
            return false;
        }
    }

    //Handles Stripe webhook events for async payment confirmation.
    //Use this as the primary confirmation mechanism in production.

    public void handleWebhook(String payload, String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            log.error("Invalid Stripe webhook signature: ", e);
            throw new RuntimeException("Invalid webhook signature");
        }

        log.info("Received Stripe webhook event: {}", event.getType());

        switch (event.getType()) {

            case "payment_intent.succeeded" -> {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new RuntimeException("Could not deserialize PaymentIntent"));

                paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                        .ifPresent(payment -> {
                            payment.setStripeChargeId(paymentIntent.getLatestCharge());
                            payment.setStatus("paid");
                            paymentRepository.save(payment);
                            upgradeUserSubscription(payment.getUserId(), payment.getPlanType());
                            log.info("Payment succeeded via webhook for intentId: {}", paymentIntent.getId());
                        });
            }

            case "payment_intent.payment_failed" -> {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new RuntimeException("Could not deserialize PaymentIntent"));

                paymentRepository.findByStripePaymentIntentId(paymentIntent.getId())
                        .ifPresent(payment -> {
                            payment.setStatus("failed");
                            paymentRepository.save(payment);
                            log.warn("Payment failed via webhook for intentId: {}", paymentIntent.getId());
                        });
            }

            default -> log.info("Unhandled webhook event type: {}", event.getType());
        }
    }

    private void upgradeUserSubscription(String userId, String planType) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
        existingUser.setSubscriptionPlan(planType);
        userRepository.save(existingUser);
        log.info("Upgraded subscription for userId: {}, planType: {}", userId, planType);
    }

    public List<Payment> getUserPayments(Object principal) {
        AuthResponse authResponse = authService.getProfile(principal);
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(authResponse.getId());
    }

    public Payment getPaymentDetails(String paymentIntentId) {
        return paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for intentId: " + paymentIntentId));
    }
}