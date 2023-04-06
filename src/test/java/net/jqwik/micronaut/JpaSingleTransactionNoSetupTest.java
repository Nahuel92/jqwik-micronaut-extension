package net.jqwik.micronaut;

import jakarta.inject.Inject;
import net.jqwik.api.Example;
import net.jqwik.api.lifecycle.AfterContainer;
import net.jqwik.micronaut.annotation.DbProperties;
import net.jqwik.micronaut.annotation.JqwikMicronautTest;
import net.jqwik.micronaut.beans.Book;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;

import static org.assertj.core.api.Assertions.assertThat;

@JqwikMicronautTest
@DbProperties
class JpaSingleTransactionNoSetupTest {
    @Inject
    private EntityManager entityManager;

    @AfterContainer
    static void tearDown(final JpaSingleTransactionNoSetupTest subject) {
        // check test was rolled back
        final CriteriaQuery<Book> query = subject.entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(subject.entityManager.createQuery(query).getResultList()).isEmpty();
    }

    @Example
    void testPersistOne() {
        final Book book = new Book();
        book.setTitle("The Stand");
        entityManager.persist(book);

        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList().size()).isEqualTo(1);
    }

    @Example
    void testPersistTwo() {
        final Book book = new Book();
        book.setTitle("The Shining");
        entityManager.persist(book);

        final CriteriaQuery<Book> query = entityManager.getCriteriaBuilder().createQuery(Book.class);
        query.from(Book.class);
        assertThat(entityManager.createQuery(query).getResultList().size()).isEqualTo(1);
    }
}