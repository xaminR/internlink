package com.example.internlink;

public class UserModel {
    private String uid;
    private String email;
    private String role;
    private long createdAt;
    private String verificationStatus;

    public UserModel() {
        // Required for Firestore
    }

    public UserModel(String uid, String email, String role, long createdAt, String verificationStatus) {
        this.uid = uid;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.verificationStatus = verificationStatus;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}
