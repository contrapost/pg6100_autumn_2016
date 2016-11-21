package org.pg6100.rest.pagination.jee.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class News {

    @Id
    @GeneratedValue
    private Long id;

    private String text;

    private String country;

    /*
        TODO comment on Lazy
     */


    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "news",
            cascade = CascadeType.ALL
    )
    private List<Comment> comments;


    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "news",
            cascade = CascadeType.ALL
    )
    private List<Vote> votes;


    public News(){
        comments = new ArrayList<>();
        votes = new ArrayList<>();
    }

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

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<Vote> getVotes() {
        return votes;
    }

    public void setVotes(List<Vote> votes) {
        this.votes = votes;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
