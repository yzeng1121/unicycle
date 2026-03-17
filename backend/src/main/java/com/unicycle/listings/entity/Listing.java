package com.unicycle.listings.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "listings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {
    // TODO: why did i have a user field rather than a userId field?
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "user_id", nullable = false)
    private UUID userId; 

    @Column(name = "listing_type", nullable = false)
    private String listingType;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private String brand;
    @Column(nullable = false)
    private String condition;
    private String category;
    @Column(name = "pick_up_location", nullable = false)
    private String pickUpLocation;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "image_urls", columnDefinition = "json")
    private List<String> imageUrls;  // List of S3 URLs

    @Builder.Default
    @Column(nullable = false)
    private boolean sold = false;

    // optional fields
    private BigDecimal price;
    @Column(name = "trade_for")
    private String tradeFor;
    @Column(name = "return_by")
    private LocalDate returnBy;
    @Column(name = "pick_up_by")
    private LocalDate pickUpBy;
    
}