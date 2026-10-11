package com.bbms.web;

import com.bbms.model.Administrator;
import com.bbms.model.BloodInventory;
import com.bbms.model.BloodIssue;
import com.bbms.model.Donation;
import com.bbms.model.Donor;
import com.bbms.service.AuthService;
import com.bbms.service.DonorService;
import com.bbms.service.InventoryService;
import com.bbms.service.ReportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Blood Bank Management System
 * 
 * Bridges the Web Browser Frontend with Core Java OOP Services and MySQL JDBC.
 * Zero external dependencies: Uses standard JDK HTTP primitives and manual JSON serialization.
 */
public class ApiHandler implements HttpHandler {

    private final AuthService authService = AuthService.getInstance();
    private final DonorService donorService = new DonorService();
    private final InventoryService inventoryService = new InventoryService();
    private final ReportService reportService = new ReportService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod().toUpperCase();

        try {
            if ("/api/login".equals(path) && "POST".equals(method)) {
                handleLogin(exchange);
            } else if ("/api/dashboard".equals(path) && "GET".equals(method)) {
                handleGetDashboard(exchange);
            } else if ("/api/donors".equals(path)) {
                if ("GET".equals(method)) handleGetDonors(exchange);
                else if ("POST".equals(method)) handleAddDonor(exchange);
                else if ("PUT".equals(method)) handleUpdateDonor(exchange);
                else if ("DELETE".equals(method)) handleDeleteDonor(exchange);
                else sendError(exchange, 405, "Method Not Allowed");
            } else if ("/api/donations".equals(path) && "POST".equals(method)) {
                handleRecordDonation(exchange);
            } else if ("/api/inventory/update".equals(path) && "POST".equals(method)) {
                handleUpdateInventory(exchange);
            } else if ("/api/issues".equals(path) && "POST".equals(method)) {
                handleIssueBlood(exchange);
            } else if ("/api/reports".equals(path) && "GET".equals(method)) {
                handleGetReport(exchange);
            } else {
                sendError(exchange, 404, "Endpoint Not Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        String username = params.get("username");
        String password = params.get("password");

        Administrator admin = authService.login(username, password);
        if (admin != null) {
            String json = "{\"success\": true, \"admin\": {" +
                    "\"username\": \"" + escape(admin.getUsername()) + "\"," +
                    "\"name\": \"" + escape(admin.getName()) + "\"," +
                    "\"role\": \"" + escape(admin.getRole()) + "\"" +
                    "}}";
            sendJson(exchange, 200, json);
        } else {
            sendJson(exchange, 401, "{\"success\": false, \"message\": \"Invalid admin credentials\"}");
        }
    }

    private void handleGetDashboard(HttpExchange exchange) throws IOException {
        List<Donor> donors = donorService.getAllDonors();
        List<BloodInventory> inventory = inventoryService.getAllInventory();
        List<Donation> donations = inventoryService.getAllDonations();
        List<BloodIssue> issues = inventoryService.getAllIssues();

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        
        // Donors
        sb.append("\"donors\": [");
        for (int i = 0; i < donors.size(); i++) {
            Donor d = donors.get(i);
            sb.append("{\"id\": ").append(d.getId())
              .append(", \"name\": \"").append(escape(d.getName())).append("\"")
              .append(", \"bloodGroup\": \"").append(escape(d.getBloodGroup())).append("\"")
              .append(", \"contact\": \"").append(escape(d.getContact())).append("\"}");
            if (i < donors.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Inventory
        sb.append("\"inventory\": [");
        for (int i = 0; i < inventory.size(); i++) {
            BloodInventory bi = inventory.get(i);
            sb.append("{\"bloodGroup\": \"").append(escape(bi.getBloodGroup())).append("\"")
              .append(", \"quantity\": ").append(bi.getQuantity()).append("}");
            if (i < inventory.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Donations
        sb.append("\"donations\": [");
        for (int i = 0; i < donations.size(); i++) {
            Donation dn = donations.get(i);
            sb.append("{\"id\": ").append(dn.getDonationID())
              .append(", \"donorId\": ").append(dn.getDonorID())
              .append(", \"donorName\": \"").append(escape(dn.getDonorName())).append("\"")
              .append(", \"bloodGroup\": \"").append(escape(dn.getBloodGroup())).append("\"")
              .append(", \"quantity\": ").append(dn.getQuantity())
              .append(", \"date\": \"").append(dn.getDate()).append("\"}");
            if (i < donations.size() - 1) sb.append(",");
        }
        sb.append("],");

        // Issues
        sb.append("\"issues\": [");
        for (int i = 0; i < issues.size(); i++) {
            BloodIssue bi = issues.get(i);
            sb.append("{\"id\": ").append(bi.getIssueID())
              .append(", \"bloodGroup\": \"").append(escape(bi.getBloodGroup())).append("\"")
              .append(", \"quantity\": ").append(bi.getQuantity())
              .append(", \"date\": \"").append(bi.getDate()).append("\"")
              .append(", \"adminName\": \"").append(escape(bi.getAdminUsername())).append("\"")
              .append(", \"recipient\": \"").append(escape(bi.getRecipientInfo())).append("\"}");
            if (i < issues.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        sendJson(exchange, 200, sb.toString());
    }

    private void handleGetDonors(HttpExchange exchange) throws IOException {
        List<Donor> donors = donorService.getAllDonors();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < donors.size(); i++) {
            Donor d = donors.get(i);
            sb.append("{\"id\": ").append(d.getId())
              .append(", \"name\": \"").append(escape(d.getName())).append("\"")
              .append(", \"bloodGroup\": \"").append(escape(d.getBloodGroup())).append("\"")
              .append(", \"contact\": \"").append(escape(d.getContact())).append("\"}");
            if (i < donors.size() - 1) sb.append(",");
        }
        sb.append("]");
        sendJson(exchange, 200, sb.toString());
    }

    private void handleAddDonor(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        String name = params.get("name");
        String bloodGroup = params.get("bloodGroup");
        String contact = params.get("contact");

        boolean ok = donorService.addDonor(name, bloodGroup, contact);
        sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
    }

    private void handleUpdateDonor(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        int id = Integer.parseInt(params.get("id"));
        String name = params.get("name");
        String bloodGroup = params.get("bloodGroup");
        String contact = params.get("contact");

        boolean ok = donorService.updateDonor(id, name, bloodGroup, contact);
        sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
    }

    private void handleDeleteDonor(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        int id = 0;
        if (query != null && query.contains("id=")) {
            String[] parts = query.split("id=");
            if (parts.length > 1) {
                id = Integer.parseInt(parts[1].split("&")[0]);
            }
        }
        if (id <= 0) {
            sendError(exchange, 400, "Invalid donor id");
            return;
        }
        boolean ok = donorService.deleteDonor(id);
        sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
    }

    private void handleRecordDonation(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        int donorId = Integer.parseInt(params.get("donorId"));
        int quantity = Integer.parseInt(params.get("quantity"));
        Date date = Date.valueOf(params.get("date"));

        boolean ok = inventoryService.recordDonation(donorId, date, quantity);
        sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
    }

    private void handleUpdateInventory(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        String bloodGroup = params.get("bloodGroup");
        int quantity = Integer.parseInt(params.get("quantity"));

        boolean ok = inventoryService.updateStockQuantity(bloodGroup, quantity);
        sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
    }

    private void handleIssueBlood(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        Map<String, String> params = parseJson(body);

        String bloodGroup = params.get("bloodGroup");
        int quantity = Integer.parseInt(params.get("quantity"));
        Date date = Date.valueOf(params.get("date"));
        String recipient = params.get("recipient");

        try {
            boolean ok = inventoryService.issueBlood(bloodGroup, quantity, date, recipient);
            sendJson(exchange, ok ? 200 : 400, "{\"success\": " + ok + "}");
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, "{\"success\": false, \"message\": \"" + escape(e.getMessage()) + "\"}");
        }
    }

    private void handleGetReport(HttpExchange exchange) throws IOException {
        String report = reportService.generateExecutiveReport();
        sendJson(exchange, 200, "{\"report\": \"" + escape(report) + "\"}");
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, String responseJson) throws IOException {
        byte[] bytes = responseJson.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        sendJson(exchange, statusCode, "{\"error\": \"" + escape(message) + "\"}");
    }

    private String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1).trim();
        }
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replaceAll("^\"|\"$", "");
                String val = keyValue[1].trim().replaceAll("^\"|\"$", "");
                map.put(key, val);
            }
        }
        return map;
    }
}
