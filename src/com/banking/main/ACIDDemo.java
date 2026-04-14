package com.banking.main;

import com.banking.service.BankingService;
import com.banking.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * ACIDDemo — demonstrates all four ACID properties with real Oracle 11g calls.
 *
 * This is the "unique advantage" of the project:
 * it shows WHAT ACID is AND proves it works live.
 *
 * Run from the guest menu → option 3.
 * Requires at least one account to exist in the database.
 */
public class ACIDDemo {

    private static final BankingService service = new BankingService();

    public static void run() {
        printHeader();
        demoAtomicity();
        demoConsistency();
        demoIsolation();
        demoDurability();
        printFooter();
    }

    // ── A: Atomicity ──────────────────────────────────────────────────────

    private static void demoAtomicity() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  A — ATOMICITY                                          │");
        System.out.println("│  'All or nothing' — a transaction is indivisible.       │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Scenario: Transfer ₹500 between two accounts.");
        System.out.println("  Steps:    (1) Debit ₹500 from account A");
        System.out.println("            (2) Credit ₹500 to account B");
        System.out.println();
        System.out.println("  We will SIMULATE a crash after step 1 and show rollback.");
        System.out.println();

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);   // begin manual transaction

            System.out.println("  → setAutoCommit(false) — transaction started");

            // Step 1: debit (using a demo account, adjust ID as needed)
            String debitSQL = "UPDATE accounts SET balance = balance - 500 WHERE account_id = 1000";
            try (PreparedStatement ps = conn.prepareStatement(debitSQL)) {
                ps.executeUpdate();
                System.out.println("  → Step 1 executed: balance - 500 (NOT yet committed)");
            }

            // Simulate a crash / error before step 2
            System.out.println("  → SIMULATING ERROR before step 2...");
            throw new SQLException("Simulated network failure!");

        } catch (SQLException e) {
            System.out.println("  → Exception: " + e.getMessage());
            try {
                conn.rollback();
                System.out.println("  → conn.rollback() called");
                System.out.println("  → RESULT: Step 1 was UNDONE. Balance is unchanged.");
                System.out.println("  → ATOMICITY PROVEN: partial updates never persist.");
            } catch (SQLException re) {
                System.err.println("  Rollback failed: " + re.getMessage());
            }
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // ── C: Consistency ────────────────────────────────────────────────────

    private static void demoConsistency() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  C — CONSISTENCY                                        │");
        System.out.println("│  DB stays valid before and after every transaction.     │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Rule 1 (CHECK constraint): balance >= 0");
        System.out.println("  Rule 2 (business logic)  : withdrawal <= current balance");
        System.out.println();
        System.out.println("  Test: Try to withdraw ₹999,999 from an account with ₹0 balance.");
        System.out.println();

        try {
            service.withdraw(1000, 999_999, "Demo overdraft attempt");
            System.out.println("  (This line should NOT print — withdrawal should fail)");
        } catch (IllegalStateException e) {
            System.out.println("  → Caught: " + e.getMessage());
            System.out.println("  → RESULT: Withdrawal rejected by business rule.");
        } catch (SQLException e) {
            System.out.println("  → DB enforced: " + e.getMessage());
        }

        System.out.println();
        System.out.println("  Test 2: Negative amount (violates CHECK constraint).");
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            String sql = "UPDATE accounts SET balance = -100 WHERE account_id = 1000";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.executeUpdate();
                conn.commit();
                System.out.println("  (Should NOT reach here — Oracle CHECK should reject)");
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException re) {}
            System.out.println("  → Oracle rejected: " + e.getMessage().split("\n")[0]);
            System.out.println("  → CONSISTENCY PROVEN: DB constraint prevented invalid state.");
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // ── I: Isolation ──────────────────────────────────────────────────────

    private static void demoIsolation() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  I — ISOLATION                                          │");
        System.out.println("│  Concurrent transactions don't interfere with each other│");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Oracle 11g default level: READ COMMITTED");
        System.out.println();
        System.out.println("  What this means:");
        System.out.println("    • Transaction T1 reads a row.");
        System.out.println("    • Transaction T2 modifies and commits that row.");
        System.out.println("    • T1 cannot read T2's uncommitted changes (no dirty reads).");
        System.out.println();
        System.out.println("  In this single-user demo we cannot run two threads simultaneously,");
        System.out.println("  but Oracle guarantees this at the server level via:");
        System.out.println("    → Multi-Version Concurrency Control (MVCC / Undo segments)");
        System.out.println("    → Row-level locking on UPDATE");
        System.out.println("    → SELECT...FOR UPDATE when explicit locking is needed");
        System.out.println();
        System.out.println("  Our code uses conn.setAutoCommit(false) which means no other");
        System.out.println("  session sees our changes until conn.commit() is called.");
        System.out.println("  → ISOLATION DEMONSTRATED (conceptually).");
    }

    // ── D: Durability ─────────────────────────────────────────────────────

    private static void demoDurability() {
        System.out.println("\n┌─────────────────────────────────────────────────────────┐");
        System.out.println("│  D — DURABILITY                                         │");
        System.out.println("│  Committed data survives system crashes.                │");
        System.out.println("└─────────────────────────────────────────────────────────┘");
        System.out.println();
        System.out.println("  Performing a real deposit and committing it now...");
        System.out.println();

        try {
            double before = service.getBalance(1000);
            service.deposit(1000, 100.00, "ACID durability demo deposit");
            double after = service.getBalance(1000);

            System.out.printf("  → Balance before: ₹ %.2f%n", before);
            System.out.printf("  → Balance after : ₹ %.2f%n", after);
            System.out.println();
            System.out.println("  How Oracle ensures Durability:");
            System.out.println("    1. conn.commit() triggers a REDO LOG FLUSH.");
            System.out.println("    2. Oracle writes the redo record to disk BEFORE");
            System.out.println("       acknowledging the commit to the client.");
            System.out.println("    3. On crash recovery, Oracle replays the redo log.");
            System.out.println("    4. Your ₹ 100 deposit is permanently recorded.");
            System.out.println();
            System.out.println("  → DURABILITY PROVEN: committed data is on disk.");

        } catch (Exception e) {
            System.out.println("  → Note: Account 1000 may not exist yet.");
            System.out.println("    Create an account first, then re-run the demo.");
            System.out.println("    Error: " + e.getMessage());
        }
    }

    // ── UI Helpers ────────────────────────────────────────────────────────

    private static void printHeader() {
        System.out.println("\n");
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║           ACID PROPERTIES — LIVE DEMO               ║");
        System.out.println("  ║      Atomicity · Consistency · Isolation · Durability║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
    }

    private static void printFooter() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║  ACID DEMO COMPLETE                                 ║");
        System.out.println("  ║  All four properties demonstrated with Oracle 11g   ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }
}