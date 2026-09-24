package com.surakshascan.controller;

import com.google.gson.*;
import com.surakshascan.dao.*;
import com.surakshascan.model.*;
import com.surakshascan.service.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private final AdminStatsDAO      statsDAO      = new AdminStatsDAO();
    private final AdminPatternService patternSvc   = new AdminPatternService();
    private final AdminCommunityDAO  communityDAO  = new AdminCommunityDAO();
    private final AdminCommunityService commSvc    = new AdminCommunityService();
    private final AdminUserDAO       userDAO       = new AdminUserDAO();
    private final AdminUserService   userSvc       = new AdminUserService();
    private final AdminScanDAO       scanDAO       = new AdminScanDAO();
    private final AdminAuditLogDAO   auditDAO      = new AdminAuditLogDAO();
    private final Gson gson = new Gson();

    // ── Auth guard ──────────────────────────────────────────────────────────────
    private boolean guardAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("userId") == null) {
            resp.setStatus(401);
            resp.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
            return false;
        }
        if (!"admin".equals(s.getAttribute("role"))) {
            resp.setStatus(403);
            resp.getWriter().write("{\"success\":false,\"message\":\"Admin access required\"}");
            return false;
        }
        return true;
    }

    private int uid(HttpServletRequest req) {
        return (Integer) req.getSession(false).getAttribute("userId");
    }

    private int parsePage(HttpServletRequest req) {
        try { int p = Integer.parseInt(req.getParameter("page")); return p < 1 ? 1 : p; }
        catch (Exception e) { return 1; }
    }
    private int parseLimit(HttpServletRequest req) {
        try { int l = Integer.parseInt(req.getParameter("limit")); return l < 1 ? 20 : Math.min(l, 100); }
        catch (Exception e) { return 20; }
    }

    // ── GET ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!guardAdmin(req, resp)) return;
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();

        try {
            if ("/stats".equals(path)) {
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("stats", statsDAO.getStats());
                write(resp, 200, res);

            } else if ("/patterns".equals(path)) {
                List<ScamPattern> patterns = patternSvc.getAllPatterns();
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("patterns", gson.toJsonTree(patterns));
                write(resp, 200, res);

            } else if ("/community".equals(path)) {
                int page = parsePage(req), limit = parseLimit(req), offset = (page-1)*limit;
                List<CommunityFlag> flags = communityDAO.findAll(limit, offset);
                int total = communityDAO.countAll();
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("items", gson.toJsonTree(flags));
                res.add("pagination", pagination(page, limit, total));
                write(resp, 200, res);

            } else if ("/users".equals(path)) {
                int page = parsePage(req), limit = parseLimit(req), offset = (page-1)*limit;
                String search = req.getParameter("search");
                String role   = req.getParameter("role");
                List<User> users = userDAO.findAll(search, role, limit, offset);
                int total = userDAO.countAll(search, role);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("users", gson.toJsonTree(users));
                res.add("pagination", pagination(page, limit, total));
                write(resp, 200, res);

            } else if ("/scans".equals(path)) {
                int page = parsePage(req), limit = parseLimit(req), offset = (page-1)*limit;
                String verdict   = req.getParameter("verdict");
                String inputType = req.getParameter("inputType");
                List<UserReport> scans = scanDAO.findAll(verdict, inputType, limit, offset);
                int total = scanDAO.countAll(verdict, inputType);
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("scans", gson.toJsonTree(scans));
                res.add("pagination", pagination(page, limit, total));
                write(resp, 200, res);

            } else if (path.matches("/scans/\\d+")) {
                int id = Integer.parseInt(path.substring(7));
                UserReport r = scanDAO.findById(id);
                if (r == null) { write(resp, 404, err("Scan not found")); return; }
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("scan", gson.toJsonTree(r));
                write(resp, 200, res);

            } else if ("/audit-logs".equals(path)) {
                int page = parsePage(req), limit = parseLimit(req), offset = (page-1)*limit;
                List<AdminAuditLog> logs = auditDAO.findAll(limit, offset);
                int total = auditDAO.countAll();
                JsonObject res = new JsonObject();
                res.addProperty("success", true);
                res.add("logs", gson.toJsonTree(logs));
                res.add("pagination", pagination(page, limit, total));
                write(resp, 200, res);

            } else {
                write(resp, 404, err("Not found"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            write(resp, 500, err("Internal server error"));
        }
    }

    // ── POST ────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!guardAdmin(req, resp)) return;
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            if ("/patterns".equals(path)) {
                JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
                JsonObject res = patternSvc.createPattern(uid(req), body);
                write(resp, 201, res);
            } else {
                write(resp, 404, err("Not found"));
            }
        } catch (IllegalArgumentException e) {
            write(resp, 400, err(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); write(resp, 500, err("Internal server error"));
        }
    }

    // ── PUT ─────────────────────────────────────────────────────────────────────
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!guardAdmin(req, resp)) return;
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            if (path.matches("/patterns/\\d+")) {
                int id = Integer.parseInt(path.split("/")[2]);
                JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
                JsonObject res = patternSvc.updatePattern(uid(req), id, body);
                write(resp, 200, res);
            } else {
                write(resp, 404, err("Not found"));
            }
        } catch (IllegalArgumentException e) {
            write(resp, 400, err(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); write(resp, 500, err("Internal server error"));
        }
    }

    // ── PATCH ───────────────────────────────────────────────────────────────────
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Handle PATCH since HttpServlet doesn't define doPatch
        if ("PATCH".equalsIgnoreCase(req.getMethod())) {
            handlePatch(req, resp);
        } else {
            super.service(req, resp);
        }
    }

    private void handlePatch(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!guardAdmin(req, resp)) return;
        String path = req.getPathInfo() == null ? "/" : req.getPathInfo();
        try {
            JsonObject body = gson.fromJson(req.getReader(), JsonObject.class);
            if (path.matches("/patterns/\\d+/status")) {
                int id = Integer.parseInt(path.split("/")[2]);
                boolean active = body.has("active") && body.get("active").getAsBoolean();
                write(resp, 200, patternSvc.setStatus(uid(req), id, active));

            } else if (path.matches("/community/\\d+/status")) {
                int id = Integer.parseInt(path.split("/")[2]);
                String status = body.has("status") ? body.get("status").getAsString() : "";
                write(resp, 200, commSvc.setFlagStatus(uid(req), id, status));

            } else if (path.matches("/users/\\d+/role")) {
                int id = Integer.parseInt(path.split("/")[2]);
                String role = body.has("role") ? body.get("role").getAsString() : "";
                write(resp, 200, userSvc.setRole(uid(req), id, role));

            } else {
                write(resp, 404, err("Not found"));
            }
        } catch (IllegalArgumentException e) {
            write(resp, 400, err(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); write(resp, 500, err("Internal server error"));
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────
    private void write(HttpServletResponse resp, int status, JsonObject body) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write(gson.toJson(body));
    }

    private JsonObject err(String msg) {
        JsonObject o = new JsonObject();
        o.addProperty("success", false);
        o.addProperty("message", msg);
        return o;
    }

    private JsonObject pagination(int page, int limit, int total) {
        JsonObject p = new JsonObject();
        int totalPages = (int) Math.ceil((double) total / limit);
        p.addProperty("page", page);
        p.addProperty("limit", limit);
        p.addProperty("totalRecords", total);
        p.addProperty("totalPages", totalPages == 0 ? 1 : totalPages);
        return p;
    }
}
