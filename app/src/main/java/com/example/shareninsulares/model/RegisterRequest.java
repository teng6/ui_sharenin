package com.example.shareninsulares.model;

public class RegisterRequest {
    public String studentId;
    public String email;
    public String fullName;
    public String password;
    public String campus;

    public RegisterRequest(String studentId, String email, String fullName,
                           String password, String campus) {
        this.studentId = studentId;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
        this.campus = campus;
    }
}
