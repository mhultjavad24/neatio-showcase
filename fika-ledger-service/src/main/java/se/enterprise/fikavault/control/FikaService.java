package se.enterprise.fikavault.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.fikavault.boundary.dto.FikaVoucherResponse;
import se.enterprise.fikavault.boundary.dto.TransactionRecord;
import se.enterprise.fikavault.entity.EmployeeAccount;
import se.enterprise.fikavault.entity.EmployeeNotFoundException;
import se.enterprise.fikavault.entity.FikaVoucher;
import se.enterprise.fikavault.entity.InsufficientKudosBalanceException;
import se.enterprise.neatio.annotation.NeatioLogged;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Control managing Fika café voucher redemptions and QR voucher generation.
 */
@ApplicationScoped
public class FikaService {

    private static final Logger log = LoggerFactory.getLogger(FikaService.class);

    @Inject
    private InMemoryLedgerRepository repository;

    @Inject
    @ConfigProperty(name = "fika.voucher.cost", defaultValue = "10")
    private int voucherCost;

    @Inject
    @ConfigProperty(name = "fika.voucher.validity-hours", defaultValue = "72")
    private int validityHours;

    @Inject
    @ConfigProperty(name = "fika.voucher.item-description", defaultValue = "Artisan Coffee & Fresh Cinnamon Bun")
    private String itemDescription;

    @NeatioLogged
    public FikaVoucherResponse redeem(String employeeId) {
        EmployeeAccount account = repository.findAccount(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        if (!account.deductForVoucher(voucherCost)) {
            throw new InsufficientKudosBalanceException(employeeId, account.getBalance(), voucherCost);
        }

        UUID voucherId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(validityHours));
        String qrPayload = String.format("FIKA-QR-%s-%s-%d", voucherId, employeeId, expiresAt.getEpochSecond());

        FikaVoucher voucher = new FikaVoucher(
                voucherId,
                employeeId,
                itemDescription,
                qrPayload,
                now,
                expiresAt,
                false
        );

        repository.saveVoucher(voucher);

        TransactionRecord record = new TransactionRecord(
                voucherId,
                "FIKA_REDEMPTION",
                employeeId,
                "INTERNAL_CAFE",
                voucherCost,
                "Redeemed Fika Voucher: " + itemDescription,
                now
        );
        repository.recordTransaction(record);

        log.info("Redeemed Fika voucher for employee {}. Voucher ID: {}", employeeId, voucherId);

        return new FikaVoucherResponse(
                voucherId,
                employeeId,
                itemDescription,
                qrPayload,
                expiresAt
        );
    }
}
