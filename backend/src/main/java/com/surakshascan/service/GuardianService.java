package com.surakshascan.service;

import com.surakshascan.dao.FamilyLinkDAO;
import com.surakshascan.dao.GuardianAlertDAO;
import com.surakshascan.model.FamilyLink;
import com.surakshascan.model.GuardianAlert;
import java.util.List;

public class GuardianService {
    private FamilyLinkDAO familyLinkDAO = new FamilyLinkDAO();
    private GuardianAlertDAO alertDAO = new GuardianAlertDAO();

    public void createAlertsForDependent(int dependentId, int reportId, int riskScore, String verdict) {
        if (!"HIGH RISK".equalsIgnoreCase(verdict) && !"HIGH_RISK".equalsIgnoreCase(verdict)) {
            return;
        }

        List<FamilyLink> activeLinks = familyLinkDAO.findActiveForDependent(dependentId);
        for (FamilyLink link : activeLinks) {
            GuardianAlert alert = new GuardianAlert();
            alert.setLinkId(link.getLinkId());
            alert.setReportId(reportId);
            alert.setGuardianId(link.getGuardianId());
            alert.setDependentId(dependentId);
            alert.setAlertType("HIGH_RISK_SCAN");
            alert.setTitle("High-risk scan detected");
            alert.setMessage("An item detected on a linked account matched multiple scam indicators.");
            alert.setRiskScore(riskScore);
            alert.setVerdict(verdict);
            
            // Duplicate prevention constraint will handle existing alerts, or we could check here.
            alertDAO.createAlert(alert);
        }
    }
}
