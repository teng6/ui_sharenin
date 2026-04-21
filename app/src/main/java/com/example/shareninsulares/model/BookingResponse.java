package com.example.shareninsulares.model;

public class BookingResponse {
    public long id;
    public long listingId;
    public String listingTitle;
    public long buyerId;
    public String buyerName;
    public long sellerId;
    public String sellerName;
    public String status; // PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED
    public String message;
    public String createdAt;
    public String updatedAt;
}
