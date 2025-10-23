package com.unicycle.listings.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.unicycle.listings.dto.ListingCardDto;
import com.unicycle.listings.entity.Listing;
import com.unicycle.listings.repository.ListingRepository;

// // TODO: integrate + implement vectors for larger database searching
public class SearchingService {
//     public List<ListingCardDto> searchWithFullText(String query, int limit, int offset) {
//         private final ListingRepository listingRepository;
//         String processedQuery = processForBooleanMode(query);
        
//         List<Object[]> results = listingRepository.searchWithBooleanFullText(processedQuery, limit, offset);
        
//         return results.stream()
//             .map(row -> {
//                 Listing listing = (Listing) row[0];
//                 Double relevanceScore = (Double) row[1];
                
//                 ListingCardDto dto = convertToCardDto(listing);
//                 // You can use relevance score for ranking
//                 return dto;
//             })
//             .collect(Collectors.toList());
//     }
    
//     private String processForBooleanMode(String query) {
//         // Convert "nike shoes red" to "+nike +shoes +red" (all words required)
//         // Or to "nike shoes red" (any word matches)
//         return Arrays.stream(query.trim().split("\\s+"))
//             .filter(word -> word.length() > 1)
//             .map(word -> "+" + word) // Require all words
//             .collect(Collectors.joining(" "));
//     }
}
