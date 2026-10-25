# Java Order Service

A Spring Boot microservice responsible for creating, retrieving, and managing customer orders in the e-commerce application.

## 1. Technology Stack

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Maven
- Docker
- REST APIs

## 2. Application Architecture

```text
Node.js E-commerce Application
              |
              | POST /orders
              v
      Java Order Service
        Port: 8002
              |
              +--------------------+
              |                    |
              v                    v
      Python Product Service   PostgreSQL
         Port: 8001             order_db
              |
              v
         product_db
```

The Order Service retrieves product information from the Product Service, validates product availability and quantity, calculates the order total, and saves the order and order items in PostgreSQL.

## 3. Build the Docker Image

Navigate to the project root directory:

```bash
cd ~/java-order-service
```

Build the Docker image:

```bash
docker build -t java-order-service:1.2 .
```

Verify the image:

```bash
docker images
```

## 4. Database Configuration

PostgreSQL runs on the Windows host, and the Order Service runs inside a Docker container.

The application connects to PostgreSQL using `host.docker.internal`.

Example `application.properties` configuration:

```properties
spring.application.name=java-order-service
server.port=8002

spring.datasource.url=jdbc:postgresql://host.docker.internal:5432/order_db
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

product.service.url=http://host.docker.internal:8001
```

**Security:** Supply `DB_PASSWORD` when starting the container. Never hardcode or commit the actual database password.

Ensure PostgreSQL is running and the `order_db` database exists before starting the application.

## 5. Run the Docker Container

If a container with this name already exists, remove it first if you intend to recreate it:

```bash
docker rm -f java-order-service
```

In Git Bash, enter the PostgreSQL password without displaying it:

```bash
read -s -p "Enter PostgreSQL password: " DB_PASSWORD
echo
```

Start the container:

```bash
docker run -d \
  --name java-order-service \
  -p 8002:8002 \
  -e DB_PASSWORD="$DB_PASSWORD" \
  java-order-service:1.2
```

Clear the temporary shell variable:

```bash
unset DB_PASSWORD
```

## 6. Verify the Application

Check the container status:

```bash
docker ps
```

View application logs:

```bash
docker logs -f java-order-service
```

Check the health endpoint:

```bash
curl -i http://localhost:8002/actuator/health
```

Expected result: HTTP `200 OK`, with the application and database reporting healthy status.

## 7. REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/orders` | Create a new order |
| GET | `/orders` | Retrieve all orders |
| GET | `/orders/{id}` | Retrieve an order by ID |
| GET | `/actuator/health` | Check application health |

### Example: Create an Order

```bash
curl -i -X POST http://localhost:8002/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerEmail": "student@example.com",
    "items": [
      {"productId": 3, "quantity": 2},
      {"productId": 4, "quantity": 1}
    ]
  }'
```

The Order Service retrieves the product prices from the Product Service and calculates the order total.

For the example above:

- Keyboard: 2 × ₹2,500 = ₹5,000
- Wireless Mouse: 1 × ₹1,200 = ₹1,200
- **Total order amount: ₹6,200**

The service returns the created order, including its ID, status, total amount, and item details.

## 8. Useful Docker Commands

```bash
# List running containers
docker ps

# View logs
docker logs java-order-service

# Stop the service
docker stop java-order-service

# Start the existing container
docker start java-order-service

# Remove the container
docker rm -f java-order-service
```

## 9. Important Notes

- PostgreSQL stores orders in `order_db`.
- The Python Product Service manages product information in `product_db`.
- Both services must be running and reachable for successful order creation.
- The Order Service currently validates available stock but does not decrement or reserve inventory.
- This is a learning project; production deployments should also implement stronger inventory consistency, authentication, authorization, and secret management.

