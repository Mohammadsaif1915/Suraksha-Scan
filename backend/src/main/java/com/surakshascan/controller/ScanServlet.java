package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.surakshascan.service.ScanService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/scan")
public class ScanServlet extends HttpServlet {
    private ScanService scanService = new ScanService();
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
        
        int userId = (Integer) session.getAttribute("userId");
        
        try {
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
            if (json == null) throw new JsonSyntaxException("Empty body");

            String inputType = json.has("inputType") ? json.get("inputType").getAsString() : null;
            String content = json.has("content") ? json.get("content").getAsString() : null;

            JsonObject result = scanService.performScan(userId, inputType, content);
            
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(result));
            
        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON format\"}");
        } catch (Exception e) {
            e.printStackTrace(); // Log internally
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Internal Server Error\"}");
        }
    }
}
