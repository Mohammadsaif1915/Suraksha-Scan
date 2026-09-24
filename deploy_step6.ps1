$base = "c:\Users\Administrator\Desktop\SurakshaScan"

# 1. Models
$reportModelPath = "$base\backend\src\main\java\com\surakshascan\model\CommunityReport.java"
$reportModelContent = @"
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
"@
Set-Content -Path $reportModelPath -Value $reportModelContent -Encoding UTF8

# 2. InputNormalizer
$normPath = "$base\backend\src\main\java\com\surakshascan\util\InputNormalizer.java"
$normContent = @"
package com.surakshascan.util;
import java.net.URI;
public class InputNormalizer {
    public static String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase().replaceAll("\\s+", " ");
    }
    public static String extractDomain(String urlStr) {
        if (urlStr == null) return "";
        urlStr = urlStr.trim().toLowerCase();
        try {
            if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                urlStr = "http://" + urlStr;
            }
            URI uri = new URI(urlStr);
            String host = uri.getHost();
            return host != null ? host.toLowerCase() : urlStr;
        } catch(Exception e) {
            return urlStr;
        }
    }
    public static String normalizeUpi(String upi) {
        if (upi == null) return "";
        return upi.trim().toLowerCase();
    }
    public static String normalizePhone(String phone) {
        if (phone == null) return "";
        return phone.trim().replaceAll("[\\s\\-\\(\\)]", "");
    }
}
"@
Set-Content -Path $normPath -Value $normContent -Encoding UTF8

# 3. CommunityFlagDAO
$flagDaoPath = "$base\backend\src\main\java\com\surakshascan\dao\CommunityFlagDAO.java"
$flagDaoContent = @"
package com.surakshascan.dao;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import java.sql.*;

public class CommunityFlagDAO {
    public CommunityFlag findByFlaggedValue(String value) {
        String sql = "SELECT * FROM community_flags WHERE flagged_value = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractFlag(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public CommunityFlag findByFlaggedValue(Connection conn, String value) throws SQLException {
        String sql = "SELECT * FROM community_flags WHERE flagged_value = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return extractFlag(rs);
            }
        }
        return null;
    }
    
    public void incrementOrInsertFlag(Connection conn, String value) throws SQLException {
        CommunityFlag flag = findByFlaggedValue(conn, value);
        if (flag != null) {
            String sql = "UPDATE community_flags SET report_count = report_count + 1, last_reported = CURRENT_TIMESTAMP WHERE flagged_value = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, value);
                stmt.executeUpdate();
            }
        } else {
            String sql = "INSERT INTO community_flags (flagged_value, report_count, first_reported, last_reported) VALUES (?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, value);
                stmt.executeUpdate();
            }
        }
    }

    private CommunityFlag extractFlag(ResultSet rs) throws SQLException {
        CommunityFlag flag = new CommunityFlag();
        flag.setFlagId(rs.getInt("flag_id"));
        flag.setFlaggedValue(rs.getString("flagged_value"));
        flag.setReportCount(rs.getInt("report_count"));
        flag.setFirstReported(rs.getTimestamp("first_reported"));
        flag.setLastReported(rs.getTimestamp("last_reported"));
        return flag;
    }
}
"@
Set-Content -Path $flagDaoPath -Value $flagDaoContent -Encoding UTF8

# 4. CommunityReportDAO
$crDaoPath = "$base\backend\src\main\java\com\surakshascan\dao\CommunityReportDAO.java"
$crDaoContent = @"
package com.surakshascan.dao;
import java.sql.*;
public class CommunityReportDAO {
    public boolean hasUserReported(Connection conn, int userId, String flaggedValue, String type) throws SQLException {
        String sql = "SELECT 1 FROM community_reports WHERE user_id = ? AND flagged_value = ? AND input_type = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, flaggedValue);
            stmt.setString(3, type);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
    public void createReport(Connection conn, int userId, String flaggedValue, String type) throws SQLException {
        String sql = "INSERT INTO community_reports (user_id, flagged_value, input_type) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, flaggedValue);
            stmt.setString(3, type);
            stmt.executeUpdate();
        }
    }
}
"@
Set-Content -Path $crDaoPath -Value $crDaoContent -Encoding UTF8

# 5. CommunityReportService
$crServicePath = "$base\backend\src\main\java\com\surakshascan\service\CommunityReportService.java"
$crServiceContent = @"
package com.surakshascan.service;
import com.google.gson.JsonObject;
import com.surakshascan.dao.CommunityFlagDAO;
import com.surakshascan.dao.CommunityReportDAO;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.util.DatabaseConnection;
import com.surakshascan.util.InputNormalizer;
import java.sql.Connection;
import java.sql.SQLException;

public class CommunityReportService {
    private CommunityFlagDAO flagDAO = new CommunityFlagDAO();
    private CommunityReportDAO reportDAO = new CommunityReportDAO();

    public JsonObject reportItem(int userId, String type, String value) throws Exception {
        if (type == null || value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Type and value are required.");
        }
        
        type = type.trim().toLowerCase();
        if (!type.equals("phone") && !type.equals("upi") && !type.equals("domain")) {
            throw new IllegalArgumentException("Invalid type. Allowed: phone, upi, domain.");
        }
        if (value.length() > 255) throw new IllegalArgumentException("Value too long.");

        String normalizedValue = "";
        if (type.equals("domain")) normalizedValue = InputNormalizer.extractDomain(value);
        else if (type.equals("upi")) normalizedValue = InputNormalizer.normalizeUpi(value);
        else if (type.equals("phone")) normalizedValue = InputNormalizer.normalizePhone(value);

        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Invalid input value.");
        }

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            if (reportDAO.hasUserReported(conn, userId, normalizedValue, type)) {
                conn.rollback();
                JsonObject err = new JsonObject();
                err.addProperty("success", false);
                err.addProperty("status", 409);
                err.addProperty("message", "You have already reported this item");
                return err;
            }
            
            reportDAO.createReport(conn, userId, normalizedValue, type);
            flagDAO.incrementOrInsertFlag(conn, normalizedValue);
            
            CommunityFlag updatedFlag = flagDAO.findByFlaggedValue(conn, normalizedValue);
            conn.commit();
            
            JsonObject communityData = new JsonObject();
            communityData.addProperty("type", type);
            communityData.addProperty("reportCount", updatedFlag.getReportCount());
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("status", 201);
            result.addProperty("message", "Report submitted successfully");
            result.add("community", communityData);
            return result;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    public JsonObject checkItem(String type, String value) {
        if (type == null || value == null) return createNotFound();
        type = type.trim().toLowerCase();
        
        String normalizedValue = "";
        if (type.equals("domain")) normalizedValue = InputNormalizer.extractDomain(value);
        else if (type.equals("upi")) normalizedValue = InputNormalizer.normalizeUpi(value);
        else if (type.equals("phone")) normalizedValue = InputNormalizer.normalizePhone(value);
        
        CommunityFlag flag = flagDAO.findByFlaggedValue(normalizedValue);
        if (flag != null) {
            JsonObject communityData = new JsonObject();
            communityData.addProperty("type", type);
            communityData.addProperty("reportCount", flag.getReportCount());
            communityData.addProperty("firstReported", flag.getFirstReported().toString());
            communityData.addProperty("lastReported", flag.getLastReported().toString());
            
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("found", true);
            result.add("community", communityData);
            return result;
        }
        return createNotFound();
    }
    
    private JsonObject createNotFound() {
        JsonObject result = new JsonObject();
        result.addProperty("success", true);
        result.addProperty("found", false);
        return result;
    }
}
"@
Set-Content -Path $crServicePath -Value $crServiceContent -Encoding UTF8

# 6. CommunityServlet
$cServletPath = "$base\backend\src\main\java\com\surakshascan\controller\CommunityServlet.java"
$cServletContent = @"
package com.surakshascan.controller;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.surakshascan.service.CommunityReportService;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/community/*")
public class CommunityServlet extends HttpServlet {
    private CommunityReportService service = new CommunityReportService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
            return;
        }

        String pathInfo = req.getPathInfo();
        if ("/report".equals(pathInfo)) {
            int userId = (Integer) session.getAttribute("userId");
            try {
                JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
                if (json == null) throw new JsonSyntaxException("Empty body");
                String type = json.has("type") ? json.get("type").getAsString() : null;
                String value = json.has("value") ? json.get("value").getAsString() : null;
                
                JsonObject result = service.reportItem(userId, type, value);
                resp.setStatus(result.get("status").getAsInt());
                result.remove("status"); // don't send status in body
                resp.getWriter().write(gson.toJson(result));
                
            } catch (IllegalArgumentException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\":false,\"message\":\"Internal Server Error\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Not found\"}");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        
        if ("/check".equals(pathInfo)) {
            String type = req.getParameter("type");
            String value = req.getParameter("value");
            JsonObject result = service.checkItem(type, value);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(result));
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Not found\"}");
        }
    }
}
"@
Set-Content -Path $cServletPath -Value $cServletContent -Encoding UTF8

# 7. RiskThresholds Update
$rtPath = "$base\backend\src\main\java\com\surakshascan\util\RiskThresholds.java"
$rtContent = @"
package com.surakshascan.util;
public class RiskThresholds {
    public static final int SUSPICIOUS_MIN = 20;
    public static final int HIGH_RISK_MIN = 50;
    public static final int MAX_COMMUNITY_CONTRIBUTION = 40;
    public static final int COMMUNITY_FLAG_WEIGHT = 20;
    public static final int MAX_INPUT_LENGTH = 1000;
}
"@
Set-Content -Path $rtPath -Value $rtContent -Encoding UTF8

# 8. RuleEngine Update
$rePath = "$base\backend\src\main\java\com\surakshascan\service\RuleEngine.java"
$reContent = @"
package com.surakshascan.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surakshascan.dao.CommunityFlagDAO;
import com.surakshascan.dao.ScamPatternDAO;
import com.surakshascan.model.CommunityFlag;
import com.surakshascan.model.ScamPattern;
import com.surakshascan.util.InputNormalizer;
import com.surakshascan.util.RiskThresholds;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Set;

public class RuleEngine {
    private ScamPatternDAO scamPatternDAO = new ScamPatternDAO();
    private CommunityFlagDAO communityFlagDAO = new CommunityFlagDAO();

    public JsonObject evaluate(String inputType, String rawInput) {
        String normalizedInput = InputNormalizer.normalize(rawInput);
        List<ScamPattern> patterns = scamPatternDAO.findByCategory(inputType);
        
        int totalScore = 0;
        JsonArray matchedRulesArray = new JsonArray();
        
        for (ScamPattern pattern : patterns) {
            boolean matched = false;
            if ("regex".equalsIgnoreCase(pattern.getPatternType())) {
                try {
                    Pattern p = Pattern.compile(pattern.getPatternText(), Pattern.CASE_INSENSITIVE);
                    if (p.matcher(normalizedInput).find()) {
                        matched = true;
                    }
                } catch (Exception e) {}
            } else if ("keyword".equalsIgnoreCase(pattern.getPatternType())) {
                if (normalizedInput.contains(pattern.getPatternText().toLowerCase())) {
                    matched = true;
                }
            }
            if (matched) {
                totalScore += pattern.getRiskWeight();
                JsonObject matchObj = new JsonObject();
                matchObj.addProperty("category", pattern.getCategory());
                matchObj.addProperty("description", pattern.getDescription());
                matchObj.addProperty("scoreContribution", pattern.getRiskWeight());
                matchedRulesArray.add(matchObj);
            }
        }
        
        Set<String> identifiersToCheck = new HashSet<>();
        if ("link".equalsIgnoreCase(inputType)) {
            identifiersToCheck.add(InputNormalizer.extractDomain(rawInput));
        } else if ("upi".equalsIgnoreCase(inputType)) {
            identifiersToCheck.add(InputNormalizer.normalizeUpi(rawInput));
        } else if ("sms".equalsIgnoreCase(inputType)) {
            // Extract UPIs
            Matcher mUpi = Pattern.compile("[a-zA-Z0-9.\\-_]+@[a-zA-Z]+").matcher(rawInput);
            while (mUpi.find()) identifiersToCheck.add(InputNormalizer.normalizeUpi(mUpi.group()));
            // Extract Domains
            Matcher mDom = Pattern.compile("https?://([a-zA-Z0-9.-]+)").matcher(rawInput);
            while (mDom.find()) identifiersToCheck.add(InputNormalizer.extractDomain(mDom.group()));
            // Extract Phone (simple)
            Matcher mPhone = Pattern.compile("\\+?[0-9]{10,13}").matcher(InputNormalizer.normalizePhone(rawInput));
            while (mPhone.find()) identifiersToCheck.add(mPhone.group());
        }
        
        for (String idToCheck : identifiersToCheck) {
            CommunityFlag flag = communityFlagDAO.findByFlaggedValue(idToCheck);
            if (flag != null && flag.getReportCount() > 0) {
                int count = flag.getReportCount();
                int communityScore = 0;
                if (count >= 10) communityScore = 40;
                else if (count >= 5) communityScore = 30;
                else if (count >= 3) communityScore = 20;
                else if (count >= 1) communityScore = 10;
                
                totalScore += communityScore;
                
                JsonObject communityObj = new JsonObject();
                communityObj.addProperty("category", "community");
                communityObj.addProperty("description", "Community reports: this identifier has been reported by " + count + " user(s).");
                communityObj.addProperty("scoreContribution", communityScore);
                matchedRulesArray.add(communityObj);
                break; // Limit to one community flag application per scan to avoid score explosion
            }
        }
        
        String verdict;
        if (totalScore >= RiskThresholds.HIGH_RISK_MIN) verdict = "HIGH RISK";
        else if (totalScore >= RiskThresholds.SUSPICIOUS_MIN) verdict = "SUSPICIOUS";
        else verdict = "SAFE";
        
        String summary = verdict.equals("SAFE") ? 
            "No known scam indicators were detected." : 
            "This input contains multiple indicators commonly associated with scams.";
            
        JsonObject result = new JsonObject();
        result.addProperty("inputType", inputType);
        result.addProperty("verdict", verdict);
        result.addProperty("riskScore", totalScore);
        result.addProperty("summary", summary);
        result.add("matchedRules", matchedRulesArray);
        
        return result;
    }
}
"@
Set-Content -Path $rePath -Value $reContent -Encoding UTF8

# 9. Schema Update
$schemaSqlPath = "$base\database\schema.sql"
$indexSql = @"

-- Step 6 Community Reports
CREATE TABLE IF NOT EXISTS community_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    flagged_value VARCHAR(255) NOT NULL,
    input_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE INDEX idx_community_reports_user_flag (user_id, flagged_value, input_type)
);
"@
Add-Content -Path $schemaSqlPath -Value $indexSql

# 10. Dashboard UI Navigation Update
$dashHtmlPath = "$base\frontend\dashboard.html"
$dashHtmlContent = Get-Content $dashHtmlPath -Raw
$dashHtmlContent = $dashHtmlContent -replace '<a href="history.html">History</a>', '<a href="history.html">History</a><a href="community.html">Community</a>'
Set-Content -Path $dashHtmlPath -Value $dashHtmlContent -Encoding UTF8

# 11. Scanner UI Update (scan.html)
$scanHtmlPath = "$base\frontend\scan.html"
$scanHtmlContent = Get-Content $scanHtmlPath -Raw
$scanHtmlContent = $scanHtmlContent -replace '<a href="history.html" style="text-decoration: underline;">History</a>', '<a href="history.html">History</a><a href="community.html">Community</a>'
# Let's insert the report action dynamically in JS instead of HTML manipulation to be safe
Set-Content -Path $scanHtmlPath -Value $scanHtmlContent -Encoding UTF8

# 12. Scanner JS Update
$scanJsPath = "$base\frontend\js\scanner.js"
$scanJsContent = Get-Content $scanJsPath -Raw
$scanJsContent = $scanJsContent -replace 'resultCard.style.display = ''block'';', @"
        // Add Community Report Option
        let reportHtml = '';
        if (scanData.verdict !== 'SAFE') {
            reportHtml = \`
                <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;">
                    <a href="community.html?value=\${encodeURIComponent(scanData.inputType === 'sms' ? '' : inputTitle.innerText.includes('UPI') ? document.getElementById('scanContentUpi').value : document.getElementById('scanContentLink').value)}&type=\${scanData.inputType}" style="display:inline-block; background:#6c757d; color:#fff; padding:8px 16px; text-decoration:none; border-radius:4px; font-weight:bold; font-size:14px;">Report this \${scanData.inputType}</a>
                </div>
            \`;
        }
        
        let reportDiv = document.getElementById('reportActionDiv');
        if (!reportDiv) {
            reportDiv = document.createElement('div');
            reportDiv.id = 'reportActionDiv';
            resultCard.appendChild(reportDiv);
        }
        reportDiv.innerHTML = reportHtml;

        resultCard.style.display = 'block';
"@
Set-Content -Path $scanJsPath -Value $scanJsContent -Encoding UTF8

# 13. community.html
$commHtmlPath = "$base\frontend\community.html"
$commHtmlContent = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Community Reporting - SurakshaScan</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f4f7f6; margin: 0; display: none; }
        header { background: #002b5e; color: #fff; padding: 15px 20px; display: flex; justify-content: space-between; align-items: center; }
        header h1 { margin: 0; font-size: 22px; }
        nav a { color: #fff; margin-right: 15px; text-decoration: none; font-size: 15px; }
        nav a:hover { text-decoration: underline; }
        main { padding: 20px; max-width: 600px; margin: 0 auto; }
        
        .report-card { background: #fff; padding: 25px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .report-card h2 { margin-top: 0; color: #333; }
        .info-box { background: #e9ecef; color: #495057; padding: 15px; border-left: 4px solid #6c757d; border-radius: 4px; font-size: 14px; margin-bottom: 20px; }
        
        .form-group { margin-bottom: 15px; }
        label { display: block; font-weight: bold; margin-bottom: 5px; color: #555; }
        select, input[type="text"] { width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 4px; font-size: 16px; box-sizing: border-box; }
        
        button.submit-btn { width: 100%; padding: 12px; background: #dc3545; color: #fff; border: none; border-radius: 4px; font-size: 16px; font-weight: bold; cursor: pointer; margin-top: 10px;}
        button.submit-btn:disabled { background: #999; cursor: not-allowed; }
        
        .msg-box { margin-top: 15px; padding: 12px; border-radius: 4px; display: none; font-weight: bold; text-align: center; }
        .msg-success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .msg-error { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }
    </style>
</head>
<body id="pageBody">
    <header>
        <div style="display:flex; align-items: center;">
            <h1>SurakshaScan</h1>
            <nav style="margin-left: 30px;">
                <a href="dashboard.html">Dashboard</a>
                <a href="scan.html">Scanner</a>
                <a href="history.html">History</a>
                <a href="community.html" style="text-decoration: underline;">Community</a>
            </nav>
        </div>
    </header>

    <main>
        <div class="report-card">
            <h2>Report Suspicious Item</h2>
            <div class="info-box">
                Community reports help identify repeatedly reported scam indicators. A report is an indicator and does not by itself prove wrongdoing.
            </div>
            
            <div id="msgBox" class="msg-box"></div>
            
            <div class="form-group">
                <label>Item Type</label>
                <select id="reportType">
                    <option value="phone">Phone Number</option>
                    <option value="upi">UPI ID</option>
                    <option value="domain">Domain / Link</option>
                </select>
            </div>
            
            <div class="form-group">
                <label>Suspicious Identifier</label>
                <input type="text" id="reportValue" placeholder="Enter phone, UPI, or URL..." maxlength="255">
            </div>
            
            <button id="submitBtn" class="submit-btn">Submit Report</button>
        </div>
    </main>

    <script src="js/api.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            checkAuthAndInit(() => {
                document.getElementById('pageBody').style.display = 'block';
                
                // Prefill if query params exist
                const params = new URLSearchParams(window.location.search);
                if (params.get('type')) {
                    const type = params.get('type') === 'link' ? 'domain' : (params.get('type') === 'sms' ? 'phone' : params.get('type'));
                    document.getElementById('reportType').value = type;
                }
                if (params.get('value')) {
                    document.getElementById('reportValue').value = params.get('value');
                }
            });

            document.getElementById('submitBtn').addEventListener('click', async () => {
                const type = document.getElementById('reportType').value;
                const value = document.getElementById('reportValue').value.trim();
                const msgBox = document.getElementById('msgBox');
                const btn = document.getElementById('submitBtn');
                
                if (!value) {
                    showMsg('Please enter a valid phone number, UPI ID, or domain.', false);
                    return;
                }
                
                btn.disabled = true;
                btn.innerText = 'Submitting...';
                
                try {
                    const data = await authenticatedFetch('/api/community/report', {
                        method: 'POST',
                        body: { type, value }
                    });
                    
                    if (data && data.success) {
                        let msg = 'Report submitted successfully.';
                        if (data.community && data.community.reportCount) {
                            msg += `<br><span style="font-size:13px; color:#555;">Community reports: \${data.community.reportCount}</span>`;
                        }
                        showMsg(msg, true);
                        document.getElementById('reportValue').value = '';
                    } else if (data) {
                        showMsg(data.message || 'Error submitting report.', false);
                    }
                } catch (e) {
                    showMsg('Network error occurred.', false);
                } finally {
                    btn.disabled = false;
                    btn.innerText = 'Submit Report';
                }
            });
            
            function showMsg(html, isSuccess) {
                const msgBox = document.getElementById('msgBox');
                msgBox.innerHTML = html;
                msgBox.className = 'msg-box ' + (isSuccess ? 'msg-success' : 'msg-error');
                msgBox.style.display = 'block';
            }
        });
    </script>
</body>
</html>
"@
Set-Content -Path $commHtmlPath -Value $commHtmlContent -Encoding UTF8

# 14. README
$readmePath = "$base\README.md"
$readmeContent = Get-Content $readmePath -Raw
$readmeContent += @"

## Community Intelligence System (Step 6)
An authenticated, isolated, duplicate-preventing community flag system.

### Details
* `POST /api/community/report`: Securely logs a user report. Checks `community_reports` transactionally to strictly prevent duplicate spamming from the same user.
* **Normalization**: All entries (phone, upi, domain) are normalized (e.g. domains extracted from URLs, spacing removed from phones) to prevent fragmenting flags.
* **Scoring Rules**: The `RuleEngine` queries Community Flags. The community scoring contribution is explicitly capped (`10, 20, 30, 40 max`) to prevent report counts from unfairly inflating scores to infinity.
* **Data Privacy**: Reporter identity is never exposed. The system only provides aggregated stats (`report_count`).
* **Disclaimer**: Clearly indicates that "Community reports are indicators and are not independently verified proof of fraud."
"@
Set-Content -Path $readmePath -Value $readmeContent -Encoding UTF8
