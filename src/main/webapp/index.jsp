<%--
  Created by IntelliJ IDEA.
  User: apple
  Date: 1/21/25
  Time: 5:49 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>CS6650 Lab</title>

    <!-- Google Font: Montserrat -->
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin/>
    <link
            href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap"
            rel="stylesheet">

    <!-- Font Awesome CDN for Icons -->
    <link
            rel="stylesheet"
            href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css"
            integrity="sha512-p1CmJGqLaRI9/RGThO05hDm0mF5nU1/6N21iEMTf+K30/lymbE1Nz6QiNiq1YgACV9nZUZ32k1fGc0y3jX1aBw=="
            crossorigin="anonymous"
            referrerpolicy="no-referrer"
    />

    <style>
        /* CSS Variables for Light and Dark Themes */
        :root {
            --background-color: #f3f3f3;
            --text-color: #333;
            --header-bg: linear-gradient(135deg, #0f9b0f, #00a400, #0f9b0f);
            --header-text-color: #fff;
            --button-bg: #0f9b0f;
            --button-hover-bg: #0b7f0b;
            --footer-color: #777;
            --tooltip-bg: #555;
        }

        [data-theme="dark"] {
            --background-color: #1e1e1e;
            --text-color: #e0e0e0;
            --header-bg: linear-gradient(135deg, #333, #444, #333);
            --header-text-color: #fff;
            --button-bg: #555;
            --button-hover-bg: #777;
            --footer-color: #aaa;
            --tooltip-bg: #333;
        }

        /* Reset and global settings */
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        html, body {
            font-family: 'Montserrat', sans-serif;
            height: 100%;
            background: var(--background-color);
            color: var(--text-color);
            transition: background 0.3s ease, color 0.3s ease;
        }

        /* Gradient header with animation */
        header {
            background: var(--header-bg);
            background-size: 300% 300%;
            animation: gradientBG 10s ease infinite;
            text-align: center;
            padding: 40px 20px;
            color: var(--header-text-color);
            margin-bottom: 20px;
            position: relative;
        }

        @keyframes gradientBG {
            0% {
                background-position: 0% 50%;
            }
            50% {
                background-position: 100% 50%;
            }
            100% {
                background-position: 0% 50%;
            }
        }

        header h1 {
            font-weight: 700;
            font-size: 2.5rem;
        }

        /* Dark Mode Toggle Button */
        .toggle-btn {
            position: absolute;
            top: 20px;
            right: 20px;
            background: none;
            border: none;
            color: var(--header-text-color);
            font-size: 1.5rem;
            cursor: pointer;
            transition: color 0.3s ease;
        }

        .toggle-btn:hover {
            color: #ddd;
        }

        /* Container for page content */
        .container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: #fff;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
            transition: background 0.3s ease, box-shadow 0.3s ease;
        }

        [data-theme="dark"] .container {
            background: #2e2e2e;
            box-shadow: 0 2px 8px rgba(255, 255, 255, 0.1);
        }

        .intro {
            text-align: center;
            margin-bottom: 20px;
        }

        .intro p {
            margin: 10px 0 20px;
            line-height: 1.6;
        }

        /* Styled buttons */
        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 24px;
            margin: 5px;
            border-radius: 25px;
            border: none;
            font-size: 1rem;
            font-weight: 600;
            text-transform: uppercase;
            color: #fff;
            background: var(--button-bg);
            cursor: pointer;
            transition: background 0.3s ease, transform 0.2s ease;
            text-decoration: none;
        }

        .btn:hover {
            background: var(--button-hover-bg);
            transform: scale(1.05);
        }

        /* GitHub Button Specific Styles */
        .github-btn {
            background: #333; /* GitHub's black color */
        }

        .github-btn:hover {
            background: #555; /* Darker shade on hover */
        }

        .github-btn i {
            margin-right: 8px; /* Space between icon and text */
        }

        /* Tooltip container */
        .tooltip {
            position: relative;
            display: inline-block;
            cursor: pointer;
        }

        /* Tooltip text */
        .tooltip .tooltiptext {
            visibility: hidden;
            width: 130px;
            background-color: var(--tooltip-bg);
            color: #fff;
            text-align: center;
            border-radius: 6px;
            padding: 8px 0;
            position: absolute;
            z-index: 1;
            bottom: 125%; /* Position above the element */
            left: 50%;
            margin-left: -80px; /* Center the tooltip */
            opacity: 0;
            transition: opacity 0.3s;
        }

        /* Tooltip arrow */
        .tooltip .tooltiptext::after {
            content: "";
            position: absolute;
            top: 100%; /* At the bottom of the tooltip */
            left: 50%;
            margin-left: -5px;
            border-width: 5px;
            border-style: solid;
            border-color: var(--tooltip-bg) transparent transparent transparent;
        }

        /* Show tooltip on hover */
        .tooltip:hover .tooltiptext {
            visibility: visible;
            opacity: 1;
        }

        /* GitHub Button Positioning */
        .github-container {
            margin-top: 20px;
            text-align: center;
        }

        /* Footer styling */
        footer {
            text-align: center;
            margin-top: 20px;
            color: var(--footer-color);
            font-size: 0.9rem;
            transition: color 0.3s ease;
        }

        /* Smooth button animation */
        .btn:active {
            transform: scale(0.95);
        }

        /* Responsive Design */
        @media (max-width: 600px) {
            header h1 {
                font-size: 2rem;
            }

            .btn {
                padding: 10px 20px;
                font-size: 0.9rem;
            }

            .tooltip .tooltiptext {
                width: 140px;
                margin-left: -70px;
            }
        }
    </style>
</head>

<body data-theme="light">
<header>
    <h1>CS6650 Lab</h1>
    <button class="toggle-btn" id="theme-toggle" aria-label="Toggle Dark Mode">
        <i class="fas fa-moon"></i>
    </button>
    <p id="greeting" style="font-size:1.2rem; margin-top: 10px;"></p>
</header>

<div class="container">
    <div class="intro">
        <p>
            Welcome to Lucian's CS6650 Lab!
        </p>
        <p>No worries, I'm just taking the Web Dev course to make this page fancier.</p>

        <!-- Functional Lab Links with Tooltips -->
        <div class="tooltip">
            <a href="http://localhost:8080/multi_threads_war_exploded/skiers/12/seasons/2019/day/1/skier/123" class="btn"
               target="_blank" rel="noopener noreferrer">
                Test doGet Lab2
            </a>
            <span class="tooltiptext">Get For Skiers</span>
        </div>

        <div class="tooltip">
            <a href="http://localhost:8080/multi_threads_war_exploded/hello/12/seasons/2019/day/1/skier/123" class="btn"
               target="_blank" rel="noopener noreferrer">
                Test doGet Lab3
            </a>
            <span class="tooltiptext">Get For Hello</span>
        </div>

        <!-- GitHub Icon Button with Tooltip -->
        <div class="github-container">
            <div class="tooltip">
                <a href="https://github.com/Tantantan-py/multi-thread" class="btn github-btn" target="_blank"
                   rel="noopener noreferrer">
                    <i class="fab fa-github"></i> GitHub
                </a>
                <span class="tooltiptext">Visit Lucian's GitHub Repository</span>
            </div>
        </div>
    </div>
</div>

<footer>
    &copy; <%= java.time.Year.now() %> Northeastern University Seattle - CS6650 Distributed Systems - Spring 2025 -
    Lucian
</footer>

<script>
    // Function to update the greeting based on the time of day
    function updateGreeting() {
        const hour = new Date().getHours();
        let greeting;

        if (hour < 12) {
            greeting = "Good morning, Husky!";
        } else if (hour < 18) {
            greeting = "Good afternoon, Husky!";
        } else {
            greeting = "Good evening, Husky!";
        }

        document.getElementById("greeting").textContent = greeting;
    }

    // Dark Mode Toggle Functionality
    const toggleBtn = document.getElementById('theme-toggle');
    const body = document.body;

    // Check for saved theme in localStorage
    const savedTheme = localStorage.getItem('theme') || 'light';
    body.setAttribute('data-theme', savedTheme);
    if (savedTheme === 'dark') {
        toggleBtn.innerHTML = '<i class="fas fa-sun"></i>';
    } else {
        toggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
    }

    // Event listener for toggle button
    toggleBtn.addEventListener('click', () => {
        const currentTheme = body.getAttribute('data-theme');
        if (currentTheme === 'light') {
            body.setAttribute('data-theme', 'dark');
            toggleBtn.innerHTML = '<i class="fas fa-sun"></i>';
            localStorage.setItem('theme', 'dark');
        } else {
            body.setAttribute('data-theme', 'light');
            toggleBtn.innerHTML = '<i class="fas fa-moon"></i>';
            localStorage.setItem('theme', 'light');
        }
    });

    // Initialize greeting on page load
    window.onload = updateGreeting;
</script>
</body>
</html>
