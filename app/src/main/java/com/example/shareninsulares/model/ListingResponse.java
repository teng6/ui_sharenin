package com.example.shareninsulares.model;

import java.math.BigDecimal;

public class ListingResponse {
    public long id;
    public String title;
    public String description;
    public BigDecimal price;
    public String category;
    public String type;       // SELL, RENT, FREE
    public String status;     // ACTIVE, SOLD, RESERVED, INACTIVE
    public String campus;
    public String imageUrl;
    public long sellerId;
    public String sellerName;
    public BigDecimal sellerReputationScore;
    public String createdAt;
    public String updatedAt;
}
