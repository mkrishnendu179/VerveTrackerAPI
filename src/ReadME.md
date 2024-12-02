Here’s an updated version of the README file with explanations for how to handle the additional extensions:

---

# VerveService - High Performance REST API

This is a Java-based REST API that processes GET requests at a high throughput rate (10K requests per second), logs the number of unique requests received every minute, and sends a POST request to an external endpoint if provided.

## Features

- **High Throughput**: Can process up to 10K requests per second.
- **Unique Request Tracking**: Tracks unique requests based on the `id` query parameter.
- **Logging**: Logs the count of unique requests every minute.
- **External HTTP POST**: Sends a POST request to a specified external endpoint with the unique request count as a query parameter.

## Endpoints

### GET /api/verve/accept

**Parameters:**
- `id` (required) - An integer ID representing the request.
- `endpoint` (optional) - A string URL where the POST request will be sent with the unique request count.

**Response:**
- `200 OK` - Returns `"ok"` if the request is successfully processed.
- `500 Internal Server Error` - Returns `"failed"` in case of errors.

### Example Request

```bash
curl -X GET "http://localhost:8080/api/verve/accept?id=123&endpoint=http://example.com/notify"
```

### Example Response

```json
"ok"
```

---

## Application Features

1. **Request Handling**:
    - The service accepts GET requests at the `/api/verve/accept` endpoint.
    - The `id` query parameter is used to track unique requests. Requests with the same ID are considered the same, and only unique IDs are counted.

2. **Logging**:
    - Every minute, the application logs the number of unique requests received in the last 60 seconds.
    - The log is generated using a standard logger (SLF4J).

3. **HTTP POST Notification**:
    - If an optional `endpoint` query parameter is provided, the application will send a POST request to that endpoint with the count of unique requests as a query parameter.

4. **High Throughput**:
    - The application is designed to handle a large number of requests per second (10K+), using `ConcurrentHashMap` to track unique requests and for thread-safe counting.

---

## Setup

### Prerequisites

1. **Java 17 or later** - Ensure you have Java 17 or a later version installed.
2. **Maven** - To build and run the application.

### Installation

1. Clone the repository:

```bash
git clone https://github.com/------------------------------------------------------------------krishnendu---------------------------/verve-service.git
cd verve-service
```

2. Build the application using Maven:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn spring-boot:run
```

4. The service will start at `http://localhost:8080`.

---

## Configuration

You can modify the logging behavior and HTTP POST request behavior using the standard Spring Boot configuration files.

### Logging Configuration

The application uses Log4j2 for logging. The log file can be configured in the `src/main/resources/log4j2.xml` file.

### Scheduled Task Configuration

The scheduled task to log the unique request count every minute runs by default. You can adjust the frequency of the task by modifying the `@Scheduled(fixedRate = 60000)` annotation in the `VerveServiceApplication` class.

---

## Extensions

### Extension 2: Handling ID Deduplication in a Load-Balanced Environment

To ensure that the ID deduplication works even when the service is behind a Load Balancer and multiple instances of your application are handling requests simultaneously, we can use a **distributed cache** or **shared storage**. The solution can be implemented with a distributed caching mechanism like **Redis** or **Apache Kafka**.

**Solution:**
1. **Distributed Cache (Redis)**: Use Redis to store the unique request IDs. Redis is a fast, in-memory store that can be shared between all instances of the application. When a request is received, the application will check Redis to see if the ID is already present. If it is not, the ID is added to the cache and counted as unique.

    - Add Redis dependency in `pom.xml`:

    ```xml
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    ```

    - Modify the application to connect to Redis and store unique IDs:
    ```java
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping("/api/verve/accept")
    public String acceptRequest(@RequestParam int id, @RequestParam(required = false) String endpoint) {
        String idKey = "unique-request:" + id;

        if (!redisTemplate.hasKey(idKey)) {
            redisTemplate.opsForValue().set(idKey, "true", 1, TimeUnit.MINUTES);  // Set expiration to avoid stale data
            requestCount.incrementAndGet();
        }

        return "ok";
    }
    ```

2. **Message Queue (Apache Kafka)**: Alternatively, you could use Kafka to send messages containing unique request IDs. Each service instance would publish the ID to a Kafka topic, and a downstream consumer would deduplicate the IDs by storing them in a central data store (e.g., Redis, a database).

### Extension 3: Sending Unique ID Count to a Distributed Streaming Service

Instead of writing the count of unique received IDs to a log file, we can send this information to a distributed streaming service of your choice. **Apache Kafka**, **AWS Kinesis**, or **Google Cloud Pub/Sub** are some popular options for this.

**Solution:**
1. **Apache Kafka**: Kafka can be used to stream the unique request count to another service or system. You would need to publish the unique count to a Kafka topic at the end of each minute.

    - Add Kafka dependencies to `pom.xml`:

    ```xml
    <dependency>
        <groupId>org.springframework.kafka</groupId>
        <artifactId>spring-kafka</artifactId>
    </dependency>
    ```

    - Modify the scheduled task to send the unique request count to Kafka:

    ```java
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedRate = 60000)  // Run every minute
    public void logAndSendStats() {
        int uniqueRequestCount = uniqueRequestIds.size();
        logger.info("Unique requests in the last minute: {}", uniqueRequestCount);

        // Send to Kafka topic
        kafkaTemplate.send("unique-requests-topic", String.valueOf(uniqueRequestCount));
    }
    ```

2. **AWS Kinesis or Google Cloud Pub/Sub**: Similar to Kafka, you can integrate with cloud-based services like AWS Kinesis or Google Cloud Pub/Sub. Use their respective SDKs to send messages (e.g., the unique request count) to a stream or topic.

    - For example, with AWS Kinesis, you could use the AWS SDK to send data to a Kinesis stream instead of logging it.

---

## Example Log Output

Every minute, the application will log the number of unique requests:

```log
2024-12-02 12:00:01 INFO  VerveServiceApplication: Unique requests in the last minute: 150
```

If an HTTP POST request is made to an external endpoint, the status code of the response will be logged:

```log
2024-12-02 12:00:10 INFO  VerveServiceApplication: POST request to http://example.com/notify returned status code: 200
```

---

## Performance Considerations

- **Concurrency**: The application uses `ConcurrentHashMap` for storing unique request IDs and for safe, atomic request counting, ensuring high performance and scalability under load.
- **Distributed Caching/Streaming**: Redis, Kafka, and other distributed systems ensure consistency and deduplication when the service is scaled horizontally or deployed behind a load balancer.
- **Scheduling**: The unique request count is logged and sent as a POST request every minute using the `@Scheduled` annotation. This ensures that the logging and notification process is decoupled from request handling, minimizing overhead during peak traffic.

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---
