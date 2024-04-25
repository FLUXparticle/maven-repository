package com.example.maven.server;

import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class MavenServer {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 8081), 0);

        HttpContext context = server.createContext(MavenHttpHandler.PATH, new MavenHttpHandler());
        List<Filter> filters = context.getFilters();
        filters.add(new LoggingFilter());
        filters.add(new SecurityFilter());

        server.start();
        System.out.println("Server started ...");
    }

}
