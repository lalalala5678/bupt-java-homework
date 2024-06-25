package com.example.chatserver;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.*;
import java.net.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ChatServer {
    private static final int PORT = 8000;
    private static Map<String, String> users = new HashMap<>();
    private static List<Map<String, String>> messages = new ArrayList<>();
    private static Set<String> onlineUsers = new HashSet<>();

    public static void main(String[] args) throws Exception {
        // 打印当前工作路径
        System.out.println("当前工作路径: " + Paths.get("").toAbsolutePath().toString());

        // 读入用户文件
        BufferedReader userReader = new BufferedReader(new FileReader("users.txt"));
        String line;
        while ((line = userReader.readLine()) != null) {
            String[] parts = line.split(" ");
            users.put(parts[0], parts[1]);
        }
        userReader.close();

        // 启动HTTP服务器
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/send", new SendMessageHandler());
        server.createContext("/messages", new GetMessagesHandler());
        server.createContext("/list", new ListUsersHandler());
        server.createContext("/logout", new LogoutHandler());
        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("服务器启动，端口：" + PORT);
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String requestBody = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                Map<String, String> requestData = parseRequestBody(requestBody);

                String username = requestData.get("username");
                String password = requestData.get("password");

                String response;
                if (users.containsKey(username) && users.get(username).equals(password)) {
                    onlineUsers.add(username); // 添加到在线用户列表
                    response = "{\"success\": true}";
                } else {
                    response = "{\"success\": false}";
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class SendMessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String requestBody = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                Map<String, String> requestData = parseRequestBody(requestBody);

                String messageContent = URLDecoder.decode(requestData.get("message"), "UTF-8");
                Map<String, String> message = new HashMap<>();
                message.put("username", requestData.get("username"));
                message.put("message", messageContent);

                messages.add(message);

                String response = "{\"success\": true, \"totalMessages\": " + messages.size() + "}";

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class GetMessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                int clientMessageCount = Integer.parseInt(query.split("=")[1]);

                List<Map<String, String>> newMessages = messages.subList(clientMessageCount, messages.size());
                Map<String, Object> response = new HashMap<>();
                response.put("newMessages", newMessages);
                response.put("totalMessages", messages.size());

                String jsonResponse = new Gson().toJson(response);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }
    }

    static class ListUsersHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                String jsonResponse = new Gson().toJson(onlineUsers);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes());
                os.close();
            }
        }
    }

    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                System.out.println("处理退出请求...");
                InputStream is = exchange.getRequestBody();
                String requestBody = new BufferedReader(new InputStreamReader(is)).lines()
                        .collect(Collectors.joining("\n"));
                System.out.println("接收到的请求体: " + requestBody);
                Map<String, String> requestData = parseRequestBody(requestBody);

                String username = requestData.get("username");

                String response;
                if (onlineUsers.contains(username)) {
                    onlineUsers.remove(username); // 从在线用户列表中移除
                    response = "{\"success\": true}";
                } else {
                    response = "{\"success\": false}";
                }

                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("发送响应: " + response);
            }
        }
    }

    private static Map<String, String> parseRequestBody(String body) {
        Map<String, String> data = new HashMap<>();
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            try {
                data.put(keyValue[0], URLDecoder.decode(keyValue[1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}
