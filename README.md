# TCP/IP Server Project
## Overview

This project implements a basic TCP/IP server in Java that listens for incoming data on a specified port. Upon receiving a message containing a user ID from the client, the server retrieves all vehicles and insurance offers associated with that user from the "users" table in a relational database (PostgreSQL or any other). The server then sends this information back to the client as a response.

## Prerequisites
- Java Development Kit (JDK) installed
- PostgreSQL installed and running

## Usage
1. Create database using data from postgres.sql file.
2. Add db.properties to src/main/resources/ and configure the connection details:
```
jdbcUrl=jdbc:postgresql://localhost:5432/insurance_db
dbUsername={username}
dbPassword={password}
```

2. Start server application.
3. Start client application that connects to the server and sends a message containing a user ID.
4. Use console to enter user ID on client side.
5. Upon receiving the user ID, the server fetches relevant information from the database and sends it back to the client.
6. The client application prints the received information on the console.

