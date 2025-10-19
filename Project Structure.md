# Project Structure

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
