package edu.neu.seattle.cs6650.s25.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;


@WebServlet("/hello/*")
public class HelloWorldServlet extends HttpServlet {
    private String msg;
    private int sleepTime = 1000;

    public void init() throws ServletException {
        // Initialization
        msg = "Hello World From CS6650 - Lucian";
    }

    // handle a GET request
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        // Set response content type to text
        res.setContentType("text/html");

        // sleep for 1000ms. You can vary this value for different tests
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String urlPath = req.getPathInfo();

        // check we have a URL
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("doGet - missing URL parameters for HelloWorldServlet");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("doGet - invalid URL parameters for HelloWorldServlet");
        } else {
            res.setStatus(HttpServletResponse.SC_OK);
            // Send the response
            PrintWriter out = res.getWriter();
            out.println("<h1>" + msg + "</h1>" + "with sleep time of " + sleepTime + " ms");
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

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("doPost - missing URL parameters for HelloWorldServlet");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!isUrlValid(urlParts)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("doPost - invalid URL parameters for HelloWorldServlet");
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
        String requestBody = sb.toString();

        int time = -1;
        int liftID = -1;

        String timeKey = "\"time\":";
        int timeIndex = requestBody.indexOf(timeKey);
        if (timeIndex != -1) {
            int start = timeIndex + timeKey.length();
            int end = requestBody.indexOf(',', start);
            if (end == -1) {
                end = requestBody.indexOf('}', start);
            }
            if (end > start) {
                String timeValue = requestBody.substring(start, end).trim();
                time = Integer.parseInt(timeValue);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("doPost - missing time parameter");
            return;
        }

        String liftIDKey = "\"liftID\":";
        int liftIDIndex = requestBody.indexOf(liftIDKey);
        if (liftIDIndex != -1) {
            int start = liftIDIndex + liftIDKey.length();
            int end = requestBody.indexOf(',', start);
            if (end == -1) {
                end = requestBody.indexOf('}', start);
            }
            if (end > start) {
                String liftIDValue = requestBody.substring(start, end).trim();
                liftID = Integer.parseInt(liftIDValue);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("doPost - missing liftID parameter");
            return;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.getWriter().write("doPost works!");
    }


    public void destroy() {
        // nothing to do here
    }
}




