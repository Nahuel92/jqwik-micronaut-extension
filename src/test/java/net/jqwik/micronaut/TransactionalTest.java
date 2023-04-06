package net.jqwik.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import net.jqwik.api.Example;
import net.jqwik.api.lifecycle.AfterExample;
import net.jqwik.api.lifecycle.BeforeExample;
import net.jqwik.micronaut.annotation.DbProperties;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
@DbProperties
class TransactionalTest {
    @Inject
    private ApplicationContext applicationContext;

    @BeforeExample
    void setup() {
        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
    }

    @AfterExample
    void cleanup() {
        assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
    }

    @Example
    void testSpringTransactionListenerMissing() {
        assertThat(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class)).isTrue();
    }
}