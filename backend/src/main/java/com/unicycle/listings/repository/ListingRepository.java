package com.unicycle.listings.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.dto.ListingCardDto;

@Repository
public interface ListingRepository extends JpaRepository<Listing, UUID> {
    @Query("SELECT l FROM Listing l WHERE l.itemId = :itemId")
    Optional<Listing> getListingInfo(@Param("itemId") UUID itemId);

    @Query(value = "SELECT image_urls->>0 FROM listings WHERE item_id = ?1", nativeQuery = true)
    String getCoverPhoto(UUID itemId);

    // TODO: integrate vectors for larger database search
    // SEARCH queries...
    
    // Basic search across title, brand, category
    @Query(value = """
    SELECT item_id, user_id, listing_type, category, pick_up_location,
            CASE WHEN image_urls IS NOT NULL AND json_array_length(image_urls) > 0
                    THEN image_urls->>0
                    ELSE NULL END as cover_image,
            price, trade_for, return_by, pick_up_by
        FROM listings 
        WHERE (LOWER(title) LIKE LOWER('%' || :query || '%')
            OR LOWER(brand) LIKE LOWER('%' || :query || '%')
            OR LOWER(category) LIKE LOWER('%' || :query || '%'))
        AND user_id != :userId
        ORDER BY created_at DESC
        """, nativeQuery = true)
    List<Object[]> searchListings(@Param("query") String query, @Param("userId") UUID userId);
        
    // Search with basic filters
    @Query(value = """
        SELECT * FROM listings 
        WHERE (LOWER(title) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(brand) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(category) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:category IS NULL OR LOWER(category) = LOWER(:category))
        AND (:listingType IS NULL OR listing_type = :listingType)
        AND (:condition IS NULL OR condition = :condition)
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        ORDER BY 
            CASE WHEN :sort = 'price_low' THEN price END ASC,
            CASE WHEN :sort = 'price_high' THEN price END DESC,
            CASE WHEN :sort = 'alphabetical' THEN title END ASC,
            created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<ListingCardDto> searchWithFilters(@Param("query") String query,
                                   @Param("category") String category,
                                   @Param("listingType") String listingType,
                                   @Param("condition") String condition,
                                   @Param("minPrice") Double minPrice,
                                   @Param("maxPrice") Double maxPrice,
                                   @Param("sort") String sort,
                                   @Param("limit") int limit,
                                   @Param("offset") int offset);
    
    // Count search results
    @Query(value = """
        SELECT COUNT(*) FROM listings 
        WHERE (LOWER(title) LIKE LOWER(CONCAT('%', :query, '%')) 
           OR LOWER(brand) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(description) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(category) LIKE LOWER(CONCAT('%', :query, '%')))
        AND (:category IS NULL OR LOWER(category) = LOWER(:category))
        AND (:listingType IS NULL OR listing_type = :listingType)
        AND (:condition IS NULL OR condition = :condition)
        AND (:minPrice IS NULL OR price >= :minPrice)
        AND (:maxPrice IS NULL OR price <= :maxPrice)
        """, nativeQuery = true)
    int countSearchResults(@Param("query") String query,
                          @Param("category") String category,
                          @Param("listingType") String listingType,
                          @Param("condition") String condition,
                          @Param("minPrice") Double minPrice,
                          @Param("maxPrice") Double maxPrice);
    
    // Simple autocomplete suggestions
    @Query(value = """
        SELECT DISTINCT 
            CASE 
                WHEN LOWER(title) LIKE LOWER(CONCAT(:query, '%')) THEN title
                WHEN LOWER(brand) LIKE LOWER(CONCAT(:query, '%')) THEN brand
                WHEN LOWER(category) LIKE LOWER(CONCAT(:query, '%')) THEN category
            END as suggestion
        FROM listings
        WHERE (LOWER(title) LIKE LOWER(CONCAT(:query, '%')) 
           OR LOWER(brand) LIKE LOWER(CONCAT(:query, '%'))
           OR LOWER(category) LIKE LOWER(CONCAT(:query, '%')))
        AND suggestion IS NOT NULL
        ORDER BY suggestion
        LIMIT :limit
        """, nativeQuery = true)
    List<String> findSearchSuggestions(@Param("query") String query, @Param("limit") int limit);
}
