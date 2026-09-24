package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surakshascan.dao.UserReportDAO;
import com.surakshascan.model.UserReport;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import com.google.gson.JsonParser;

@WebServlet("/api/history/*")
public class HistoryServlet extends HttpServlet {
    private UserReportDAO userReportDAO = new UserReportDAO();
    private Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/history
            handleGetList(req, resp, userId);
        } else {
            // GET /api/history/{id}
            handleGetDetails(req, resp, userId, pathInfo);
        }
    }

    private void handleGetList(HttpServletRequest req, HttpServletResponse resp, int userId) throws IOException {
        int page = 1;
        int limit = 20;

        try {
            if (req.getParameter("page") != null) page = Integer.parseInt(req.getParameter("page"));
            if (req.getParameter("limit") != null) limit = Integer.parseInt(req.getParameter("limit"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid pagination parameters\"}");
            return;
        }

        if (page < 1) page = 1;
        if (limit < 1) limit = 20;
        if (limit > 50) limit = 50; // max limit

        int offset = (page - 1) * limit;
        List<UserReport> reports = userReportDAO.findByUserId(userId, limit, offset);
        int totalRecords = userReportDAO.countTotalByUserId(userId);
        int totalPages = (int) Math.ceil((double) totalRecords / limit);

        JsonArray historyArray = new JsonArray();
        for (UserReport r : reports) {
            JsonObject obj = new JsonObject();
            obj.addProperty("reportId", r.getReportId());
            obj.addProperty("inputType", r.getInputType());
            obj.addProperty("verdict", r.getVerdict());
            obj.addProperty("riskScore", r.getRiskScore());
            obj.addProperty("createdAt", r.getCreatedAt().toString());
            historyArray.add(obj);
        }

        JsonObject pagination = new JsonObject();
        pagination.addProperty("page", page);
        pagination.addProperty("limit", limit);
        pagination.addProperty("totalRecords", totalRecords);
        pagination.addProperty("totalPages", totalPages);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("history", historyArray);
        response.add("pagination", pagination);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(response));
    }

    private void handleGetDetails(HttpServletRequest req, HttpServletResponse resp, int userId, String pathInfo) throws IOException {
        try {
            int reportId = Integer.parseInt(pathInfo.substring(1));
            UserReport report = userReportDAO.findByIdAndUserId(reportId, userId);
            
            if (report == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"success\":false,\"message\":\"Scan not found\"}");
                return;
            }

            JsonObject scanObj = new JsonObject();
            scanObj.addProperty("reportId", report.getReportId());
            scanObj.addProperty("inputType", report.getInputType());
            scanObj.addProperty("rawInput", report.getRawInput());
            scanObj.addProperty("verdict", report.getVerdict());
            scanObj.addProperty("riskScore", report.getRiskScore());
            scanObj.addProperty("createdAt", report.getCreatedAt().toString());
            
            try {
                JsonArray rulesArr = JsonParser.parseString(report.getMatchedRules()).getAsJsonArray();
                scanObj.add("matchedRules", rulesArr);
            } catch (Exception e) {
                scanObj.add("matchedRules", new JsonArray());
            }

            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.add("scan", scanObj);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(response));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid report ID\"}");
        }
    }
}
