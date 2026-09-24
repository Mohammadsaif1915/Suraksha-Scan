package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.surakshascan.dao.UserReportDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/dashboard/stats")
public class DashboardServlet extends HttpServlet {
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

        int totalScans = userReportDAO.countTotalByUserId(userId);
        JsonObject verdictCounts = userReportDAO.countByVerdict(userId);

        JsonObject stats = new JsonObject();
        stats.addProperty("totalScans", totalScans);
        stats.addProperty("safe", verdictCounts.has("safe") ? verdictCounts.get("safe").getAsInt() : 0);
        stats.addProperty("suspicious", verdictCounts.has("suspicious") ? verdictCounts.get("suspicious").getAsInt() : 0);
        stats.addProperty("highRisk", verdictCounts.has("highRisk") ? verdictCounts.get("highRisk").getAsInt() : 0);

        JsonObject response = new JsonObject();
        response.addProperty("success", true);
        response.add("stats", stats);

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(gson.toJson(response));
    }
}
