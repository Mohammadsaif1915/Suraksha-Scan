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
