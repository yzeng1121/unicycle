package com.unicycle.listings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
// TODO: may need to make a separate constructor for each type of listings
public class ListingCardDto {
    @Column(name = "item_id")
    private UUID itemId;
    private UUID userId;
    private String listingType;
    private String category;
    private String pickUpLocation;
    private String coverImage;

    private BigDecimal price;
    private String tradeFor;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate returnBy;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate pickUpBy;

}
