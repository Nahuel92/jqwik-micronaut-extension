package net.jqwik.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.transaction.support.TransactionSynchronizationManager;
import io.micronaut.transaction.test.DefaultTestTransactionExecutionListener;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.micronaut.annotation.DbProperties;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import org.assertj.core.api.Assertions;

// FIXME: This test fails to pass
@JqwikMicronautTest
@DbProperties
class TransactionalTest {
    @Inject
    private ApplicationContext applicationContext;

    @BeforeProperty
    void setup() {
        Assertions.assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
    }

    @AfterProperty
    void cleanup() {
        Assertions.assertThat(TransactionSynchronizationManager.isSynchronizationActive()).isTrue();
    }

    @Property
    void testSpringTransactionListenerMissing() {
        Assertions.assertThat(applicationContext.containsBean(DefaultTestTransactionExecutionListener.class)).isTrue();
    }
}