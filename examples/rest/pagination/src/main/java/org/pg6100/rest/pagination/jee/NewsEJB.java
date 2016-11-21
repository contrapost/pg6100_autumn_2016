package org.pg6100.rest.pagination.jee;


import org.pg6100.rest.pagination.jee.entity.Comment;
import org.pg6100.rest.pagination.jee.entity.News;
import org.pg6100.rest.pagination.jee.entity.Vote;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Stateless
public class NewsEJB {

    @PersistenceContext
    private EntityManager em;

    public News getNews(long id){
        News news = em.find(News.class, id);
        if(news != null){
            news.getVotes().size();
            news.getComments().size();
        }
        return news;
    }

    public List<News> getNewsList(String country,
                                  boolean withComments,
                                  boolean withVotes,
                                  int limit){

        Query query;
        if(country == null){
            query = em.createQuery("select n from News n");
        } else {
            query = em.createQuery("select n from News n where n.country=?1");
            query.setParameter(1, country);
        }
        query.setMaxResults(limit);

        /*
            Those lists are lazily initialized, so they are going to be
            loaded from database only when accessed for the firs time.
            But it has to be done when there is an open session from an
            EntityManager, so we do it here
         */
        List<News> result = query.getResultList();
        if(withComments){
            result.stream().forEach(n -> n.getComments().size());
        }
        if(withVotes){
            result.stream().forEach(n -> n.getVotes().size());
        }

        return result;
    }


    public Long createNews(String text, String country) {

        News news = new News();
        news.setText(text);
        news.setCountry(country);

        em.persist(news);

        return news.getId();
    }

    public void deleteNews(long newsId){
        News news = em.find(News.class, newsId);
        if(news != null){
            em.remove(news);
        }
    }

    public Long createComment(long newsId, String text){

        News news = em.find(News.class, newsId);
        if(news == null){
            throw new IllegalArgumentException("News does not exist");
        }

        Comment comment = new Comment();
        comment.setNews(news);
        comment.setText(text);
        em.persist(comment);

        /*
            Recall: as I get news from em.find() inside
            this transaction, any change to it will be automatically
            sent to the database.

            If, on the other hand, this came as input to the method (
            eg among the "newsId" and "text") from a non-transactional
            context, then I would need to use em.merger()
         */
        news.getComments().add(comment);

        return comment.getId();
    }

    public Long createVote(long newsId, String user){

        News news = em.find(News.class, newsId);
        if(news == null){
            throw new IllegalArgumentException("News does not exist");
        }

        Vote vote = new Vote();
        vote.setUser(user);
        vote.setNews(news);
        em.persist(vote);

        news.getVotes().add(vote);

        return vote.getId();
    }
}
