package com.example.internlink;

public class InternshipModel {
    private String id;
    private String title;
    private String companyName;
    private String companyId;
    private String description;
    private String location;
    private String duration;
    private String skillsRequired;
    private String internshipType;
    private String semesterEligibility;
    private String educationCriteria;
    private long expiresAt;
    private long createdAt;
    private String status;
    private int applicantCount;

    public InternshipModel() {}

    public InternshipModel(String id, String title, String companyName, String companyId, String description, String location, String duration, long expiresAt, String status) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.companyId = companyId;
        this.description = description;
        this.location = location;
        this.duration = duration;
        this.expiresAt = expiresAt;
        this.status = status;
        this.applicantCount = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getCompanyId() { return companyId; }
    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }
    public String getSkillsRequired() { return skillsRequired; }
    public void setSkillsRequired(String skillsRequired) { this.skillsRequired = skillsRequired; }
    public String getInternshipType() { return internshipType; }
    public void setInternshipType(String internshipType) { this.internshipType = internshipType; }
    public String getSemesterEligibility() { return semesterEligibility; }
    public void setSemesterEligibility(String semesterEligibility) { this.semesterEligibility = semesterEligibility; }
    public String getEducationCriteria() { return educationCriteria; }
    public void setEducationCriteria(String educationCriteria) { this.educationCriteria = educationCriteria; }
    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getApplicantCount() { return applicantCount; }
    public void setApplicantCount(int applicantCount) { this.applicantCount = applicantCount; }
}
