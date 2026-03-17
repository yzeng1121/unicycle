package com.unicycle.payment.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.unicycle.auth.repository.UserRepository;
import com.unicycle.auth.service.EmailService;
import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.repository.ListingRepository;
import com.unicycle.payment.dto.PaymentIntentResponse;
import com.unicycle.profile.repository.UserProfilesRepository;

import lombok.RequiredArgsConstructor;

// TODO: customize merchant ID you register in Apple Developer if you plan to use Apple Pay
@Service
@RequiredArgsConstructor
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    private final ListingRepository listingRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public PaymentIntentResponse createPaymentIntent(UUID listingId, UUID buyerId, String currency) {
        Stripe.apiKey = secretKey;

        Optional<Listing> listingOpt = listingRepository.getListingInfo(listingId);
        if (listingOpt.isEmpty()) {
            return PaymentIntentResponse.builder()
                    .status("FAILURE")
                    .message("Listing not found.")
                    .build();
        }

        Listing listing = listingOpt.get();

        if (listing.isSold()) {
            return PaymentIntentResponse.builder()
                    .status("FAILURE")
                    .message("This item has already been sold.")
                    .build();
        }

        if (listing.getPrice() == null) {
            return PaymentIntentResponse.builder()
                    .status("FAILURE")
                    .message("Listing has no price.")
                    .build();
        }

        long amountInCents = listing.getPrice()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        String resolvedCurrency = (currency != null && !currency.isBlank()) ? currency : "usd";

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency(resolvedCurrency)
                .putMetadata("listingId", listingId.toString())
                .putMetadata("buyerId", buyerId.toString())
                .putMetadata("sellerId", listing.getUserId().toString())
                .build();

        try {
            PaymentIntent paymentIntent = PaymentIntent.create(params);
            return PaymentIntentResponse.builder()
                    .status("SUCCESS")
                    .message("Payment intent created.")
                    .clientSecret(paymentIntent.getClientSecret())
                    .paymentIntentId(paymentIntent.getId())
                    .build();
        } catch (StripeException e) {
            return PaymentIntentResponse.builder()
                    .status("FAILURE")
                    .message(e.getMessage())
                    .build();
        }
    }

    @Transactional
    public void handlePaymentIntentSucceeded(PaymentIntent paymentIntent) {
        String listingIdStr = paymentIntent.getMetadata().get("listingId");
        String buyerIdStr = paymentIntent.getMetadata().get("buyerId");
        String sellerIdStr = paymentIntent.getMetadata().get("sellerId");

        if (listingIdStr == null || buyerIdStr == null || sellerIdStr == null) return;

        UUID listingId = UUID.fromString(listingIdStr);
        UUID buyerId = UUID.fromString(buyerIdStr);
        UUID sellerId = UUID.fromString(sellerIdStr);

        // Mark listing as sold
        listingRepository.markAsSold(listingId);

        // Add to buyer's purchased list
        userProfilesRepository.addToPurchased(buyerId, listingId);

        // Notify seller via email
        userRepository.findByUserId(sellerId).ifPresent(seller -> {
            Optional<Listing> listingOpt = listingRepository.getListingInfo(listingId);
            String title = listingOpt.map(Listing::getTitle).orElse("your item");
            try {
                emailService.sendNotificationEmail(
                        seller.getEmail(),
                        "Your item sold on UniCycle!",
                        "<p>Great news! <strong>" + title + "</strong> has been purchased.</p>" +
                        "<p>Arrange pickup with the buyer through the app.</p>"
                );
            } catch (Exception e) {
                System.err.println("Failed to send seller notification: " + e.getMessage());
            }
        });
    }
}
