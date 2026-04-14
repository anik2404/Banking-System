package com.banking.model;

import java.sql.Timestamp;

/**
 * Transaction — plain Java model matching the TRANSACTIONS table.
 */
public class Transaction {

    private int       transactionId;
    private int       accountId;
    private String    transactionType;
    private double    amount;
    private double    balanceBefore;
    private double    balanceAfter;
    private String    description;
    private Timestamp transactionDate;
    private String    status;

    // ── Constructors ──────────────────────────────────────────────────────

    public Transaction() {}

    public Transaction(int accountId, String transactionType,
                       double amount, double balanceBefore,
                       double balanceAfter, String description) {
        this.accountId       = accountId;
        this.transactionType = transactionType;
        this.amount          = amount;
        this.balanceBefore   = balanceBefore;
        this.balanceAfter    = balanceAfter;
        this.description     = description;
        this.status          = "SUCCESS";
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int       getTransactionId()          { return transactionId; }
    public void      setTransactionId(int v)      { this.transactionId = v; }

    public int       getAccountId()               { return accountId; }
    public void      setAccountId(int v)           { this.accountId = v; }

    public String    getTransactionType()          { return transactionType; }
    public void      setTransactionType(String v)  { this.transactionType = v; }

    public double    getAmount()                   { return amount; }
    public void      setAmount(double v)            { this.amount = v; }

    public double    getBalanceBefore()            { return balanceBefore; }
    public void      setBalanceBefore(double v)     { this.balanceBefore = v; }

    public double    getBalanceAfter()             { return balanceAfter; }
    public void      setBalanceAfter(double v)      { this.balanceAfter = v; }

    public String    getDescription()              { return description; }
    public void      setDescription(String v)       { this.description = v; }

    public Timestamp getTransactionDate()          { return transactionDate; }
    public void      setTransactionDate(Timestamp v){ this.transactionDate = v; }

    public String    getStatus()                   { return status; }
    public void      setStatus(String v)            { this.status = v; }

    @Override
    public String toString() {
        return String.format(
            "%-16s | %-10s | %+10.2f | Balance: %10.2f | %s",
            transactionDate != null ? transactionDate.toString().substring(0, 16) : "N/A",
            transactionType, amount, balanceAfter,
            description != null ? description : "");
    }
}