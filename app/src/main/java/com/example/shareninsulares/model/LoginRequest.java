package com.example.shareninsulares.model;

public class LoginRequest {
    public String studentId;
    public String password;

    public LoginRequest(String studentId, String password) {
        this.studentId = studentId;
        this.password = password;
    }
}
