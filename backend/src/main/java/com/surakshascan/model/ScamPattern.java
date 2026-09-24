package com.surakshascan.model;

public class ScamPattern {
    private int patternId;
    private String category;
    private String patternText;
    private String patternType;
    private int riskWeight;
    private String description;
    private boolean active = true;

    public int getPatternId() { return patternId; }
    public void setPatternId(int patternId) { this.patternId = patternId; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getPatternText() { return patternText; }
    public void setPatternText(String patternText) { this.patternText = patternText; }
    public String getPatternType() { return patternType; }
    public void setPatternType(String patternType) { this.patternType = patternType; }
    public int getRiskWeight() { return riskWeight; }
    public void setRiskWeight(int riskWeight) { this.riskWeight = riskWeight; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
