package com.surakshascan.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.surakshascan.service.ScanService;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * OcrScanServlet — Handles image-based scam detection requests.
 *
 * The OCR text extraction is performed client-side using Tesseract.js
 * and the extracted text is sent to this servlet as a plain text string
 * alongside the inputType. This keeps Tesseract (a large native library)
 * off the Java classpath while still routing all actual scan logic through
 * the existing RuleEngine + ScamPatternDAO pipeline.
 *
 * Endpoint: POST /api/scan/ocr
 * Content-Type: application/json  (extracted text forwarded from frontend)
 * OR multipart/form-data           (future: server-side extraction path)
 */
@WebServlet("/api/scan/ocr")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)   // 5 MB max image
public class OcrScanServlet extends HttpServlet {

    private ScanService scanService = new ScanService();
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // Auth check
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Authentication required\"}");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        try {
            // Accept JSON body: { "extractedText": "...", "inputType": "sms" }
            JsonObject json = gson.fromJson(req.getReader(), JsonObject.class);

            if (json == null || !json.has("extractedText")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"extractedText field is required\"}");
                return;
            }

            String extractedText = json.get("extractedText").getAsString().trim();
            // OCR text is always treated as SMS-type for scan analysis
            String inputType = json.has("inputType") ? json.get("inputType").getAsString() : "sms";

            if (extractedText.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"success\":false,\"message\":\"No text could be extracted from the image\"}");
                return;
            }

            if (extractedText.length() > 5000) {
                extractedText = extractedText.substring(0, 5000);
            }

            JsonObject result = scanService.performScan(userId, inputType, extractedText);

            // Add the extracted text to the response so frontend can show it
            if (result.has("scan")) {
                result.getAsJsonObject("scan").addProperty("ocrExtractedText", extractedText);
            }

            result.addProperty("ocrSource", true);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(gson.toJson(result));

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", e.getMessage());
            resp.getWriter().write(gson.toJson(error));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Internal Server Error during OCR scan\"}");
        }
    }
}
