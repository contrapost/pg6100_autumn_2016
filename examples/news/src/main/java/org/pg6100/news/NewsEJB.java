package org.pg6100.news;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class NewsEJB {

    @PersistenceContext
    private EntityManager em;

    public Long createNews(String authorId, String text, String country){

        NewsEntity news = new NewsEntity();
        news.setAuthorId(authorId);
        news.setText(text);
        news.setCountry(country);
        news.setCreationTime(LocalDateTime.now());

        em.persist(news);

        return news.getId();
    }


    public boolean isPresent(Long newsId){
        return em.find(NewsEntity.class, newsId) != null;
    }

    public boolean deleteNews(Long newsId){
        NewsEntity news = em.find(NewsEntity.class, newsId);
        if(news != null){
            em.remove(news);
            return true;
        }
        return false;
    }


    public boolean updateText(@NotNull Long newsId, @NotNull String text){
        NewsEntity news = em.find(NewsEntity.class, newsId);
        if(news == null){
            return false;
        }
        news.setText(text);
        return true;
    }

    public NewsEntity get(@NotNull Long newsId){
        return em.find(NewsEntity.class, newsId);
    }

    public List<NewsEntity> getAll(){
        Query query = em.createQuery("select n from NewsEntity n order by n.creationTime DESC");
        return query.getResultList();
    }

    public List<NewsEntity> getAllByCountry(@NotNull String country){
        Query query = em.createQuery("select n from NewsEntity n where n.country = ?1 order by n.creationTime DESC");
        query.setParameter(1, country);
        return query.getResultList();
    }

    public List<NewsEntity> getAllByAuthor(@NotNull String authorId){
        Query query = em.createQuery("select n from NewsEntity n where n.authorId = ?1 order by n.creationTime DESC");
        query.setParameter(1, authorId);
        return query.getResultList();
    }

    public List<NewsEntity> getAllByCountryAndAuthor(@NotNull String country, @NotNull String authorId){
        Query query = em.createQuery(
                "select n from NewsEntity n where n.country = ?1 and n.authorId = ?2 order by n.creationTime DESC");
        query.setParameter(1, country);
        query.setParameter(2, authorId);
        return query.getResultList();
    }
}
