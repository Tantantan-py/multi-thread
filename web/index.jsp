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
  <meta charset="UTF-8" />
  <title>CS6650 Lab</title>

  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link
          href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;700&display=swap"
          rel="stylesheet">

  <style>
    * {
      margin: 0;
      padding: 0;
      box-sizing: border-box;
    }

    html, body {
      font-family: 'Montserrat', sans-serif;
      height: 100%;
      background: #f3f3f3;
      color: #333;
    }

    header {
      background: linear-gradient(135deg, #0f9b0f, #00a400, #0f9b0f);
      background-size: 300% 300%;
      animation: gradientBG 10s ease infinite;
      text-align: center;
      padding: 40px 20px;
      color: #fff;
      margin-bottom: 20px;
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

    .container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
      background: #fff;
      border-radius: 10px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .intro {
      text-align: center;
      margin-bottom: 20px;
    }

    .intro p {
      margin: 10px 0 20px;
      line-height: 1.6;
    }

    .btn {
      display: inline-block;
      padding: 12px 24px;
      margin: 5px;
      border-radius: 25px;
      border: none;
      font-size: 1rem;
      font-weight: 600;
      text-transform: uppercase;
      color: #fff;
      background: #0f9b0f;
      cursor: pointer;
      transition: background 0.3s ease;
      text-decoration: none;
    }

    .btn:hover {
      background: #0b7f0b;
    }

    footer {
      text-align: center;
      margin-top: 20px;
      color: #777;
      font-size: 0.9rem;
    }
  </style>
</head>

<body>
<header>
  <h1>Skiers Setup</h1>
  <p id="greeting" style="font-size:1.2rem; margin-top: 10px;"></p>
</header>

<div class="container">
  <div class="intro">
    <p>
      Welcome to the Skiers setup page!
    </p>
    <p>No worries, I'm just taking the Web Dev course to make this fancier.</p>
    <a href="#" class="btn">Button 1</a>
    <a href="#" class="btn">Button 2</a>
  </div>
</div>

<footer>
  &copy; <%= java.time.Year.now() %> Northeastern University Seattle - CS6650 Distributed Systems - Spring 2025 - Lucian
</footer>

<script>
  function updateGreeting() {
    const hour = new Date().getHours();
    let greeting;

    if (hour < 12) {
      greeting = "Good morning, Skiers!";
    } else if (hour < 18) {
      greeting = "Good afternoon, Skiers!";
    } else {
      greeting = "Good evening, Skiers!";
    }

    document.getElementById("greeting").textContent = greeting;
  }

  window.onload = updateGreeting;
</script>
</body>
</html>

