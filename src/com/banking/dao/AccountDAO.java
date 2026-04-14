package com.banking.dao;

import com.banking.model.Account;
import com.banking.util.DBConnection;
import com.banking.util.PasswordUtil;

import java.sql.*;

/**
 * AccountDAO — Data Access Object for the ACCOUNTS table.
 *
 * Every public method opens a PreparedStatement, executes it,
 * and closes resources in a finally block to prevent leaks.
 */
public class AccountDAO {

    // ── Create Account ────────────────────────────────────────────────────

    /**
     * Inserts a new account row and returns the generated account_id.
     * The balance CHECK constraint (>= 0) and UNIQUE (username) are
     * enforced by Oracle — this method propagates any SQL violations.
     */
    public int createAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts " +
                     "(username, password_hash, full_name, email, balance, account_type) " +
                     "VALUES (?, ?, ?, ?, 0, ?) ";

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet generatedKeys = null;

        try {
            conn = DBConnection.getConnection();

            // Use RETURN_GENERATED_KEYS to fetch the sequence-assigned account_id
            ps = conn.prepareStatement(sql, new String[]{"ACCOUNT_ID"});
            ps.setString(1, account.getUsername());
            ps.setString(2, account.getPasswordHash());
            ps.setString(3, account.getFullName());
            ps.setString(4, account.getEmail());
            ps.setString(5, account.getAccountType());

            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Account creation failed — no rows inserted.");

            generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            }
            throw new SQLException("Account created but no account_id returned.");

        } finally {
            closeQuietly(generatedKeys);
            closeQuietly(ps);
        }
    }

    // ── Login ─────────────────────────────────────────────────────────────

    /**
     * Validates credentials and returns the Account, or null if invalid.
     * Also updates last_login timestamp on success.
     */
    public Account login(String username, String plainPassword) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE username = ? AND is_active = 1";

        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password_hash");
                    if (PasswordUtil.verify(plainPassword, storedHash)) {
                        Account acc = mapRow(rs);
                        updateLastLogin(acc.getAccountId());
                        return acc;
                    }
                }
            }
        }
        return null; // invalid credentials
    }

    // ── Find by ID ────────────────────────────────────────────────────────

    public Account findById(int accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── Update Balance ────────────────────────────────────────────────────

    /**
     * Updates the balance column for the given account.
     * Called inside a larger transaction from BankingService —
     * the connection's autoCommit is managed by the service layer.
     */
    public boolean updateBalance(int accountId, double newBalance, Connection conn)
            throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        }
    }

    // ── Get Balance ───────────────────────────────────────────────────────

    public double getBalance(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("balance");
            }
        }
        throw new SQLException("Account not found: " + accountId);
    }

    // ── Check Username Exists ─────────────────────────────────────────────

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM accounts WHERE username = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ── Update Last Login ─────────────────────────────────────────────────

    private void updateLastLogin(int accountId) throws SQLException {
        String sql = "UPDATE accounts SET last_login = SYSTIMESTAMP WHERE account_id = ?";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        }
    }

    // ── Row Mapper ────────────────────────────────────────────────────────

    private Account mapRow(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setAccountId(rs.getInt("account_id"));
        acc.setUsername(rs.getString("username"));
        acc.setPasswordHash(rs.getString("password_hash"));
        acc.setFullName(rs.getString("full_name"));
        acc.setEmail(rs.getString("email"));
        acc.setBalance(rs.getDouble("balance"));
        acc.setAccountType(rs.getString("account_type"));
        acc.setActive(rs.getInt("is_active") == 1);
        acc.setCreatedAt(rs.getTimestamp("created_at"));
        acc.setLastLogin(rs.getTimestamp("last_login"));
        return acc;
    }

    // ── Resource Helpers ──────────────────────────────────────────────────

    private void closeQuietly(AutoCloseable c) {
        if (c != null) try { c.close(); } catch (Exception e) { /* ignore */ }
    }
}