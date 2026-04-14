package com.banking.model;

import java.sql.Timestamp;

/**
 * Account — plain Java model matching the ACCOUNTS table.
 */
public class Account {

    private int       accountId;
    private String    username;
    private String    passwordHash;
    private String    fullName;
    private String    email;
    private double    balance;
    private String    accountType;
    private boolean   active;
    private Timestamp createdAt;
    private Timestamp lastLogin;

    // ── Constructors ──────────────────────────────────────────────────────

    public Account() {}

    // Used when creating a new account (before DB insert)
    public Account(String username, String passwordHash,
                   String fullName, String email, String accountType) {
        this.username     = username;
        this.passwordHash = passwordHash;
        this.fullName     = fullName;
        this.email        = email;
        this.balance      = 0.00;
        this.accountType  = accountType;
        this.active       = true;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────

    public int       getAccountId()    { return accountId; }
    public void      setAccountId(int v){ this.accountId = v; }

    public String    getUsername()     { return username; }
    public void      setUsername(String v){ this.username = v; }

    public String    getPasswordHash() { return passwordHash; }
    public void      setPasswordHash(String v){ this.passwordHash = v; }

    public String    getFullName()     { return fullName; }
    public void      setFullName(String v){ this.fullName = v; }

    public String    getEmail()        { return email; }
    public void      setEmail(String v){ this.email = v; }

    public double    getBalance()      { return balance; }
    public void      setBalance(double v){ this.balance = v; }

    public String    getAccountType()  { return accountType; }
    public void      setAccountType(String v){ this.accountType = v; }

    public boolean   isActive()        { return active; }
    public void      setActive(boolean v){ this.active = v; }

    public Timestamp getCreatedAt()    { return createdAt; }
    public void      setCreatedAt(Timestamp v){ this.createdAt = v; }

    public Timestamp getLastLogin()    { return lastLogin; }
    public void      setLastLogin(Timestamp v){ this.lastLogin = v; }

    @Override
    public String toString() {
        return String.format(
            "Account[id=%d, user=%s, name=%s, type=%s, balance=%.2f]",
            accountId, username, fullName, accountType, balance);
    }
}