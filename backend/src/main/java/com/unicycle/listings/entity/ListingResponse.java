package com.unicycle.listings.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ListingResponse {
    @Id
    @Column(name = "item_id")
    private UUID itemId;
    @Column(name = "user_id", nullable = false)
    private UUID userId; 
    @Column(nullable = false)
    private String title;
    @Column(name = "pick_up_location", nullable = false)
    private String pickUpLocation;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;  // List of S3 URLs
    private BigDecimal price;
}
