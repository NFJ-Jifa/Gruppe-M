# Energy Community Dashboard

Microservice-based application for collecting, processing, and visualizing energy production and consumption data within a community.

## 🎯 Overview

The solution comprises the following components:

1. **Producer Service**

    * Simulates community energy production (0–2 kWh) and publishes `EnergyMessage` to RabbitMQ.
    * Technologies: Spring Boot, RabbitMQ

2. **User Service**

    * Simulates consumer energy usage (0–2 kWh + peak bonuses), publishes `EnergyMessage` to RabbitMQ on a schedule.
    * Technologies: Spring Boot, RabbitMQ, Spring Scheduling

3. **Usage Service**

    * Persists all incoming `EnergyMessage` records into an   PostgreSQL database.
    * Technologies: Spring Boot, Spring Data JPA, RabbitMQ

4. **Percentage Service**

    * Calculates `gridPortion` and `communityDepleted` percentages from usage data and publishes `PercentageData` to RabbitMQ.
    * Technologies: Spring Boot, RabbitMQ

5. **Energy REST API**

    * Exposes endpoints:

        * `GET /energy/current` — current percentage status
        * `GET /energy/historical?start=…&end=…` — historical data range
        * `POST /energy/publish` — publish `PercentageData` manually
        * `GET /energy/available-range` — actual data availability window
    * Technologies: Spring Boot, Spring Web, Spring Data JPA

6. **JavaFX GUI**

    * Desktop application fetching data from the REST API, displays current and historical statistics in text and table views.
    * Technologies: Java 17, JavaFX, Java HTTP Client

## 🗂 Repository Structure

```text
.
├── producer-service
├── user-service
├── usage-service
├── percentage-service
├── energy-rest-api
└── energy-gui
```

Each module is a standalone Maven/Spring Boot or JavaFX project.

## 🔧 Prerequisites

* Java 17+
* Maven 3.6+
* Docker & Docker Compose
* RabbitMQ (default port: 5672)

## ▶ Local (IDE) Startup

1. **RabbitMQ**

   ```bash
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
   **database+RabbitMQ**
   docker compose up -d rabbitmq database

   ```

   UI: [http://localhost:15672](http://localhost:15672) (guest/guest)

2. **Run each service** in your IDE by launching its `*Application.main()`:

    * UserService 
    * ProducerService
    * UsageService
    * PercentageService
    * EnergyRestApiApplication

3. **Run the GUI**
   In the `energy-gui` module, launch `MainApplication`.


  # GUI is meant to run in IDE due to JavaFX
```

Launch everything:

```bash
docker-compose up --build
```

## 📚 References

* [Spring Boot](https://spring.io/projects/spring-boot)
* [RabbitMQ](https://www.rabbitmq.com/)
* [JavaFX](https://openjfx.io/)
* [TestFX](https://github.com/TestFX/TestFX)

---

*Author: Group M — June 2025*
