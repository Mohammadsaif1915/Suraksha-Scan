package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.surakshascan.dao.UserDAO;
import com.surakshascan.model.User;
import com.surakshascan.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
    private UserDAO userDAO = new UserDAO();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();
        
        if ("/register".equals(pathInfo)) {
            handleRegister(req, resp);
        } else if ("/login".equals(pathInfo)) {
            handleLogin(req, resp);
        } else if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Not Found\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String pathInfo = req.getPathInfo();

        if ("/me".equals(pathInfo)) {
            handleMe(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("{\"success\":false,\"message\":\"Not Found\"}");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
            if (json == null) throw new JsonSyntaxException("Empty body");

            String name = json.has("name") ? json.get("name").getAsString().trim() : "";
            String email = json.has("email") ? json.get("email").getAsString().trim().toLowerCase() : "";
            String password = json.has("password") ? json.get("password").getAsString() : "";
            String role = json.has("role") ? json.get("role").getAsString().trim() : "";

            if (name.isEmpty() || email.isEmpty() || password.length() < 6 || 
                (!role.equals("normal") && !role.equals("guardian"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"Please provide valid registration details\"}");
                return;
            }

            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"Please provide valid registration details\"}");
                return;
            }

            if (userDAO.findByEmail(email) != null) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"success\":false,\"message\":\"An account with this email already exists\"}");
                return;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(PasswordUtil.hashPassword(password));
            user.setRole(role);

            if (userDAO.createUser(user)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write("{\"success\":true,\"message\":\"Registration successful\"}");
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\":false,\"message\":\"Server error during registration\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON payload\"}");
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);
            if (json == null) throw new JsonSyntaxException("Empty body");

            String email = json.has("email") ? json.get("email").getAsString().trim().toLowerCase() : "";
            String password = json.has("password") ? json.get("password").getAsString() : "";

            if (email.isEmpty() || password.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"success\":false,\"message\":\"Invalid email or password\"}");
                return;
            }

            User user = userDAO.findByEmail(email);
            if (user != null && PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("name", user.getName());
                session.setAttribute("email", user.getEmail());
                session.setAttribute("role", user.getRole());
                
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Login successful");
                JsonObject userJson = new JsonObject();
                userJson.addProperty("userId", user.getUserId());
                userJson.addProperty("name", user.getName());
                userJson.addProperty("email", user.getEmail());
                userJson.addProperty("role", user.getRole());
                responseJson.add("user", userJson);
                
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write(gson.toJson(responseJson));
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.getWriter().write("{\"success\":false,\"message\":\"Invalid email or password\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"success\":false,\"message\":\"Invalid JSON payload\"}");
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write("{\"success\":true,\"message\":\"Logout successful\"}");
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        JsonObject responseJson = new JsonObject();
        if (session != null && session.getAttribute("userId") != null) {
            responseJson.addProperty("success", true);
            responseJson.addProperty("authenticated", true);
            JsonObject userJson = new JsonObject();
            userJson.addProperty("userId", (Integer) session.getAttribute("userId"));
            userJson.addProperty("name", (String) session.getAttribute("name"));
            userJson.addProperty("email", (String) session.getAttribute("email"));
            userJson.addProperty("role", (String) session.getAttribute("role"));
            responseJson.add("user", userJson);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            responseJson.addProperty("success", false);
            responseJson.addProperty("authenticated", false);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
        resp.getWriter().write(gson.toJson(responseJson));
    }
}
