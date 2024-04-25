package com.example.maven.server;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;

public class LoggingFilter extends Filter {

    private long lastMillis;

    private String lastPath;

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        Exception exception = null;
        try {
            chain.doFilter(exchange);
        } catch (Exception e) {
            exception = e;
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, -1);
        } finally {
            String path = exchange.getRequestURI().getPath();

            long now = System.currentTimeMillis();
            if (lastMillis > 0 && now - lastMillis > 1_000) {
                System.out.println();
                lastPath = null;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(exchange.getRequestMethod());
            while (sb.length() < 6) {
                sb.append(' ');
            }
            if (lastPath == null) {
                sb.append(path);
                lastPath = path;
            } else {
                sb.append(shortPath(path));
            }
            sb.append(" -> ");
            sb.append(exchange.getResponseCode());
            String contentType = exchange.getResponseHeaders().getFirst("Content-Type");
            if (contentType != null) {
                sb.append(" Content-Type: ");
                sb.append(contentType);
            }
            System.out.print(sb);
            if (exception == null) {
                System.out.println();
            } else {
                System.out.print(' ');
                exception.printStackTrace(System.out);
            }
            System.out.flush();

            lastMillis = now;
        }
    }

    private String shortPath(String str) {
        int end = lastPath.lastIndexOf('/');
        String[] segments1 = lastPath.substring(0, end).split("/");
        String[] segments2 = str.split("/");

        int splitSegment = 0;
        for (; splitSegment < segments1.length; splitSegment++) {
            if (!segments1[splitSegment].equals(segments2[splitSegment])) {
                break;
            }
        }

        // Erstelle das Ergebnis mit "..." am Anfang
        StringBuilder result = new StringBuilder();
        result.append("...");

        for (int i = splitSegment; i < segments2.length; i++) {
            result.append("/");
            result.append(segments2[i]);
        }

        return result.toString();
    }

    @Override
    public String description() {
        return "Logging Filter";
    }

}
