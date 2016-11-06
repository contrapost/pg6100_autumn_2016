package org.pg6100.dropwizard.newsdw;


import org.pg6100.news.NewsEJB;
import org.pg6100.news.NewsEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Supplier;


/*
    Note: you should not write classes like this one.
    Here, I just want to re-use JEE code (EJB in particular)
    in a non-JEE environment, and show what would need to be done.

    This is just for educational purposes. Do NOT write
    code like this.

    Note: Dropwizard is an opinionated framework, and
    comes with third-party libraries, like JDBI for
    database accesses

    http://www.dropwizard.io/1.0.3/docs/getting-started.html
 */
public class DatabaseBean extends NewsEJB {

    private final EntityManagerFactory factory = Persistence.createEntityManagerFactory("NEWS_DB");

    private EntityManager entityManager;

    public DatabaseBean() {

        entityManager = factory.createEntityManager();

        /*
            Note: this is a "dirty" hack that you should not
            use, unless in extreme situations.

            Ie, you should not use reflection to change private
            fields of external libraries, as that breaks encapsulation.

            Here we are just simulation dependency injection, as
            the JEE annotation @PersistenceContext would be totally
            ignored by DropWizard
         */
        try {
            Field field = NewsEJB.class.getDeclaredField("em");
            field.setAccessible(true);
            field.set(this, entityManager);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        Note: to avoid possible concurrency issues, here
        all methods are synchronized
     */


    /**
        Here I am going to wrap each DB operation in a transaction.
     */
    private <T> T exeInTransaction(Supplier<T> supplier){

        EntityTransaction et = entityManager.getTransaction();
        et.begin();

        try {
            T result =  supplier.get();
            et.commit();
            return result;
        } catch (Exception e){
            et.rollback();
            throw e;
        }
    }

    @Override
    public synchronized Long createNews(String authorId, String text, String country) {

        return exeInTransaction(() ->
                super.createNews(authorId, text, country));
    }


    @Override
    public synchronized boolean deleteNews(Long newsId) {

        return exeInTransaction(() ->
                super.deleteNews(newsId));
    }


    @Override
    public synchronized boolean updateText(@NotNull Long newsId, @NotNull String text) {

        return exeInTransaction(() ->
                super.updateText(newsId, text));
    }

    @Override
    public synchronized boolean update(@NotNull Long newsId,
                                       @NotNull String text,
                                       @NotNull String authorId,
                                       @NotNull String country,
                                       @NotNull ZonedDateTime creationTime) {

        return exeInTransaction(() ->
                super.update(newsId, text, authorId, country, creationTime));
    }

    /*
        Following are read-only operations, so no need
        of handling transactions.

        If it wasn't for synchronized, I would not need
        to override them
     */

    @Override
    public synchronized boolean isPresent(Long newsId) {
        return super.isPresent(newsId);
    }

    @Override
    public synchronized NewsEntity get(@NotNull Long newsId) {
        return super.get(newsId);
    }

    @Override
    public synchronized List<NewsEntity> getAll() {
        return super.getAll();
    }

    @Override
    public synchronized List<NewsEntity> getAllByCountry(@NotNull String country) {
        return super.getAllByCountry(country);
    }

    @Override
    public synchronized List<NewsEntity> getAllByAuthor(@NotNull String authorId) {
        return super.getAllByAuthor(authorId);
    }

    @Override
    public synchronized List<NewsEntity> getAllByCountryAndAuthor(@NotNull String country, @NotNull String authorId) {
        return super.getAllByCountryAndAuthor(country, authorId);
    }
}
