package com.surakshascan.model;
import java.sql.Timestamp;
public class CommunityReport {
    private int reportId;
    private int userId;
    private String flaggedValue;
    private String inputType;
    private Timestamp createdAt;

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFlaggedValue() { return flaggedValue; }
    public void setFlaggedValue(String flaggedValue) { this.flaggedValue = flaggedValue; }
    public String getInputType() { return inputType; }
    public void setInputType(String inputType) { this.inputType = inputType; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
