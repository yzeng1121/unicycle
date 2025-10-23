package com.unicycle.listings.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.dto.ListingPatchDto;
import com.unicycle.listings.repository.ListingRepository;

import jakarta.transaction.Transactional;
import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional
public class ListingService {
    @Autowired
    private ListingRepository listingRepository;

    public Listing patchListing(UUID itemId, ListingPatchDto listingPatch) {
        Optional<Listing> resultListing = listingRepository.getListingInfo(itemId);

        Listing existingListing = resultListing
            .orElseThrow(() -> new EntityNotFoundException("Listing not found with itemId: " + itemId));

        if (listingPatch.getListingType() != null) {
            existingListing.setListingType(listingPatch.getListingType());
        }
        if (listingPatch.getTitle() != null) {
            existingListing.setTitle(listingPatch.getTitle());
        }
        if (listingPatch.getDescription() != null) {
            existingListing.setDescription(listingPatch.getDescription());
        }
        if (listingPatch.getBrand() != null) {
            existingListing.setBrand(listingPatch.getBrand());
        }
        if (listingPatch.getCondition() != null) {
            existingListing.setCondition(listingPatch.getCondition());
        }
        if (listingPatch.getCategory() != null) {
            existingListing.setCategory(listingPatch.getCategory());
        }
        if (listingPatch.getPickUpLocation() != null) {
            existingListing.setPickUpLocation(listingPatch.getPickUpLocation());
        }
        if (listingPatch.getImages() != null) {
            existingListing.setImageUrls(listingPatch.getImages());
        }
        if (listingPatch.getPrice() != null) {
            existingListing.setPrice(listingPatch.getPrice());
        }
        if (listingPatch.getTradeFor() != null) {
            existingListing.setTradeFor(listingPatch.getTradeFor());
        }
        if (listingPatch.getReturnBy() != null) {
            existingListing.setReturnBy(listingPatch.getReturnBy());
        }
        if (listingPatch.getPickUpBy() != null) {
            existingListing.setPickUpBy(listingPatch.getPickUpBy());
        }

        return listingRepository.save(existingListing);
    }
}
