package com.banking.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DBConnection — Singleton JDBC connection manager for Oracle 11g.
 *
 * Change URL / USER / PASSWORD to match your Oracle instance.
 * Default Oracle 11g XE settings:
 *   URL  : jdbc:oracle:thin:@localhost:1521:XE
 *   USER : system  (or your schema user)
 *   PASS : your_password
 */
public class DBConnection {

    // ── Oracle 11g connection details ──────────────────────────────────────
    private static final String URL      = "jdbc:oracle:thin:@127.0.0.1:1521:XE";
    private static final String USER     = "system";
    private static final String PASSWORD = "newpassword";   // <-- change this
    // ───────────────────────────────────────────────────────────────────────

    private static Connection connection = null;

    // Private constructor — singleton, no instantiation
    private DBConnection() {}

    /**
     * Returns the single shared Connection.
     * Loads Oracle JDBC driver automatically via Class.forName().
     */
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                // Load Oracle Thin JDBC driver
                Class.forName("oracle.jdbc.driver.OracleDriver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                connection.setAutoCommit(true);   // default; DAO methods override for transactions
                System.out.println("[DB] Connection established.");
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "Oracle JDBC driver not found. " +
                "Add ojdbc6.jar to your project's Build Path.\n" + e.getMessage());
        }
        return connection;
    }

    /** Closes the connection (call on application exit). */
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("[DB] Connection closed.");
            } catch (SQLException e) {
                System.err.println("[DB] Error closing connection: " + e.getMessage());
            }
        }
    }
}