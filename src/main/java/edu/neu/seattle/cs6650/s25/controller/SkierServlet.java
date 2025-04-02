package edu.neu.seattle.cs6650.s25.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkierServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SkierServlet.class.getName());
    private static final String QUEUE_NAME = "skier_queue";
    private static final String RABBITMQ_EC2_IP = "54.245.33.144";
    private static final String REDIS_EC2_IP = "44.243.233.191";
    private static final int REDIS_PORT = 6379;

    private JedisPool jedisPool;
    private Connection rabbitConnection;
    private GenericObjectPool<Channel> channelPool;

    @Override
    public void init() throws ServletException {
        try {
            // Initialize Redis connection pool
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(50);
            jedisPool = new JedisPool(poolConfig, REDIS_EC2_IP, REDIS_PORT);
            LOGGER.info("✅ Redis" + REDIS_EC2_IP+ " connection initialized successfully.");
            // Debugging in EC2: sudo tail -f /usr/share/apache-tomcat-9.0.93/logs/catalina.out

            // Initialize RabbitMQ connection and channel pool
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(RABBITMQ_EC2_IP);  // Set RabbitMQ host IP
            rabbitConnection = factory.newConnection();

            // Configure channel pool
            GenericObjectPoolConfig<Channel> poolConfigChannel = new GenericObjectPoolConfig<>();
            poolConfigChannel.setMaxTotal(50);  // Set max total channels
            channelPool = new GenericObjectPool<>(new ChannelFactory(rabbitConnection), poolConfigChannel);
            LOGGER.info("✅ RabbitMQ" + RABBITMQ_EC2_IP+ " connection and channel pool initialized successfully.");
        } catch (Exception e) {
            throw new ServletException("Failed to initialize Redis or RabbitMQ.", e);
        }
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        String urlPath = request.getPathInfo();

        if (urlPath == null || urlPath.isEmpty() || !validateUrl(urlPath.split("/"))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Invalid URL format\"}");
            return;
        }

        String skierID = urlPath.split("/")[7];
        if (!isRequestAllowed(skierID)) {
            response.setStatus(429);
            response.getWriter().write("{\"error\": \"Rate limit exceeded. Try again later.\"}");
            return;
        }

        JSONObject requestBody = parseRequestBody(request);
        if (requestBody == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid JSON format\"}");
            return;
        }

        int time = requestBody.optInt("time", -1);
        int liftID = requestBody.optInt("liftID", -1);
        if (time < 1 || time > 360 || liftID < 1 || liftID > 40) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid time or liftID\"}");
            return;
        }

        JSONObject message = new JSONObject();
        message.put("resortID", Integer.parseInt(urlPath.split("/")[1]));
        message.put("seasonID", Integer.parseInt(urlPath.split("/")[3]));
        message.put("dayID", Integer.parseInt(urlPath.split("/")[5]));
        message.put("skierID", Integer.parseInt(urlPath.split("/")[7]));
        message.put("time", time);
        message.put("liftID", liftID);

        LOGGER.info("📩 Received request with skierID: " + skierID);
        if (sendMessageToQueue(message.toString())) {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write("{\"message\": \"Lift ride recorded for skier " + skierID + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Failed to process request\"}");
        }
    }

    private boolean isRequestAllowed(String skierID) {
        try (Jedis jedis = jedisPool.getResource()) {
            String redisKey = "rate_limit:" + skierID;
            int REQUEST_LIMIT = 80000;
            int TIME_WINDOW = 10;

            Long currentCount = jedis.incr(redisKey);
            if (currentCount == 1) jedis.expire(redisKey, TIME_WINDOW);
            if (currentCount > REQUEST_LIMIT) {
                LOGGER.info("🚫 Rate limit exceeded for skier: " + skierID);
                return false;
            }
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Redis rate limiting error", e);
            return true;
        }
    }

    private boolean sendMessageToQueue(String message) {
        Channel channel = null;
        try {
            // Borrow a channel from the pool
            channel = channelPool.borrowObject();

            // Publish the message to the RabbitMQ queue
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("📩 Sent message to RabbitMQ queue: " + message);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error sending message to RabbitMQ", e);
            return false;
        } finally {
            if (channel != null) {
                try {
                    // Return the channel back to the pool
                    channelPool.returnObject(channel);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to return channel to pool", e);
                }
            }
        }
    }


    private boolean validateUrl(String[] urlParts) {
        return urlParts.length == 8 && "seasons".equals(urlParts[2]) && "day".equals(urlParts[4]) &&
                "skier".equals(urlParts[6]) && isValidInteger(urlParts[1]) &&
                isValidYear(urlParts[3]) && isValidDay(urlParts[5]) && isValidInteger(urlParts[7]);
    }

    private boolean isValidInteger(String str) {
        try { return Integer.parseInt(str) >= 0; } catch (NumberFormatException e) { return false; }
    }

    private boolean isValidYear(String str) { return str.matches("\\d{4}"); }

    private boolean isValidDay(String str) {
        try { return Integer.parseInt(str) >= 1 && Integer.parseInt(str) <= 366; }
        catch (NumberFormatException e) { return false; }
    }

    private JSONObject parseRequestBody(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
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
            }
            if (rabbitConnection != null) {
                rabbitConnection.close();
            }
            if (jedisPool != null) {
                jedisPool.close();
            }
            LOGGER.info("✅ Resources cleaned up successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during cleanup", e);
        }
    }

}
