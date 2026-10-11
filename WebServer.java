package com.bbms.web;

import com.bbms.db.DBInitializer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.concurrent.Executors;

/**
 * Web Server: WebServer
 * 
 * Standalone embedded Java HTTP server for Blood Bank Management System.
 * - Serves frontend web assets from the 'web/' directory.
 * - Routes REST API requests to ApiHandler.
 * - Auto-initializes MySQL database tables on launch.
 * - Zero third-party dependencies: Runs on standard JDK 8+.
 */
public class WebServer {

    private static final int PORT = 8080;
    private static final String WEB_ROOT = "web";

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   BLOOD BANK MANAGEMENT SYSTEM (WEB EDITION)    ");
        System.out.println("   Java Backend • REST API • Modern Web Portal   ");
        System.out.println("=================================================");

        // 1. Initialize Database Schema & Seed Data
        try {
            System.out.println("Connecting to MySQL and checking database schema...");
            DBInitializer.initializeDatabase();
            System.out.println("Database tables ready.");
        } catch (Exception e) {
            System.err.println("Notice: Could not connect to MySQL server (" + e.getMessage() + ").");
            System.err.println("The web application will function with live in-memory / local fallback mode.");
        }

        // 2. Start HTTP Server
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            // Register API Handler
            server.createContext("/api", new ApiHandler());

            // Register Static File Handler
            server.createContext("/", new StaticFileHandler());

            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();

            System.out.println("-------------------------------------------------");
            System.out.println("Server successfully started on port " + PORT + "!");
            System.out.println("Open your web browser and navigate to:");
            System.out.println("   --> http://localhost:" + PORT);
            System.out.println("-------------------------------------------------");
            System.out.println("Admin Default Credentials: admin / admin123");
            System.out.println("Press Ctrl+C to terminate the server.");

        } catch (IOException e) {
            System.err.println("Failed to start web server on port " + PORT + ": " + e.getMessage());
        }
    }

    /**
     * Serves static HTML, CSS, and JS assets from the 'web/' directory.
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if ("/".equals(path) || "".equals(path)) {
                path = "/index.html";
            }

            File file = new File(WEB_ROOT, path);
            if (!file.exists() || file.isDirectory()) {
                String response = "404 Not Found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String mimeType = Files.probeContentType(file.toPath());
            if (mimeType == null) {
                if (path.endsWith(".css")) mimeType = "text/css";
                else if (path.endsWith(".js")) mimeType = "application/javascript";
                else if (path.endsWith(".html")) mimeType = "text/html";
                else mimeType = "application/octet-stream";
            }

            exchange.getResponseHeaders().set("Content-Type", mimeType + "; charset=UTF-8");
            exchange.sendResponseHeaders(200, file.length());

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        }
    }
}
