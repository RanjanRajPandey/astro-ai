package com.astroai.chart;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Supplier;

@Component
public class ChartPersistenceHelper {

    private static final Object DB_WRITE_LOCK = new Object();

    private final ChartRepository chartRepository;
    private final TransactionTemplate transactionTemplate;

    public ChartPersistenceHelper(
            ChartRepository chartRepository,
            PlatformTransactionManager transactionManager
    ) {
        this.chartRepository = chartRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * Executes a database write block inside a dedicated transaction that starts and commits
     * strictly inside a process-wide synchronized lock. This prevents concurrent HTTP requests
     * (such as React StrictMode double-mounts or Promise.all calls) from racing on unique constraints.
     */
    public <T> T runSynchronizedTransaction(Supplier<T> action) {
        synchronized (DB_WRITE_LOCK) {
            return transactionTemplate.execute(status -> action.get());
        }
    }

    public Chart getOrCreateChartInCurrentTx(
            UUID birthProfileId,
            String ayanamshaType,
            BigDecimal ayanamshaValue,
            String houseSystem,
            String nodeType,
            String ascendantSign,
            BigDecimal ascendantDegree,
            String calculationVersion
    ) {
        return chartRepository.findByBirthProfileId(birthProfileId)
                .orElseGet(() -> chartRepository.saveAndFlush(new Chart(
                        UUID.randomUUID(),
                        birthProfileId,
                        ayanamshaType,
                        ayanamshaValue,
                        houseSystem,
                        nodeType,
                        ascendantSign,
                        ascendantDegree,
                        calculationVersion,
                        Instant.now()
                )));
    }
}
