package com.unicycle.listings.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
// TODO: may need to make a separate constructor for each type of listings
public class ListingDto {
    private UUID userId; 
    private String listingType;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private String brand;
    private String condition;
    private String category;
    private String pickUpLocation;
    private MultipartFile[] images;

    private BigDecimal price;
    private String tradeFor;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate returnBy;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate pickUpBy;

}
