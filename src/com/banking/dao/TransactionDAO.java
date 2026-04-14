package com.banking.dao;

import com.banking.model.Transaction;
import com.banking.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionDAO — Data Access Object for the TRANSACTIONS table.
 *
 * Note: recordTransaction() accepts an explicit Connection parameter
 * so it can participate in the caller's ACID transaction
 * (managed by BankingService with setAutoCommit/commit/rollback).
 */
public class TransactionDAO {

    // ── Record a Transaction ──────────────────────────────────────────────

    /**
     * Inserts one transaction row using the supplied connection.
     * The connection's autoCommit is controlled by the service layer —
     * this method does NOT commit or rollback.
     */
    public void recordTransaction(Transaction txn, Connection conn) throws SQLException {
        String sql = "INSERT INTO transactions " +
                     "(account_id, transaction_type, amount, " +
                     " balance_before, balance_after, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, txn.getAccountId());
            ps.setString(2, txn.getTransactionType());
            ps.setDouble(3, txn.getAmount());
            ps.setDouble(4, txn.getBalanceBefore());
            ps.setDouble(5, txn.getBalanceAfter());
            ps.setString(6, txn.getDescription());
            ps.setString(7, txn.getStatus());
            ps.executeUpdate();
        }
    }

    // ── Get Transaction History ───────────────────────────────────────────

    /**
     * Returns up to `limit` most-recent transactions for the account.
     */
    public List<Transaction> getHistory(int accountId, int limit) throws SQLException {
        String sql = "SELECT * FROM " +
                     "(SELECT * FROM transactions WHERE account_id = ? " +
                     " ORDER BY transaction_date DESC) " +
                     "WHERE ROWNUM <= ?";

        List<Transaction> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Get Full History ──────────────────────────────────────────────────

    public List<Transaction> getAllHistory(int accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? " +
                     "ORDER BY transaction_date DESC";

        List<Transaction> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    // ── Total Deposits / Withdrawals ──────────────────────────────────────

    public double getTotalDeposits(int accountId) throws SQLException {
        return sumByType(accountId, "DEPOSIT");
    }

    public double getTotalWithdrawals(int accountId) throws SQLException {
        return sumByType(accountId, "WITHDRAWAL");
    }

    private double sumByType(int accountId, String type) throws SQLException {
        String sql = "SELECT NVL(SUM(amount),0) FROM transactions " +
                     "WHERE account_id = ? AND transaction_type = ? AND status = 'SUCCESS'";
        Connection conn = DBConnection.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt   (1, accountId);
            ps.setString(2, type);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        }
        return 0;
    }

    // ── Row Mapper ────────────────────────────────────────────────────────

    private Transaction mapRow(ResultSet rs) throws SQLException {
        Transaction txn = new Transaction();
        txn.setTransactionId  (rs.getInt      ("transaction_id"));
        txn.setAccountId      (rs.getInt      ("account_id"));
        txn.setTransactionType(rs.getString   ("transaction_type"));
        txn.setAmount         (rs.getDouble   ("amount"));
        txn.setBalanceBefore  (rs.getDouble   ("balance_before"));
        txn.setBalanceAfter   (rs.getDouble   ("balance_after"));
        txn.setDescription    (rs.getString   ("description"));
        txn.setTransactionDate(rs.getTimestamp("transaction_date"));
        txn.setStatus         (rs.getString   ("status"));
        return txn;
    }
}