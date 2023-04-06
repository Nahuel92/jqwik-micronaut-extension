package net.jqwik.micronaut;

import io.micronaut.test.annotation.TransactionMode;
import jakarta.inject.Inject;
import net.jqwik.api.Example;
import net.jqwik.api.lifecycle.AfterExample;
import net.jqwik.api.lifecycle.BeforeExample;
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

    @BeforeExample
    void setUp() {
        final var book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);
    }

    @AfterExample
    void tearDown() {
        // check setup was rolled back
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).isEmpty();
    }

    @Example
    void testPersistOne() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).hasSize(1);
    }

    @Example
    void testPersistTwo() {
        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList()).hasSize(1);
    }
}