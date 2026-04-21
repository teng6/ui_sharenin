package com.example.shareninsulares.model;

import java.math.BigDecimal;

public class CreateListingRequest {
    public String title;
    public String description;
    public BigDecimal price;
    public String category;
    public String type; // "SELL", "RENT", "FREE"
    public String imageUrl;

    public CreateListingRequest(String title, String description, BigDecimal price,
                                String category, String type, String imageUrl) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.category = category;
        this.type = type;
        this.imageUrl = imageUrl;
    }
}
