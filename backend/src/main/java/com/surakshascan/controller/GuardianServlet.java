package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.surakshascan.dao.FamilyLinkDAO;
import com.surakshascan.dao.GuardianAlertDAO;
import com.surakshascan.dao.UserDAO;
import com.surakshascan.model.FamilyLink;
import com.surakshascan.model.GuardianAlert;
import com.surakshascan.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/guardian/*")
public class GuardianServlet extends HttpServlet {
    private FamilyLinkDAO linkDAO = new FamilyLinkDAO();
    private GuardianAlertDAO alertDAO = new GuardianAlertDAO();
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String pathInfo = req.getPathInfo();

        try {
            if ("/link/request".equals(pathInfo)) {
                if (!"guardian".equals(role)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Only guardians can create link requests.");
                    return;
                }
                JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
                String dependentEmail = json.has("dependentEmail") ? json.get("dependentEmail").getAsString().trim().toLowerCase() : "";
                
                User dependent = userDAO.findByEmail(dependentEmail);
                if (dependent == null) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Dependent user not found.");
                    return;
                }
                if (dependent.getUserId() == userId) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot link to yourself.");
                    return;
                }
                
                FamilyLink existing = linkDAO.findByUsers(userId, dependent.getUserId());
                if (existing != null) {
                    sendError(resp, HttpServletResponse.SC_CONFLICT, "Relationship already exists or is pending.");
                    return;
                }
                
                if (linkDAO.createRequest(userId, dependent.getUserId())) {
                    sendSuccess(resp, HttpServletResponse.SC_CREATED, "Family link request sent");
                } else {
                    sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create request");
                }
            } else if (pathInfo != null && pathInfo.matches("/link/\\d+/accept")) {
                int linkId = Integer.parseInt(pathInfo.split("/")[2]);
                FamilyLink link = linkDAO.findById(linkId);
                if (link == null || link.getDependentId() != userId || !"pending".equals(link.getStatus())) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Unauthorized to accept this request");
                    return;
                }
                linkDAO.updateStatus(linkId, "active");
                sendSuccess(resp, HttpServletResponse.SC_OK, "Family link activated");
            } else if (pathInfo != null && pathInfo.matches("/link/\\d+/reject")) {
                int linkId = Integer.parseInt(pathInfo.split("/")[2]);
                FamilyLink link = linkDAO.findById(linkId);
                if (link == null || link.getDependentId() != userId || !"pending".equals(link.getStatus())) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Unauthorized to reject this request");
                    return;
                }
                linkDAO.deleteLink(linkId); // Just remove it to keep it simple as per spec
                sendSuccess(resp, HttpServletResponse.SC_OK, "Family link request rejected");
            } else if (pathInfo != null && pathInfo.matches("/alerts/\\d+/read")) {
                if (!"guardian".equals(role)) {
                    sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Only guardians can mark alerts as read.");
                    return;
                }
                int alertId = Integer.parseInt(pathInfo.split("/")[2]);
                if (alertDAO.markAsRead(alertId, userId)) {
                    sendSuccess(resp, HttpServletResponse.SC_OK, "Alert marked as read");
                } else {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Alert not found or already read");
                }
            } else {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String pathInfo = req.getPathInfo();

        if ("/requests".equals(pathInfo)) {
            // Dependent sees pending requests
            List<FamilyLink> requests = linkDAO.findPendingForDependent(userId);
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("requests", gson.toJsonTree(requests));
            resp.getWriter().write(gson.toJson(res));
        } else if ("/links".equals(pathInfo)) {
            List<FamilyLink> links;
            if ("guardian".equals(role)) {
                links = linkDAO.findActiveForGuardian(userId);
            } else {
                links = linkDAO.findActiveForDependent(userId);
            }
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("links", gson.toJsonTree(links));
            resp.getWriter().write(gson.toJson(res));
        } else if ("/alerts".equals(pathInfo)) {
            if (!"guardian".equals(role)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Only guardians can view alerts.");
                return;
            }
            List<GuardianAlert> alerts = alertDAO.findByGuardianId(userId);
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("alerts", gson.toJsonTree(alerts));
            resp.getWriter().write(gson.toJson(res));
        } else if ("/alerts/unread-count".equals(pathInfo)) {
            if (!"guardian".equals(role)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return;
            }
            int count = alertDAO.countUnread(userId);
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.addProperty("unreadCount", count);
            resp.getWriter().write(gson.toJson(res));
        } else if (pathInfo != null && pathInfo.matches("/alerts/\\d+")) {
            if (!"guardian".equals(role)) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Unauthorized");
                return;
            }
            int alertId = Integer.parseInt(pathInfo.split("/")[2]);
            GuardianAlert a = alertDAO.findByIdForGuardian(alertId, userId);
            if (a == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Alert not found");
                return;
            }
            JsonObject res = new JsonObject();
            res.addProperty("success", true);
            res.add("alert", gson.toJsonTree(a));
            resp.getWriter().write(gson.toJson(res));
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Authentication required");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.matches("/link/\\d+")) {
            int linkId = Integer.parseInt(pathInfo.split("/")[2]);
            FamilyLink link = linkDAO.findById(linkId);
            if (link == null) {
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Link not found");
                return;
            }
            if (link.getGuardianId() != userId && link.getDependentId() != userId) {
                sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Unauthorized to delete this link");
                return;
            }
            linkDAO.deleteLink(linkId);
            sendSuccess(resp, HttpServletResponse.SC_OK, "Link removed");
        } else {
            sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Not found");
        }
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject json = new JsonObject();
        json.addProperty("success", false);
        json.addProperty("message", message);
        resp.getWriter().write(gson.toJson(json));
    }

    private void sendSuccess(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        JsonObject json = new JsonObject();
        json.addProperty("success", true);
        json.addProperty("message", message);
        resp.getWriter().write(gson.toJson(json));
    }
}
