package se.enterprise.fikavault.domain;

import org.junit.jupiter.api.Test;
import se.enterprise.fikavault.api.dto.FikaRedeemRequest;
import se.enterprise.fikavault.api.dto.FikaVoucherResponse;
import se.enterprise.fikavault.api.dto.KudosCategory;
import se.enterprise.fikavault.api.dto.KudosTransferRequest;
import se.enterprise.fikavault.api.dto.KudosTransferResponse;
import se.enterprise.fikavault.api.dto.LedgerSummaryResponse;
import se.enterprise.fikavault.api.dto.TransactionRecord;
import se.enterprise.fikavault.domain.exception.EmployeeNotFoundException;
import se.enterprise.fikavault.domain.exception.InsufficientKudosBalanceException;
import se.enterprise.fikavault.domain.model.FikaVoucher;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DomainDtoAndExceptionsTest {

    @Test
    void shouldVerifyKudosCategoryEnum() {
        assertThat(KudosCategory.values()).containsExactly(
                KudosCategory.TEAMWORK,
                KudosCategory.INNOVATION,
                KudosCategory.LEADERSHIP,
                KudosCategory.CRAFTSMANSHIP,
                KudosCategory.KINDNESS,
                KudosCategory.CUSTOMER_DELIGHT
        );
        assertThat(KudosCategory.valueOf("TEAMWORK")).isEqualTo(KudosCategory.TEAMWORK);
    }

    @Test
    void shouldVerifyDtoRecords() {
        Instant now = Instant.now();
        UUID id = UUID.randomUUID();

        KudosTransferRequest req = new KudosTransferRequest("SE-1", "SE-2", 10, "Great!", KudosCategory.INNOVATION);
        assertThat(req.senderId()).isEqualTo("SE-1");
        assertThat(req.recipientId()).isEqualTo("SE-2");
        assertThat(req.amount()).isEqualTo(10);
        assertThat(req.message()).isEqualTo("Great!");
        assertThat(req.category()).isEqualTo(KudosCategory.INNOVATION);

        KudosTransferResponse res = new KudosTransferResponse(id, "COMPLETED", now);
        assertThat(res.transactionId()).isEqualTo(id);
        assertThat(res.status()).isEqualTo("COMPLETED");
        assertThat(res.timestamp()).isEqualTo(now);

        FikaRedeemRequest redeemReq = new FikaRedeemRequest("SE-1");
        assertThat(redeemReq.employeeId()).isEqualTo("SE-1");

        FikaVoucherResponse voucherRes = new FikaVoucherResponse(id, "SE-1", "Coffee", "QR-1", now);
        assertThat(voucherRes.voucherId()).isEqualTo(id);
        assertThat(voucherRes.employeeId()).isEqualTo("SE-1");
        assertThat(voucherRes.itemDescription()).isEqualTo("Coffee");
        assertThat(voucherRes.qrCodePayload()).isEqualTo("QR-1");
        assertThat(voucherRes.expiresAt()).isEqualTo(now);

        TransactionRecord tx = new TransactionRecord(id, "TRANSFER", "SE-1", "SE-2", 5, "Msg", now);
        assertThat(tx.id()).isEqualTo(id);
        assertThat(tx.type()).isEqualTo("TRANSFER");
        assertThat(tx.senderId()).isEqualTo("SE-1");
        assertThat(tx.recipientId()).isEqualTo("SE-2");
        assertThat(tx.amount()).isEqualTo(5);
        assertThat(tx.description()).isEqualTo("Msg");
        assertThat(tx.timestamp()).isEqualTo(now);

        LedgerSummaryResponse summary = new LedgerSummaryResponse("SE-1", "Name", 10, 5, 15, 1, Collections.singletonList(tx));
        assertThat(summary.employeeId()).isEqualTo("SE-1");
        assertThat(summary.employeeName()).isEqualTo("Name");
        assertThat(summary.balance()).isEqualTo(10);
        assertThat(summary.totalSent()).isEqualTo(5);
        assertThat(summary.totalReceived()).isEqualTo(15);
        assertThat(summary.fikaVouchersRedeemed()).isEqualTo(1);
        assertThat(summary.recentTransactions()).hasSize(1);

        FikaVoucher domainVoucher = new FikaVoucher(id, "SE-1", "Item", "QR", now, now.plusSeconds(3600), false);
        assertThat(domainVoucher.id()).isEqualTo(id);
        assertThat(domainVoucher.redeemed()).isFalse();
    }

    @Test
    void shouldVerifyExceptionsAndExtensions() {
        InsufficientKudosBalanceException balanceEx = new InsufficientKudosBalanceException("SE-9821", 4, 10);
        assertThat(balanceEx.getStatus()).isEqualTo(422);
        assertThat(balanceEx.getExtensions()).containsEntry("employeeId", "SE-9821");
        assertThat(balanceEx.getExtensions()).containsEntry("currentBalance", 4);
        assertThat(balanceEx.getExtensions()).containsEntry("requiredCredits", 10);
        assertThat(balanceEx.getMessage()).contains("Employee 'SE-9821' has 4 credits, but 10 are required for a Fika Voucher.");

        InsufficientKudosBalanceException transferEx = new InsufficientKudosBalanceException("SE-9821", 4, 10, "Kudos transfer");
        assertThat(transferEx.getMessage()).contains("Employee 'SE-9821' has 4 credits, but 10 are required for Kudos transfer.");

        EmployeeNotFoundException notFoundEx = new EmployeeNotFoundException("SE-999");
        assertThat(notFoundEx.getStatus()).isEqualTo(404);
        assertThat(notFoundEx.getExtensions()).containsEntry("employeeId", "SE-999");
        assertThat(notFoundEx.getMessage()).contains("Employee with ID 'SE-999' was not found.");
    }
}
