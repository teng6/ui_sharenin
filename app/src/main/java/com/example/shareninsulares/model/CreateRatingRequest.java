package com.example.shareninsulares.model;

public class CreateRatingRequest {
    public long bookingId;
    public int score;
    public String comment;

    public CreateRatingRequest(long bookingId, int score, String comment) {
        this.bookingId = bookingId;
        this.score = score;
        this.comment = comment;
    }
}
