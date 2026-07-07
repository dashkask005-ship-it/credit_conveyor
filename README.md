# Email Microservice

A robust and scalable email microservice built with Spring Boot that provides asynchronous email sending capabilities through RabbitMQ integration and persistent email tracking.

## Features

- **Asynchronous Email Processing**: Leverages RabbitMQ for message queuing and non-blocking email operations
- **Email Tracking**: Comprehensive logging and status tracking for all sent emails
- **RESTful API**: Clean REST endpoints for email management and monitoring
- **Database Persistence**: Email records stored in MySQL with full audit trail
- **Validation**: Robust input validation for email data
- **Pagination Support**: Efficient data retrieval with pagination capabilities
- **Error Handling**: Comprehensive error handling with detailed status reporting

## Technology Stack

- **Framework**: Spring Boot 3.5.3
- **Language**: Java 17
- **Database**: MySQL
- **Message Broker**: RabbitMQ
- **Email**: Spring Mail (SMTP)
- **Validation**: Spring Validation
- **ORM**: Spring Data JPA
- **Build Tool**: Maven
- **Additional**: Lombok, Spring AMQP

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+
- RabbitMQ server
- Gmail account with App Password (for SMTP)

## Installation and Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd email-microservice
```

### 2. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE `ms-email`;
```

### 3. Environment Configuration
Create a `.env` file in the root directory with the following variables:
```env
MYSQL_HOST=localhost
SPRING_MAIL_USERNAME=your-email@gmail.com
SPRING_MAIL_PASSWORD=your-app-password
SPRING_RABBITMQ_ADDRESSES=amqp://username:password@host:port/vhost
```

### 4. Gmail App Password Setup
1. Enable 2-Factor Authentication on your Gmail account
2. Generate an App Password: https://support.google.com/accounts/answer/185833
3. Use the generated App Password in your environment configuration

### 5. RabbitMQ Setup
Set up a RabbitMQ instance:
- **Local**: Install RabbitMQ locally
- **Cloud**: Use CloudAMQP (https://www.cloudamqp.com/) for managed RabbitMQ

### 6. Build and Run
```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## API Endpoints

### Send Email
```http
POST /sending-email
Content-Type: application/json

{
    "ownerRef": "user123",
    "emailFrom": "sender@example.com",
    "emailTo": "recipient@example.com",
    "subject": "Test Email",
    "text": "This is a test email message."
}
```

### Get All Emails (Paginated)
```http
GET /emails?page=0&size=10&sort=emailId,desc
```

### Get All Emails (Non-Paginated)
```http
GET /emails/all
```

### Get Email by ID
```http
GET /emails/{emailId}
```

## Response Examples

### Successful Email Send
```json
{
    "emailId": "123e4567-e89b-12d3-a456-426614174000",
    "ownerRef": "user123",
    "emailFrom": "sender@example.com",
    "emailTo": "recipient@example.com",
    "subject": "Test Email",
    "text": "This is a test email message.",
    "sendDateEmail": "2024-01-15T10:30:00",
    "statusEmail": "SENT"
}
```

### Email List Response
```json
{
    "content": [
        {
            "emailId": "123e4567-e89b-12d3-a456-426614174000",
            "ownerRef": "user123",
            "emailFrom": "sender@example.com",
            "emailTo": "recipient@example.com",
            "subject": "Test Email",
            "text": "This is a test email message.",
            "sendDateEmail": "2024-01-15T10:30:00",
            "statusEmail": "SENT"
        }
    ],
    "pageable": {
        "sort": {
            "sorted": true,
            "unsorted": false
        },
        "pageNumber": 0,
        "pageSize": 5
    },
    "totalElements": 1,
    "totalPages": 1,
    "last": true,
    "first": true
}
```

## Architecture

The application follows a clean architecture pattern:

```
├── configs/          # Configuration classes
├── controllers/      # REST API controllers
├── consumers/        # RabbitMQ message consumers
├── dtos/            # Data Transfer Objects
├── enums/           # Enumeration classes
├── models/          # JPA Entity classes
├── repositories/    # Data access layer
└── services/        # Business logic layer
```

## Message Flow

1. **Direct API Call**: Client sends email via REST endpoint
2. **Queue Processing**: Email messages consumed from RabbitMQ queue
3. **Email Sending**: SMTP service processes and sends emails
4. **Status Tracking**: Results stored in database with status updates

## Email Status

- **SENT**: Email successfully delivered
- **ERROR**: Email failed to send (network issues, invalid credentials, etc.)

## Configuration

### Application Properties
The application uses the following key configuration properties:

```properties
# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://${MYSQL_HOST:localhost}:3306/ms-email
spring.datasource.username=root
spring.datasource.password=senha123@

# JPA Configuration
spring.jpa.hibernate.ddl-auto=update

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${SPRING_MAIL_USERNAME}
spring.mail.password=${SPRING_MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# RabbitMQ Configuration
spring.rabbitmq.addresses=${SPRING_RABBITMQ_ADDRESSES}
spring.rabbitmq.queue=ms.email
```

## Security Considerations

- Use App Passwords instead of regular Gmail passwords
- Store sensitive configuration in environment variables

### Environment Variables
Ensure all required environment variables are set in your production environment:
- `MYSQL_HOST`
- `SPRING_MAIL_USERNAME`
- `SPRING_MAIL_PASSWORD`
- `SPRING_RABBITMQ_ADDRESSES`

### Database Migration
The application uses `spring.jpa.hibernate.ddl-auto=update` which automatically creates and updates database tables based on entity changes.
