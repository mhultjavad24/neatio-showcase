package se.enterprise.fikavault.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.enterprise.fikavault.boundary.dto.KudosTransferRequest;
import se.enterprise.fikavault.boundary.dto.KudosTransferResponse;
import se.enterprise.fikavault.boundary.dto.TransactionRecord;
import se.enterprise.fikavault.entity.EmployeeAccount;
import se.enterprise.fikavault.entity.EmployeeNotFoundException;
import se.enterprise.fikavault.entity.InsufficientKudosBalanceException;
import se.enterprise.fikavault.control.InMemoryLedgerRepository;
import se.enterprise.neatio.annotation.NeatioLogged;
import se.enterprise.neatio.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

/**
 * Control managing Kudos transfers between employee accounts.
 */
@ApplicationScoped
public class KudosService {

    private static final Logger log = LoggerFactory.getLogger(KudosService.class);

    @Inject
    private InMemoryLedgerRepository repository;

    @NeatioLogged
    public KudosTransferResponse transfer(KudosTransferRequest request) {
        if (request.senderId().equalsIgnoreCase(request.recipientId())) {
            throw new BusinessRuleException("Employees cannot transfer Kudos to themselves.");
        }

        EmployeeAccount sender = repository.findAccount(request.senderId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.senderId()));

        EmployeeAccount recipient = repository.findAccount(request.recipientId())
                .orElseThrow(() -> new EmployeeNotFoundException(request.recipientId()));

        int amount = request.amount();
        if (!sender.deduct(amount)) {
            throw new InsufficientKudosBalanceException(
                    sender.getEmployeeId(),
                    sender.getBalance(),
                    amount,
                    "Kudos transfer"
            );
        }

        recipient.credit(amount);

        UUID transactionId = UUID.randomUUID();
        Instant now = Instant.now();

        String desc = String.format("[%s] %s",
                request.category() != null ? request.category().name() : "GENERAL",
                request.message());

        TransactionRecord record = new TransactionRecord(
                transactionId,
                "KUDOS_TRANSFER",
                request.senderId(),
                request.recipientId(),
                amount,
                desc,
                now
        );

        repository.recordTransaction(record);

        log.info("Transferred {} kudos from {} to {}. Transaction ID: {}",
                amount, request.senderId(), request.recipientId(), transactionId);

        return new KudosTransferResponse(transactionId, "COMPLETED", now);
    }
}
