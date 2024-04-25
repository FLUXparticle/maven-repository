package com.example.maven.server;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class MavenHttpHandler implements HttpHandler {

    public static final String PATH = "/";

    private final Path dir;

    public MavenHttpHandler() throws IOException {
        dir = Paths.get(PATH.substring(1));
        Files.createDirectories(dir);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath().substring(PATH.length());

        Path resolved = dir.resolve(path);

        switch (method) {
            case "GET":
                if (Files.exists(resolved)) {
                    if (Files.isDirectory(resolved)) {
                        resolved = resolved.resolve("index.html");
                    }
                    long size = Files.size(resolved);
                    String contentType = getContentType(resolved.toString());
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, size);
                    try (OutputStream output = exchange.getResponseBody()) {
                        Files.copy(resolved, output);
                    }
                } else {
                    exchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, -1);
                }
                break;
            case "PUT":
                Files.createDirectories(resolved.getParent());
                Files.copy(exchange.getRequestBody(), resolved, StandardCopyOption.REPLACE_EXISTING);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, -1);
                break;
            case "MKCOL":
                Files.createDirectories(resolved);
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, -1);
                break;
            default:
                exchange.getResponseHeaders().set("Allow", "GET,PUT,MKCOL");
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_BAD_METHOD, -1);
        }
    }

    private static String getContentType(String filePath) {
        int split = filePath.lastIndexOf('.');
        if (split >= 0) {
            switch (filePath.substring(split)) {
                case ".xml":
                case ".pom":
                    return "application/xml";
                case ".md5":
                case ".sha1":
                    return "text/plain";
                case ".jar":
                    return "application/java-archive";
                case ".html":
                    return "text/html";
                case ".css":
                    return "text/css";
                case ".gif":
                    return "image/gif";
                case ".png":
                    return "image/x-png";
            }
        }
        throw new UnsupportedOperationException(filePath);
    }

}
