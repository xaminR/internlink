package com.example.internlink;

public class CompanyModel {
    private String uid;
    private String companyName;
    private String email;
    private String website;
    private String phone;
    private String address;
    private String registrationStatus;
    private String yearsOfOperation;
    private String ntnNumber;
    private String verificationStatus;
    private String packageType;
    private int postsUsed;
    private long postsResetAt;
    private String upgradeRequestStatus;
    private String requestedPackage;

    public CompanyModel() {}

    public CompanyModel(String uid, String companyName, String ntnNumber, String verificationStatus) {
        this.uid = uid;
        this.companyName = companyName;
        this.ntnNumber = ntnNumber;
        this.verificationStatus = verificationStatus;
        this.packageType = "BRONZE";
        this.postsUsed = 0;
        this.postsResetAt = System.currentTimeMillis();
        this.upgradeRequestStatus = "NONE";
        this.requestedPackage = "";
        this.registrationStatus = "PENDING";
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public String getYearsOfOperation() { return yearsOfOperation; }
    public void setYearsOfOperation(String yearsOfOperation) { this.yearsOfOperation = yearsOfOperation; }
    public String getNtnNumber() { return ntnNumber; }
    public void setNtnNumber(String ntnNumber) { this.ntnNumber = ntnNumber; }
    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
    public String getPackageType() { return packageType; }
    public void setPackageType(String packageType) { this.packageType = packageType; }
    public int getPostsUsed() { return postsUsed; }
    public void setPostsUsed(int postsUsed) { this.postsUsed = postsUsed; }
    public long getPostsResetAt() { return postsResetAt; }
    public void setPostsResetAt(long postsResetAt) { this.postsResetAt = postsResetAt; }
    public String getUpgradeRequestStatus() { return upgradeRequestStatus; }
    public void setUpgradeRequestStatus(String upgradeRequestStatus) { this.upgradeRequestStatus = upgradeRequestStatus; }
    public String getRequestedPackage() { return requestedPackage; }
    public void setRequestedPackage(String requestedPackage) { this.requestedPackage = requestedPackage; }
}
