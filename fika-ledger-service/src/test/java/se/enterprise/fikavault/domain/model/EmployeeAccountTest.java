package se.enterprise.fikavault.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeAccountTest {

    @Test
    void shouldInitializeAccountCorrectly() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 50);

        assertThat(account.getEmployeeId()).isEqualTo("SE-100");
        assertThat(account.getFullName()).isEqualTo("Alice Smith");
        assertThat(account.getBalance()).isEqualTo(50);
        assertThat(account.getTotalSent()).isEqualTo(0);
        assertThat(account.getTotalReceived()).isEqualTo(0);
        assertThat(account.getVouchersRedeemed()).isEqualTo(0);
    }

    @Test
    void shouldCreditAccount() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 10);
        account.credit(15);

        assertThat(account.getBalance()).isEqualTo(25);
        assertThat(account.getTotalReceived()).isEqualTo(15);
    }

    @Test
    void shouldDeductBalanceWhenSufficient() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 30);
        boolean success = account.deduct(20);

        assertThat(success).isTrue();
        assertThat(account.getBalance()).isEqualTo(10);
        assertThat(account.getTotalSent()).isEqualTo(20);
    }

    @Test
    void shouldFailDeductWhenInsufficientBalance() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 10);
        boolean success = account.deduct(25);

        assertThat(success).isFalse();
        assertThat(account.getBalance()).isEqualTo(10);
        assertThat(account.getTotalSent()).isEqualTo(0);
    }

    @Test
    void shouldDeductForVoucherAndIncrementCounter() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 20);
        boolean success = account.deductForVoucher(10);

        assertThat(success).isTrue();
        assertThat(account.getBalance()).isEqualTo(10);
        assertThat(account.getVouchersRedeemed()).isEqualTo(1);
    }

    @Test
    void shouldFailDeductForVoucherWhenInsufficientBalance() {
        EmployeeAccount account = new EmployeeAccount("SE-100", "Alice Smith", 5);
        boolean success = account.deductForVoucher(10);

        assertThat(success).isFalse();
        assertThat(account.getBalance()).isEqualTo(5);
        assertThat(account.getVouchersRedeemed()).isEqualTo(0);
    }
}
