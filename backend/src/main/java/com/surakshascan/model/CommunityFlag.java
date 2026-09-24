package com.surakshascan.model;
import java.sql.Timestamp;

public class CommunityFlag {
    private int flagId;
    private String flaggedValue;
    private int reportCount;
    private Timestamp firstReported;
    private Timestamp lastReported;
    private String status = "active";

    public int getFlagId() { return flagId; }
    public void setFlagId(int flagId) { this.flagId = flagId; }
    public String getFlaggedValue() { return flaggedValue; }
    public void setFlaggedValue(String flaggedValue) { this.flaggedValue = flaggedValue; }
    public int getReportCount() { return reportCount; }
    public void setReportCount(int reportCount) { this.reportCount = reportCount; }
    public Timestamp getFirstReported() { return firstReported; }
    public void setFirstReported(Timestamp firstReported) { this.firstReported = firstReported; }
    public Timestamp getLastReported() { return lastReported; }
    public void setLastReported(Timestamp lastReported) { this.lastReported = lastReported; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
