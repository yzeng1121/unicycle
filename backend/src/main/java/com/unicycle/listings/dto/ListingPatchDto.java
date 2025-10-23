package com.unicycle.listings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListingPatchDto {
    private UUID userId;
    private String listingType;
    private String title;
    private String description;
    private String brand;
    private String condition;
    private String category;
    private String pickUpLocation;
    private List<String> images;

    private BigDecimal price;
    private String tradeFor;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate returnBy;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate pickUpBy;

}
