package se.enterprise.fikavault.entity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe domain entity representing an employee's ledger account and balances.
 */
public class EmployeeAccount {

    private final String employeeId;
    private final String fullName;
    private final AtomicInteger balance;
    private final AtomicInteger totalSent;
    private final AtomicInteger totalReceived;
    private final AtomicInteger vouchersRedeemed;

    public EmployeeAccount(String employeeId, String fullName, int initialBalance) {
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.balance = new AtomicInteger(initialBalance);
        this.totalSent = new AtomicInteger(0);
        this.totalReceived = new AtomicInteger(0);
        this.vouchersRedeemed = new AtomicInteger(0);
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public String getFullName() {
        return fullName;
    }

    public int getBalance() {
        return balance.get();
    }

    public int getTotalSent() {
        return totalSent.get();
    }

    public int getTotalReceived() {
        return totalReceived.get();
    }

    public int getVouchersRedeemed() {
        return vouchersRedeemed.get();
    }

    public synchronized boolean deduct(int amount) {
        if (balance.get() >= amount) {
            balance.addAndGet(-amount);
            totalSent.addAndGet(amount);
            return true;
        }
        return false;
    }

    public synchronized boolean deductForVoucher(int amount) {
        if (balance.get() >= amount) {
            balance.addAndGet(-amount);
            vouchersRedeemed.incrementAndGet();
            return true;
        }
        return false;
    }

    public synchronized void credit(int amount) {
        balance.addAndGet(amount);
        totalReceived.addAndGet(amount);
    }
}
