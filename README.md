# Telco Platform

This is a Telco Platform application built with Spring Boot and Akka.

## Getting Started

To get started with the project, clone the repository and import it into your IDE.

## Prerequisites

- Java 17
- Gradle
- MongoDB

## Building the project

```bash
./gradlew build
```

## Running the application

```bash
./gradlew bootRun
```

## Project Structure
```
build.gradle                                -Dependency managment and build configuration

src/main/proto/orders.proto                 -Specifications for the GRPC functions

src/main/resources/log4j2.yml               -Loggin configuration

src/main/resources/application.yml          -General configuration and port specification

src/main/java/com/hacom/telco/service       -Source Files for Services

src/main/java/com/hacom/telco/repository    -Abstraction layer encapsulates the logic required to access data sources

src/main/java/com/hacom/telco/model         -Core objects that represent the data

src/main/java/com/hacom/telco/grpc          -GRPC API implementation

src/main/java/com/hacom/telco/dto           -Data Transfer Objects between different layers of the application

src/main/java/com/hacom/telco/config        -Setup environment-specific settings

src/main/java/com/hacom/telco/actor         -Akka actors for asynchronous order processing
```

## Database: (MongoDB)
-Defualt configuration in application.yml (change to match your environment accordingly)

    Default Database Name:
        mongodbDatabase: exampleDb

    Default URI:
        mongodbUri: "mongodb://127.0.0.1:27017"


## SMPP Server configuration:
Set up SMPP service for sending customer SMS notifications when orders are processed

-Replace the Default SMPP service, which is configured in application.yml as followed:
        
    smpp:
      host: localhost
      port: 2775
      systemId: smppclient1
      password: password


## GRPC End Points

* Note: The Default GRPC port is 9898 as defined in application.yml (apiPort), you may change it if needed.

### CreateOrder:
  This endpoint is used to create an order
```
    Example:
        grpcurl -plaintext       -d '{
        "orderId": "ORD-018",
        "customerId": "CUST-11007",
        "customerPhoneNumber": "+18295282089",
        "items": ["5G Router", "Unlimited Data Plan"]
      }'       localhost:9898 com.hacom.telco.grpc.OrderService/CreateOrder
        
    Returns:
        {
          "orderId": "ORD-018",
          "status": "PROCESSED"
        }
```
  Note: When the order is processed an SMS is sent to the specified customerPhoneNumber, the message is similar to "Your order ORD-018 has been processed"

 Required Fields: 

    * orderId
    * customerId
    * customerPhoneNumber
    * items



### GetOrdersByDateRange:
  This endpoint returns all orders created between dates "start_date" and "end_date"

* Note: Time Zone being used is UTC (Coordinated Universal Time)
```
    Exmaple:
        grpcurl -plaintext -d '{
          "start_date": "2025-10-19T16:00:00Z",
          "end_date": "2025-10-19T18:00:00Z" 
        }' localhost:9898 com.hacom.telco.grpc.OrderService/GetOrdersByDateRange

    Returns:
         {
          "orders": [
           {
              "id": "68f514e2bcd86a51fdd8f5ea",
              "orderId": "ORD-017",
              "customerId": "CUST-11001",
              "customerPhoneNumber": "+18295282089",
              "status": "PROCESSED",
              "items": [
                "eSIM",
                "SmartPhone"
              ],
              "ts": "2025-10-19T16:42:09.768Z"
            },
            {
              "id": "68f5190494f13f77562e1acc",
              "orderId": "ORD-018",
              "customerId": "CUST-11007",
              "customerPhoneNumber": "+18295282089",
              "status": "PROCESSED",
              "items": [
                "5G Router",
                "Unlimited Data Plan"
              ],
              "ts": "2025-10-19T16:59:48.083Z"
            }
          ]
        }
```


### GetOrderStatus:
  retrieves the status of a specific order (by orderId)
```
    Example:
    grpcurl -plaintext -d '{"orderId": "ORD-018"}' localhost:9898 com.hacom.telco.grpc.OrderService/GetOrderStatus

    Returns:
        {
          "orderId": "ORD-018",
          "status": "PROCESSED"
        }
```


## Metrics: (Promethius)
This metric is used indicates the total amount of orders processed by the api

- Type: Counter
- Name: telco_orders_processed_total
- Configuration: Promethius default port is configured in application.yml to be 8080 (default), feel free to change it if desired

      Example: 
            http://localhost:8080/actuator/prometheus




