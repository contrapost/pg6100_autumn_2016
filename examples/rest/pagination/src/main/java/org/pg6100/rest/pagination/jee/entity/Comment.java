package org.pg6100.rest.pagination.jee.entity;

import javax.persistence.*;

@Entity
public class Comment {

    @Id
    @GeneratedValue
    private Long id;

    private String text;

    @ManyToOne
    private News news;


    public Comment(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public News getNews() {
        return news;
    }

    public void setNews(News news) {
        this.news = news;
    }
}
