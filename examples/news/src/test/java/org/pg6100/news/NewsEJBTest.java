package org.pg6100.news;

import com.google.common.base.Throwables;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.validation.ConstraintViolationException;

import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class NewsEJBTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, "org.pg6100.news")
                .addPackages(true, "com.google")
                .addAsResource("country/country_list.txt")
                .addAsResource("META-INF/persistence.xml");
    }

    @EJB
    private NewsEJB ejb;

    @Before
    @After
    public void cleanDatabase() {
        ejb.getAll().stream().forEach(n -> ejb.deleteNews(n.getId()));
        assertEquals(0, ejb.getAll().size());
    }

    @Test
    public void testCleanDB() {
        int n = ejb.getAll().size();
        assertEquals(0, n);
    }


    @Test
    public void testCreate() {

        String author = "author";
        String text = "someText";
        String country = "Norway";

        assertEquals(0, ejb.getAll().size());

        Long id = ejb.createNews(author, text, country);

        assertEquals(1, ejb.getAll().size());
        assertEquals(id, ejb.get(id).getId());
    }

    @Test
    public void testDelete() {

        Long id = ejb.createNews("author", "text", "Norway");
        assertTrue(ejb.isPresent(id));
        assertTrue(ejb.getAll().stream().anyMatch(n -> n.getId().equals(id)));

        ejb.deleteNews(id);
        assertFalse(ejb.isPresent(id));
        assertFalse(ejb.getAll().stream().anyMatch(n -> n.getId().equals(id)));
    }

    @Test
    public void testGet() {

        String author = "author";
        String text = "someText";
        String country = "Norway";

        Long id = ejb.createNews(author, text, country);
        NewsEntity news = ejb.get(id);

        assertEquals(author, news.getAuthorId());
        assertEquals(text, news.getText());
        assertEquals(country, news.getCountry());
    }

    @Test
    public void testUpdate() {

        String text = "someText";

        Long id = ejb.createNews("author", text, "Norway");
        assertEquals(text, ejb.get(id).getText());

        String updated = "new updated text";
        ejb.updateText(id, updated);
        assertEquals(updated, ejb.get(id).getText());
    }


    private void createSomeNews() {
        ejb.createNews("a", "text", "Norway");
        ejb.createNews("a", "other text", "Norway");
        ejb.createNews("a", "more text", "Sweden");
        ejb.createNews("b", "text", "Norway");
        ejb.createNews("b", "yet another text", "Iceland");
        ejb.createNews("c", "text", "Iceland");
    }


    @Test
    public void testGetAll() {

        assertEquals(0, ejb.getAll().size());
        createSomeNews();

        assertEquals(6, ejb.getAll().size());
    }

    @Test
    public void testGetAllByCountry() {

        assertEquals(0, ejb.getAll().size());
        createSomeNews();

        assertEquals(3, ejb.getAllByCountry("Norway").size());
        assertEquals(1, ejb.getAllByCountry("Sweden").size());
        assertEquals(2, ejb.getAllByCountry("Iceland").size());
    }

    @Test
    public void testGetAllByAuthor() {

        assertEquals(0, ejb.getAll().size());
        createSomeNews();

        assertEquals(3, ejb.getAllByAuthor("a").size());
        assertEquals(2, ejb.getAllByAuthor("b").size());
        assertEquals(1, ejb.getAllByAuthor("c").size());
    }

    @Test
    public void testGetAllByCountryAndAuthor() {

        assertEquals(0, ejb.getAll().size());
        createSomeNews();

        assertEquals(2, ejb.getAllByCountryAndAuthor("Norway", "a").size());
        assertEquals(1, ejb.getAllByCountryAndAuthor("Sweden", "a").size());
        assertEquals(0, ejb.getAllByCountryAndAuthor("Iceland", "a").size());
        assertEquals(1, ejb.getAllByCountryAndAuthor("Norway", "b").size());
        assertEquals(0, ejb.getAllByCountryAndAuthor("Sweden", "b").size());
        assertEquals(1, ejb.getAllByCountryAndAuthor("Iceland", "b").size());
        assertEquals(0, ejb.getAllByCountryAndAuthor("Norway", "c").size());
        assertEquals(0, ejb.getAllByCountryAndAuthor("Sweden", "c").size());
        assertEquals(1, ejb.getAllByCountryAndAuthor("Iceland", "c").size());
    }

    @Test
    public void testInvalidAuthor() {
        try {
            ejb.createNews("", "text", "Norway");
        } catch (EJBException e) {
            Throwable cause = Throwables.getRootCause(e);
            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
        }
    }

    @Test
    public void testInvalidText() {
        try {
            ejb.createNews("author", "", "Norway");
        } catch (EJBException e) {
            Throwable cause = Throwables.getRootCause(e);
            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
        }
    }

    @Test
    public void testInvalidCountry() {
        try {
            ejb.createNews("author", "text", "Foo");
        } catch (EJBException e) {
            Throwable cause = Throwables.getRootCause(e);
            assertTrue("Cause: " + cause, cause instanceof ConstraintViolationException);
        }
    }


}