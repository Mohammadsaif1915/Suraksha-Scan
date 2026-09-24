package com.surakshascan.model;

public class FamilyLink {
    private int linkId;
    private int guardianId;
    private int dependentId;
    private String status;
    
    private User guardian;
    private User dependent;

    public int getLinkId() { return linkId; }
    public void setLinkId(int linkId) { this.linkId = linkId; }
    public int getGuardianId() { return guardianId; }
    public void setGuardianId(int guardianId) { this.guardianId = guardianId; }
    public int getDependentId() { return dependentId; }
    public void setDependentId(int dependentId) { this.dependentId = dependentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public User getGuardian() { return guardian; }
    public void setGuardian(User guardian) { this.guardian = guardian; }
    public User getDependent() { return dependent; }
    public void setDependent(User dependent) { this.dependent = dependent; }
}
