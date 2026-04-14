# Secure Bank — Java Banking System

A console-based banking application built with Core Java and Oracle 11g database. Supports account creation, login, deposit, withdrawal, fund transfer, and transaction history. Features a live ACID properties demonstration with real database calls.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| Java SE 8 | Core application logic |
| Oracle 11g XE | Database |
| JDBC (ojdbc6.jar) | Java to Oracle connectivity |
| SHA-256 | Password hashing |
| Eclipse IDE | Development environment |

---

## Prerequisites

- Oracle 11g XE installed and running on localhost
- Eclipse IDE (any edition)
- JDK 8
- ojdbc6.jar from `C:\oraclexe\app\oracle\product\11.2.0\server\jdbc\lib\`

---

## Setup

**1. Start Oracle**
```
net start OracleServiceXE
lsnrctl start
```

**2. Run the schema** in SQL*Plus or SQL Developer
```
sqlplus system/your_password@127.0.0.1:1521/XE
```
Then execute `schema.sql`.

**3. Update DBConnection.java**
```java
private static final String URL      = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
private static final String USER     = "system";
private static final String PASSWORD = "your_password";
```

**4. Add ojdbc6.jar to Eclipse Build Path**
Right-click `ojdbc6.jar` → Build Path → Add to Build Path

**5. Set compiler to Java 1.8**
Right-click project → Properties → Java Compiler → set to 1.8

---

## Run

Right-click `Main.java` → Run As → Java Application

---

## Features

| Option | Description |
|---|---|
| 1. Create Account | Register with username, password, name, email |
| 2. Login | Secure login with SHA-256 password check |
| 3. Deposit | Add funds to account |
| 4. Withdraw | Withdraw with balance validation |
| 5. Transfer | Transfer funds between accounts (ACID) |
| 6. History | View last 10 transactions |
| 7. Statement | View full transaction log with totals |
| 8. ACID Demo | Live demonstration of all 4 ACID properties |
