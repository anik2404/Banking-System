package com.banking.main;

import com.banking.model.Account;
import com.banking.model.Transaction;
import com.banking.service.BankingService;
import com.banking.util.DBConnection;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * Main — console entry point for the Banking System.
 *
 * Menu structure:
 *   [Not logged in]  1. Register  2. Login  3. ACID Demo  0. Exit
 *   [Logged in]      1. Balance   2. Deposit  3. Withdraw
 *                    4. Transfer  5. History  6. Statement 0. Logout
 */
public class Main {

    private static final BankingService service  = new BankingService();
    private static final Scanner        scanner  = new Scanner(System.in);
    private static       Account        loggedIn = null;

    public static void main(String[] args) {
        printBanner();

        boolean running = true;
        while (running) {
            if (loggedIn == null) {
                running = showGuestMenu();
            } else {
                running = showUserMenu();
            }
        }

        DBConnection.closeConnection();
        scanner.close();
        System.out.println("\n  Thank you for using Secure Bank. Goodbye!\n");
    }

    // ── Guest Menu ────────────────────────────────────────────────────────

    private static boolean showGuestMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║        SECURE BANK           ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  1. Create Account           ║");
        System.out.println("║  2. Login                    ║");
        System.out.println("║  3. ACID Concept Demo        ║");
        System.out.println("║  0. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("  Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": handleRegister(); break;
            case "2": handleLogin();    break;
            case "3": ACIDDemo.run();   break;
            case "0": return false;
            default:  System.out.println("  Invalid option.");
        }
        return true;
    }

    // ── User Menu ─────────────────────────────────────────────────────────

    private static boolean showUserMenu() {
        System.out.printf("%n╔══════════════════════════════════╗%n");
        System.out.printf("║  Welcome, %-22s║%n", loggedIn.getFullName());
        System.out.printf("║  A/C: %-27d║%n", loggedIn.getAccountId());
        System.out.println("╠══════════════════════════════════╣");
        System.out.println("║  1. Check Balance                ║");
        System.out.println("║  2. Deposit                      ║");
        System.out.println("║  3. Withdraw                     ║");
        System.out.println("║  4. Transfer                     ║");
        System.out.println("║  5. Last 10 Transactions         ║");
        System.out.println("║  6. Full Statement               ║");
        System.out.println("║  0. Logout                       ║");
        System.out.println("╚══════════════════════════════════╝");
        System.out.print("  Choice: ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1": handleBalance();       break;
            case "2": handleDeposit();       break;
            case "3": handleWithdraw();      break;
            case "4": handleTransfer();      break;
            case "5": handleHistory(10);     break;
            case "6": handleHistory(999);    break;
            case "0": loggedIn = null;
                      System.out.println("  Logged out successfully.");
                      break;
            default:  System.out.println("  Invalid option.");
        }
        return true;
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    private static void handleRegister() {
        System.out.println("\n  ── Create New Account ──");
        System.out.print  ("  Username    : "); String username = scanner.nextLine().trim();
        System.out.print  ("  Password    : "); String password = scanner.nextLine().trim();
        System.out.print  ("  Full Name   : "); String fullName = scanner.nextLine().trim();
        System.out.print  ("  Email       : "); String email    = scanner.nextLine().trim();
        System.out.println("  Account Type: 1. SAVINGS  2. CURRENT  3. FIXED");
        System.out.print  ("  Choice      : "); String typeInput = scanner.nextLine().trim();

        String accountType;
        switch (typeInput) {
            case "2":  accountType = "CURRENT"; break;
            case "3":  accountType = "FIXED";   break;
            default:   accountType = "SAVINGS"; break;
        }

        try {
            int newId = service.register(username, password, fullName, email, accountType);
            System.out.println("\n  ✔ Account created! Your Account ID: " + newId);
            System.out.println("  ✔ Account Type: " + accountType);
            System.out.println("  ✔ Please login to continue.");
        } catch (IllegalArgumentException e) {
            System.out.println("\n  ✘ Validation error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("\n  ✘ Database error: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.println("\n  ── Login ──");
        System.out.print("  Username: "); String username = scanner.nextLine().trim();
        System.out.print("  Password: "); String password = scanner.nextLine().trim();

        try {
            Account acc = service.login(username, password);
            if (acc != null) {
                loggedIn = acc;
                System.out.println("\n  ✔ Login successful! Welcome, " + acc.getFullName());
            } else {
                System.out.println("\n  ✘ Invalid username or password.");
            }
        } catch (SQLException e) {
            System.out.println("\n  ✘ Database error: " + e.getMessage());
        }
    }

    private static void handleBalance() {
        try {
            double balance = service.getBalance(loggedIn.getAccountId());
            System.out.printf("%n  ── Account Balance ──%n");
            System.out.printf("  Account ID   : %d%n",    loggedIn.getAccountId());
            System.out.printf("  Account Type : %s%n",    loggedIn.getAccountType());
            System.out.printf("  Balance      : ₹ %.2f%n", balance);
        } catch (SQLException e) {
            System.out.println("\n  ✘ Error: " + e.getMessage());
        }
    }

    private static void handleDeposit() {
        System.out.println("\n  ── Deposit ──");
        System.out.print("  Amount      : ₹ "); double amount = readDouble();
        System.out.print("  Description : ");   String desc   = scanner.nextLine().trim();

        try {
            service.deposit(loggedIn.getAccountId(), amount, desc);
            double newBal = service.getBalance(loggedIn.getAccountId());
            System.out.printf("  ✔ Deposit successful! New Balance: ₹ %.2f%n", newBal);
        } catch (Exception e) {
            System.out.println("\n  ✘ " + e.getMessage());
        }
    }

    private static void handleWithdraw() {
        System.out.println("\n  ── Withdraw ──");
        System.out.print("  Amount      : ₹ "); double amount = readDouble();
        System.out.print("  Description : ");   String desc   = scanner.nextLine().trim();

        try {
            service.withdraw(loggedIn.getAccountId(), amount, desc);
            double newBal = service.getBalance(loggedIn.getAccountId());
            System.out.printf("  ✔ Withdrawal successful! New Balance: ₹ %.2f%n", newBal);
        } catch (Exception e) {
            System.out.println("\n  ✘ " + e.getMessage());
        }
    }

    private static void handleTransfer() {
        System.out.println("\n  ── Transfer ──");
        System.out.print("  To Account ID : "); int toId     = readInt();
        System.out.print("  Amount        : ₹ "); double amount = readDouble();
        System.out.print("  Description   : ");  String desc  = scanner.nextLine().trim();

        try {
            service.transfer(loggedIn.getAccountId(), toId, amount, desc);
            double newBal = service.getBalance(loggedIn.getAccountId());
            System.out.printf("  ✔ Transfer successful! New Balance: ₹ %.2f%n", newBal);
        } catch (Exception e) {
            System.out.println("\n  ✘ " + e.getMessage());
        }
    }

    private static void handleHistory(int limit) {
        try {
            List<Transaction> history =
                (limit >= 999)
                ? service.getAllTransactions(loggedIn.getAccountId())
                : service.getTransactionHistory(loggedIn.getAccountId(), limit);

            System.out.println("\n  ── Transaction History ──");
            System.out.printf("  %-16s | %-14s | %10s | %14s | Description%n",
                              "Date/Time", "Type", "Amount", "Balance After");
            System.out.println("  " + "─".repeat(80));

            if (history.isEmpty()) {
                System.out.println("  No transactions found.");
            } else {
                for (Transaction t : history) {
                    System.out.printf("  %-16s | %-14s | %+10.2f | %14.2f | %s%n",
                        t.getTransactionDate().toString().substring(0, 16),
                        t.getTransactionType(),
                        (t.getTransactionType().contains("WITHDRAWAL") ||
                         t.getTransactionType().equals("TRANSFER_OUT")
                          ? -t.getAmount() : t.getAmount()),
                        t.getBalanceAfter(),
                        t.getDescription() != null ? t.getDescription() : "");
                }
            }

            System.out.println("  " + "─".repeat(80));
            System.out.printf("  Total Deposits   : ₹ %.2f%n",
                              service.getTotalDeposits(loggedIn.getAccountId()));
            System.out.printf("  Total Withdrawals: ₹ %.2f%n",
                              service.getTotalWithdrawals(loggedIn.getAccountId()));

        } catch (SQLException e) {
            System.out.println("\n  ✘ Error: " + e.getMessage());
        }
    }

    // ── I/O Helpers ───────────────────────────────────────────────────────

    private static double readDouble() {
        while (true) {
            try {
                double v = Double.parseDouble(scanner.nextLine().trim());
                if (v <= 0) { System.out.print("  Must be positive: ₹ "); continue; }
                return v;
            } catch (NumberFormatException e) {
                System.out.print("  Enter a valid number: ₹ ");
            }
        }
    }

    private static int readInt() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("  Enter a valid number: ");
            }
        }
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════╗");
        System.out.println("  ║       SECURE BANK — Java + Oracle 11g        ║");
        System.out.println("  ║     ACID-compliant Banking System v1.0       ║");
        System.out.println("  ╚══════════════════════════════════════════════╝");
    }
}