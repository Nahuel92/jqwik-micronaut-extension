package net.jqwik.micronaut;

import io.micronaut.test.annotation.TransactionMode;
import jakarta.inject.Inject;
import net.jqwik.api.Property;
import net.jqwik.api.lifecycle.AfterProperty;
import net.jqwik.api.lifecycle.BeforeProperty;
import net.jqwik.micronaut.annotation.DbProperties;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.Book;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest(transactionMode = TransactionMode.SINGLE_TRANSACTION)
@DbProperties
class JpaSingleTransactionTest {
    @Inject
    private EntityManager entityManager;

    @BeforeProperty
    void setUp() {
        // FIXME: This should be done automatically
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        // FIXME: This should be done automatically

        final Book book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);
    }

    @AfterProperty
    void tearDown() {
        // FIXME: This should be done automatically
        entityManager.getTransaction().rollback();
        // ^^ FIXME: This should be done automatically

        // check setup was rolled back
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).isEmpty();
    }

    @Property(tries = 1)
    void testPersistOne() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).hasSize(1);
    }

    @Property(tries = 1)
    void testPersistTwo() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).hasSize(1);
    }
}