package com.example.shareninsulares.model;

public class CreateBookingRequest {
    public long listingId;
    public String message;

    public CreateBookingRequest() {
        // Default constructor
    }

    public CreateBookingRequest(long listingId, String message) {
        this.listingId = listingId;
        this.message = message;
    }
}
