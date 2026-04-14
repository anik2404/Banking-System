package com.banking.service;

import com.banking.dao.AccountDAO;
import com.banking.dao.TransactionDAO;
import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.util.DBConnection;
import com.banking.util.InputValidator;
import com.banking.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BankingService {

    private final AccountDAO     accountDAO     = new AccountDAO();
    private final TransactionDAO transactionDAO = new TransactionDAO();

    public int register(String username, String password,
                        String fullName, String email,
                        String accountType) throws SQLException {
        if (!InputValidator.isValidUsername(username))
            throw new IllegalArgumentException("Username must be 3-50 alphanumeric characters.");
        if (!InputValidator.isValidPassword(password))
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        if (!InputValidator.isValidFullName(fullName))
            throw new IllegalArgumentException("Full name is required.");
        if (!InputValidator.isValidEmail(email))
            throw new IllegalArgumentException("Invalid email format.");
        if (accountDAO.usernameExists(username))
            throw new IllegalArgumentException("Username '" + username + "' is already taken.");

        Account account = new Account(
            username, PasswordUtil.hash(password),
            fullName, email, accountType);
        return accountDAO.createAccount(account);
    }

    public Account login(String username, String password) throws SQLException {
        return accountDAO.login(username, password);
    }

    public boolean deposit(int accountId, double amount, String description)
            throws SQLException {
        if (!InputValidator.isValidAmount(amount))
            throw new IllegalArgumentException("Invalid amount.");

        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            double balanceBefore = accountDAO.getBalance(accountId);
            double balanceAfter  = balanceBefore + amount;
            accountDAO.updateBalance(accountId, balanceAfter, conn);
            transactionDAO.recordTransaction(
                new Transaction(accountId, "DEPOSIT", amount,
                                balanceBefore, balanceAfter, description), conn);
            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean withdraw(int accountId, double amount, String description)
            throws SQLException {
        if (!InputValidator.isValidAmount(amount))
            throw new IllegalArgumentException("Invalid amount.");

        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            double balanceBefore = accountDAO.getBalance(accountId);
            if (amount > balanceBefore)
                throw new IllegalStateException(
                    String.format("Insufficient funds. Balance: %.2f, Requested: %.2f",
                                  balanceBefore, amount));
            double balanceAfter = balanceBefore - amount;
            accountDAO.updateBalance(accountId, balanceAfter, conn);
            transactionDAO.recordTransaction(
                new Transaction(accountId, "WITHDRAWAL", amount,
                                balanceBefore, balanceAfter, description), conn);
            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback();
            throw (e instanceof SQLException)
                ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean transfer(int fromId, int toId, double amount, String description)
            throws SQLException {
        if (fromId == toId)
            throw new IllegalArgumentException("Cannot transfer to same account.");
        if (!InputValidator.isValidAmount(amount))
            throw new IllegalArgumentException("Invalid amount.");

        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            double fromBefore = accountDAO.getBalance(fromId);
            double toBefore   = accountDAO.getBalance(toId);
            if (amount > fromBefore)
                throw new IllegalStateException("Insufficient funds.");

            double fromAfter = fromBefore - amount;
            double toAfter   = toBefore   + amount;

            accountDAO.updateBalance(fromId, fromAfter, conn);
            transactionDAO.recordTransaction(
                new Transaction(fromId, "TRANSFER_OUT", amount, fromBefore, fromAfter,
                                "Transfer to A/C " + toId), conn);

            accountDAO.updateBalance(toId, toAfter, conn);
            transactionDAO.recordTransaction(
                new Transaction(toId, "TRANSFER_IN", amount, toBefore, toAfter,
                                "Transfer from A/C " + fromId), conn);

            conn.commit();
            return true;
        } catch (Exception e) {
            conn.rollback();
            throw (e instanceof SQLException)
                ? (SQLException) e : new SQLException(e.getMessage(), e);
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public double getBalance(int accountId) throws SQLException {
        return accountDAO.getBalance(accountId);
    }

    public List<Transaction> getTransactionHistory(int accountId, int limit)
            throws SQLException {
        return transactionDAO.getHistory(accountId, limit);
    }

    public List<Transaction> getAllTransactions(int accountId) throws SQLException {
        return transactionDAO.getAllHistory(accountId);
    }

    public Account getAccount(int accountId) throws SQLException {
        return accountDAO.findById(accountId);
    }

    public double getTotalDeposits(int accountId) throws SQLException {
        return transactionDAO.getTotalDeposits(accountId);
    }

    public double getTotalWithdrawals(int accountId) throws SQLException {
        return transactionDAO.getTotalWithdrawals(accountId);
    }
}