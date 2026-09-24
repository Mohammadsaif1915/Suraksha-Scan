package com.surakshascan.model;
import java.sql.Timestamp;

public class GuardianAlert {
    private int alertId;
    private int linkId;
    private int reportId;
    private int guardianId;
    private int dependentId;
    private String alertType;
    private String title;
    private String message;
    private int riskScore;
    private String verdict;
    private boolean isRead;
    private Timestamp createdAt;
    
    private User dependent;

    public int getAlertId() { return alertId; }
    public void setAlertId(int alertId) { this.alertId = alertId; }
    public int getLinkId() { return linkId; }
    public void setLinkId(int linkId) { this.linkId = linkId; }
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }
    public int getDependentId() { return dependentId; }
    public void setDependentId(int dependentId) { this.dependentId = dependentId; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public User getDependent() { return dependent; }
    public void setDependent(User dependent) { this.dependent = dependent; }
}
