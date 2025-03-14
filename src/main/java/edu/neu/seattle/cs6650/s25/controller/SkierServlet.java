package edu.neu.seattle.cs6650.s25.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import org.json.JSONObject;


public class SkierServlet extends HttpServlet {
    private static final String QUEUE_NAME = "skier_queue";
    private ConnectionFactory factory;

    @Override
    public void init() throws ServletException {
        factory = new ConnectionFactory();
        // Update with your own ec2 public ip
        factory.setHost("35.91.131.96");
        factory.setUsername("assignment2");
        factory.setPassword("assignment2");

    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
        String urlPath = req.getPathInfo();

        // check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("doGet - missing URL parameters for SkierServlet");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("doGet - invalid URL parameters for SkierServlet");
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // TODO (maybe also some value if input is valid?)
            res.getWriter().write("doGet works for SkierServlet!");
        }
    }

    private boolean isUrlValid(String[] urlParts) {
        // urlPath  = "/1/seasons/2019/day/1/skier/123"
        // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
        // ["", "{resortID}", "seasons", "{seasonID}", "day", "{dayID}", "skier", "{skierID}"]

        // check if the urlParts has 8 elements
        if (urlParts.length != 8) {
            return false;
        }
        // check if the urlParts have the correct format
        if (!urlParts[2].equals("seasons") || !urlParts[4].equals("day") || !urlParts[6].equals("skier")) {
            return false;
        }
        // check if the resortID is a valid integer
        try {
            Integer.parseInt(urlParts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        // check if the seasonID is a valid year
        int seasonID = Integer.parseInt(urlParts[3]);
        if (String.valueOf(seasonID).length() != 4) {
            return false;
        }
        // check if the dayID is a valid year
        int dayID = Integer.parseInt(urlParts[5]);
        if (dayID < 1 || dayID > 366) {
            return false;
        }
        // check if the skierID is a valid integer
        try {
            Integer.parseInt(urlParts[7]);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("doPost - missing URL parameters for SkierServlet");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("doPost - invalid URL parameters for SkierServlet");
            return;
        }

        // Handle the JSON payload
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(sb.toString());
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        // Validate JSON fields
        if (!jsonObject.has("time") || !jsonObject.has("liftID")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Missing required fields (time, liftID)\"}");
            return;
        }

        int time;
        int liftID;
        try {
            time = jsonObject.getInt("time");
            liftID = jsonObject.getInt("liftID");

            if (time < 1 || time > 360) {
                throw new IllegalArgumentException("Invalid time range");
            }
            if (liftID < 1 || liftID > 40) {
                throw new IllegalArgumentException("Invalid liftID range");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid numeric values\"}");
            return;
        }

        // Format data for the queue
        JSONObject message = new JSONObject();
        message.put("resortID", Integer.parseInt(urlParts[1]));
        message.put("seasonID", Integer.parseInt(urlParts[3]));
        message.put("dayID", Integer.parseInt(urlParts[5]));
        message.put("skierID", Integer.parseInt(urlParts[7]));
        message.put("time", time);
        message.put("liftID", liftID);

        // Send message to RabbitMQ
        if (sendMessageToQueue(message.toString())) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Lift ride recorded successfully\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to process request\"}");
        }
    }

    private boolean sendMessageToQueue(String message) {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            return false;
        }
    }
}