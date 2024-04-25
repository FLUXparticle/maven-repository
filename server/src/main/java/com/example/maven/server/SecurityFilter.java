package com.example.maven.server;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class SecurityFilter extends Filter {

    enum Role {
        USER, ADMIN
    }

    private static final Map<String, String> CREDENTIALS = new HashMap<>();

    static {
        CREDENTIALS.put("admin", "admin123");
        CREDENTIALS.put("user", "user123");
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String method = exchange.getRequestMethod();
        Headers headers = exchange.getRequestHeaders();

        String authorization = headers.getFirst("Authorization");
        Role role = getRole(authorization);

        // Überprüfen der Autorisierung
        if (role == null) {
            exchange.getResponseHeaders().add("WWW-Authenticate", "Basic realm=\"Maven Repository\"");
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_UNAUTHORIZED, -1);
            return;
        }

        // Überprüfen des Request-Typs (PUT ist nur für Admins erlaubt)
        if ("PUT".equals(method) && role != Role.ADMIN) {
            exchange.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, -1);
            return;
        }

        chain.doFilter(exchange);
    }

    private static Role getRole(String authorizationHeader) {
        if (authorizationHeader == null) {
            return null;
        }

        // Extrahieren des Benutzernamens und des Passworts aus dem Authorization-Header
        String encodedCredentials = authorizationHeader.replaceFirst("Basic ", "");
        String decodedCredentials = new String(Base64.getDecoder().decode(encodedCredentials));
        String[] credentials = decodedCredentials.split(":");

        // Überprüfen, ob die Anmeldeinformationen gültig sind
        String username = credentials[0];
        String password = credentials[1];
        String expectedPassword = CREDENTIALS.get(username);

        if (!password.equals(expectedPassword)) {
            return null;
        } else if ("admin".equals(username)) {
            return Role.ADMIN;
        } else {
            return Role.USER;
        }
    }

    @Override
    public String description() {
        return "Security Filter";
    }

}
