package com.unicycle.listings.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.unicycle.listings.entity.ImageFolder;
import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.entity.ListingResponse;
import com.unicycle.exception.FailedToCreateListingException;
import com.unicycle.exception.FailedToFetchCoverPhotoException;
import com.unicycle.exception.FailedToFetchListingException;
import com.unicycle.exception.ListingNotFoundException;
import com.unicycle.exception.UnauthorizedListingAccessException;
import com.unicycle.listings.dto.ListingCardDto;
import com.unicycle.listings.dto.ListingDto;
import com.unicycle.listings.dto.ListingPatchDto;
import com.unicycle.listings.repository.ListingRepository;
import com.unicycle.profile.repository.UserProfilesRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class ListingService {
    private final ListingRepository listingRepository;
    private final UserProfilesRepository userProfilesRepository;
    private final ImageUploadService imageUploadService;

    public ListingResponse createListing(ListingDto input) {
        try {
            // 1. Upload images first
            List<String> imageUrls = imageUploadService.uploadMultipleImages(input.getImages(), ImageFolder.LISTINGS.getImagePath());
            
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
            ListingResponse response = buildListingResponse(savedListing);

            // 4. Add listing to user's listings list
            UUID profileId = userProfilesRepository.getProfileIdByUserId(input.getUserId());
            userProfilesRepository.addListingToProfile(profileId, savedListing.getItemId());

            return response;
        } catch (Exception e) { 
            throw new FailedToCreateListingException("Failed to create listing.");
        }
    }

    private ListingResponse buildListingResponse(Listing listing) {
        return ListingResponse.builder()
            .itemId(listing.getItemId())
            .userId(listing.getUserId())
            .title(listing.getTitle())
            .pickUpLocation(listing.getPickUpLocation())
            .imageUrls(listing.getImageUrls())
            .price(listing.getPrice())
            .build();
    }

    public Listing getListing(UUID listingId) {
        try {
            Optional<Listing> fetchedListing = listingRepository.getListingInfo(listingId);
            
            if (fetchedListing.isPresent()) {
                Listing listing = fetchedListing.get();
                return listing;
            } else {
                throw new FailedToFetchListingException("Failed to fetch listing data.");
            }
        } catch (Exception e) {
            throw new FailedToFetchListingException("Failed to fetch listing data.");
        }
    }

    public Listing patchListing(UUID itemId, ListingPatchDto listingPatch) {
        Optional<Listing> resultListing = listingRepository.getListingInfo(itemId);

        Listing existingListing = resultListing
            .orElseThrow(() -> new FailedToFetchListingException("Listing not found with itemId: " + itemId));

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

    public Listing updateListing(UUID listingId, UUID userId, ListingPatchDto productRequest) {
        try {
            // TODO: make sure that the userId matches the userId in the listing
            if (userId != productRequest.getUserId()) {
                throw new UnauthorizedListingAccessException("User can not modify a listing that's not theirs.");
            }
            return patchListing(listingId, productRequest);
        } catch (Exception e) {
            throw new ListingNotFoundException("Listing not found.");
        }
    }

    public void deleteListing(UUID listingId, UUID userId, ListingPatchDto productRequest) {
        try {
            // TODO: make sure that the userId matches the userId in the listing
            if (userId != productRequest.getUserId()) {
                throw new UnauthorizedListingAccessException("User can not delete a listing that's not theirs.");
            }
            Optional<Listing> fetchedListing = listingRepository.getListingInfo(listingId);
            if (!fetchedListing.isPresent()) throw new ListingNotFoundException("Listing not found.");
            listingRepository.deleteById(listingId);
        } catch (Exception e) {
            throw new FailedToFetchListingException("Failed to delete listing: " + e.getMessage());
        }
    }

    public String getFirstImageUrl(UUID listingId) {
        try {
            return listingRepository.getCoverPhoto(listingId);
        } catch (Exception e) {
            throw new FailedToFetchCoverPhotoException("Failed to fetch cover photo: " + e.getMessage());
        }
    } 

    public List<ListingCardDto> searchListings(UUID userId, String query) {
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
            return listings;
        } catch (Exception e) {
            throw new FailedToFetchListingException("Failed to fetch listings: " + e.getMessage());
        }
    }
}
