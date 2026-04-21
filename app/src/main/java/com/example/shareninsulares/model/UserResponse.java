package com.example.shareninsulares.model;

import java.math.BigDecimal;

public class UserResponse {
    public long id;
    public String studentId;
    public String email;
    public String fullName;
    public String campus;
    public String role;
    public int shareCoinsBalance;
    public BigDecimal reputationScore;
    public boolean isVerified;
    public boolean isRestricted;
}
