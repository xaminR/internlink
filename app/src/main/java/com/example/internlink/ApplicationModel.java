package com.example.internlink;

public class ApplicationModel {
    private String applicationId;
    private String internshipId;
    private String internshipTitle;
    private String companyName;
    private String studentId;
    private String studentName;
    private String studentSkills;
    private String status; // APPLIED, SHORTLISTED, REJECTED, INTERVIEW
    private long timestamp;

    public ApplicationModel() {}

    public ApplicationModel(String applicationId, String internshipId, String internshipTitle, String companyName, String studentId, String studentName, String studentSkills, String status, long timestamp) {
        this.applicationId = applicationId;
        this.internshipId = internshipId;
        this.internshipTitle = internshipTitle;
        this.companyName = companyName;
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentSkills = studentSkills;
        this.status = status;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
    public String getInternshipId() { return internshipId; }
    public void setInternshipId(String internshipId) { this.internshipId = internshipId; }
    public String getInternshipTitle() { return internshipTitle; }
    public void setInternshipTitle(String internshipTitle) { this.internshipTitle = internshipTitle; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public String getStudentSkills() { return studentSkills; }
    public void setStudentSkills(String studentSkills) { this.studentSkills = studentSkills; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
