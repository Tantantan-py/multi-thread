package edu.neu.seattle.cs6650.s25.controller;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

public class SkierServlet extends HttpServlet {
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
            response.getWriter().write("doPost - missing time parameter for SkierServlet");
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
            response.getWriter().write("doPost - missing liftID parameter for SkierServlet");
            return;
        }

        response.setStatus(HttpServletResponse.SC_CREATED);
        // TODO (maybe also some value if input is valid?)
        response.getWriter().write("doPost works for SkierServlet!");
    }
}