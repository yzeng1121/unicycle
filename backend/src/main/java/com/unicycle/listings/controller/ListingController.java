/*
 * ListingController.java
 * Purpose: supports HTML endpoints that allow CRUD operations on listings
 */

package com.unicycle.listings.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unicycle.listings.dto.ListingCardDto;
import com.unicycle.listings.dto.ListingDto;
import com.unicycle.listings.dto.ListingPatchDto;
import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.repository.ListingRepository;
import com.unicycle.listings.service.ImageUploadService;
import com.unicycle.listings.service.ListingService;
import com.unicycle.profile.repository.UserProfilesRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/api/listings")
@RestController
public class ListingController {
    private final ImageUploadService imageUploadService;
    private final ListingRepository listingRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final ListingService listingService;

    @PostMapping("/create")
    public ResponseEntity<?> createListingWithImages(ListingDto input) {
        try {
            // 1. Upload images first
            List<String> imageUrls = imageUploadService.uploadMultipleImages(input.getImages(), "listings");
            
            // 2. Create listing
            Listing listing = Listing.builder()
                .userId(input.getUserId())
                .listingType(input.getListingType())
                .title(input.getTitle())
                .description(input.getDescription())
                .createdAt(LocalDateTime.now())
                .brand(input.getBrand())
                .condition(input.getCondition())
                .category(input.getCategory())
                .pickUpLocation(input.getPickUpLocation())
                .imageUrls(imageUrls)

                .price(input.getPrice() != null ? input.getPrice() : null)
                .tradeFor(input.getTradeFor() != null ? input.getTradeFor() : null)
                .returnBy(input.getReturnBy() != null ? input.getReturnBy() : null)
                .pickUpBy(input.getPickUpBy() != null ? input.getPickUpBy() : null)
                .build();
            
            // 3. Save listing to database
            Listing savedListing = listingRepository.save(listing);

            // 4. Add listing to user's listings list
            UUID profileId = userProfilesRepository.getProfileIdByUserId(input.getUserId());
            userProfilesRepository.addListingToProfile(profileId, savedListing.getItemId());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "listing", savedListing,
                "message", "Listing created with " + imageUrls.size() + " images"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to create listing: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<?> getListing(@PathVariable UUID listingId) {
        try {
            Optional<Listing> fetchedListing = listingRepository.getListingInfo(listingId);
            
            if (fetchedListing.isPresent()) {
                Listing listing = fetchedListing.get();

                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "listing", listing,
                    "message", "Listing with itemId =" + listingId + " fetched."
                ));
            } else {
                return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch listing data"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch listing data: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{listingId}/get-cover-photo")
    public ResponseEntity<?> getCoverPhoto(@PathVariable UUID listingId) {
        try {
            String imageUrl = listingRepository.getCoverPhoto(listingId);
            System.out.println("Fetched cover image URL: " + imageUrl);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "imageUrl", imageUrl,
                "message", "Image with URL=" + imageUrl + " fetched."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch cover photo: " + e.getMessage()
            ));
        }
    }


    // get all listing card info from search results
    @GetMapping("/search")
    public ResponseEntity<?> searchListings(
        @RequestParam UUID userId,
        @RequestParam String query
    ) {
        try {
            // TODO: figure out this limit, offset stuff
            List<Object[]> results = listingRepository.searchListings(query, userId);
            System.out.println("Fetched set of listings..." + results);
            System.out.println("Found " + results.size() + " results");
            List<ListingCardDto> listings = new java.util.ArrayList<>();

            for (Object[] obj : results) {
                ListingCardDto listing = ListingCardDto.builder()
                    .itemId((UUID) obj[0]) 
                    .userId((UUID) obj[1])
                    .listingType((String) obj[2])
                    .category((String) obj[3])
                    .pickUpLocation((String) obj[4])
                    .coverImage((String) obj[5])
                    .price(obj[6] != null ? (BigDecimal) obj[6] : BigDecimal.ZERO)
                    .tradeFor((String) obj[7])
                    .returnBy(obj[8] != null ? ((java.sql.Date) obj[8]).toLocalDate() : null)
                    .pickUpBy(obj[9] != null ? ((java.sql.Date) obj[9]).toLocalDate() : null)
                    .build();

                listings.add(listing);
            }
            return ResponseEntity.ok(listings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to fetch listings: " + e.getMessage()
            ));
        }
    }

    // allows user to edit their own listings
    @PatchMapping("/{listingId}")
    public ResponseEntity<?> editListing(@PathVariable UUID listingId, @RequestParam UUID userId, @RequestBody ListingPatchDto productRequest) {
        try {
            // TODO: make sure that the userId matches the userId in the listing
            if (userId != productRequest.getUserId()) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "User can not modify a listing that's not theirs."
                ));
            }

            Listing updatedListing = listingService.patchListing(listingId, productRequest);
            return ResponseEntity.ok(updatedListing);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // allows user to delete their own listings
    @DeleteMapping("/{listingId}")
    public ResponseEntity<?> deleteListing(@PathVariable UUID listingId, @RequestParam UUID userId, @RequestBody ListingPatchDto productRequest) {
        try {
            // TODO: make sure that the userId matches the userId in the listing
            if (userId != productRequest.getUserId()) {
                return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "User can not modify a listing that's not theirs."
                ));
            }

            Optional<Listing> fetchedListing = listingRepository.getListingInfo(listingId);

            if (!fetchedListing.isPresent()) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Listing not found."
                ));
            }

            listingRepository.deleteById(listingId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Failed to delete listing: " + e.getMessage()
            ));
        }
    }

    // TODO: update a listing when sold out, no longer available, etc. (UPDATE)
    // TODO: update a listing's price, return by date, pickup location, etc.
    // TODO: delete a listing when sold out if wanted to (DELETE)
}
