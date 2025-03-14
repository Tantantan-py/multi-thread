package edu.neu.seattle.cs6650.s25.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.json.JSONObject;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SkierServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SkierServlet.class.getName());
    private static final String QUEUE_NAME = "skier_queue";

    private ConnectionFactory factory;
    private Connection rabbitConnection;
    private ObjectPool<Channel> channelPool;

    @Override
    public void init() throws ServletException {
        try {
            // Initialize RabbitMQ Connection
            // Should store on env vars, but hardcoded for simplicity
            factory = new ConnectionFactory();
            // replace with your RMQ  EC2 public IP
            factory.setHost("54.245.209.3");
            factory.setUsername("assignment2");
            factory.setPassword("assignment2");

            rabbitConnection = factory.newConnection();

            // Initialize Channel Pool
            channelPool = new GenericObjectPool<>(new ChannelFactory(rabbitConnection));

        } catch (IOException | TimeoutException e) {
            throw new ServletException("Failed to initialize RabbitMQ connection and channel pool", e);
        }
    }

    // TODO DoGet temporarily removed since Assignment2 2 only requires doPost, just refactor my isURLValid method.

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Missing URL parameters\"}");
            return;
        }

        String[] urlParts = urlPath.split("/");
        if (!validateUrl(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Invalid URL format\"}");
            return;
        }

        JSONObject requestBody = parseRequestBody(request);
        if (requestBody == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // Extract values
        int time = requestBody.optInt("time", -1);
        int liftID = requestBody.optInt("liftID", -1);
        if (time < 1 || time > 360 || liftID < 1 || liftID > 40) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid time or liftID\"}");
            return;
        }

        JSONObject message = new JSONObject();
        message.put("resortID", Integer.parseInt(urlParts[1]));
        message.put("seasonID", Integer.parseInt(urlParts[3]));
        message.put("dayID", Integer.parseInt(urlParts[5]));
        message.put("skierID", Integer.parseInt(urlParts[7]));
        message.put("time", time);
        message.put("liftID", liftID);

        if (sendMessageToQueue(message.toString())) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Lift ride recorded successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to process request\"}");
        }
    }

    private boolean sendMessageToQueue(String message) {
        Channel channel = null;
        try {
            channel = channelPool.borrowObject();
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending message to RabbitMQ", e);
            return false;
        } finally {
            if (channel != null) {
                try {
                    channelPool.returnObject(channel);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to return channel to pool", e);
                }
            }
        }
    }

    private boolean validateUrl(String[] urlParts) {
        return urlParts.length == 8 &&
                "seasons".equals(urlParts[2]) &&
                "day".equals(urlParts[4]) &&
                "skier".equals(urlParts[6]) &&
                isValidInteger(urlParts[1]) &&
                isValidYear(urlParts[3]) &&
                isValidDay(urlParts[5]) &&
                isValidInteger(urlParts[7]);
    }

    private boolean isValidInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidYear(String str) {
        return str.matches("\\d{4}");
    }

    private boolean isValidDay(String str) {
        try {
            int day = Integer.parseInt(str);
            return day >= 1 && day <= 366;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private JSONObject parseRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return new JSONObject(sb.toString());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error parsing request body", e);
            return null;
        }
    }

    @Override
    public void destroy() {
        try {
            if (channelPool != null) {
                channelPool.close();
                LOGGER.log(Level.INFO, "Channel pool closed");
            }
            if (rabbitConnection != null) {
                rabbitConnection.close();
                LOGGER.log(Level.INFO, "RabbitMQ connection closed");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error closing resources", e);
        }
    }
}
