/*
 * ListingController.java
 * Purpose: supports HTML endpoints that allow CRUD operations on listings
 */

package com.unicycle.listings.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.unicycle.auth.entity.User;
import com.unicycle.listings.dto.ListingCardDto;
import com.unicycle.listings.dto.ListingDto;
import com.unicycle.listings.dto.ListingPatchDto;
import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.service.ListingService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RequestMapping("/api/listings")
@RestController
public class ListingController {
    private final ListingService listingService;

    @PostMapping("/create")
    public ResponseEntity<?> createListingWithImages(ListingDto input) {
        return ResponseEntity.ok(listingService.createListing(input));
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<Listing> getListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(listingService.getListing(listingId));
    }

    @GetMapping("/{listingId}/get-cover-photo")
    public ResponseEntity<String> getCoverPhoto(@PathVariable UUID listingId) {
        return ResponseEntity.ok(listingService.getFirstImageUrl(listingId));
    }


    // get all listing card info from search results
    @GetMapping("/search")
    public ResponseEntity<List<ListingCardDto>> searchListings(
        @RequestParam UUID userId,
        @RequestParam String query
    ) {
        return ResponseEntity.ok(listingService.searchListings(userId, query));
    }

    // allows user to edit their own listings
    @PatchMapping("/{listingId}")
    public ResponseEntity<Listing> editListing(@PathVariable UUID listingId, @AuthenticationPrincipal User user, @RequestBody ListingPatchDto productRequest) {
        return ResponseEntity.ok(listingService.updateListing(listingId, user.getUserId(), productRequest));
    }

    // allows user to delete their own listings
    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID listingId, @AuthenticationPrincipal User user, @RequestBody ListingPatchDto productRequest) {
        listingService.deleteListing(listingId, user.getUserId(), productRequest);
        return ResponseEntity.noContent().build();
    }

    // TODO: update a listing when sold out, no longer available, etc. (UPDATE)
    // TODO: update a listing's price, return by date, pickup location, etc.
    // TODO: delete a listing when sold out if wanted to (DELETE)
}
