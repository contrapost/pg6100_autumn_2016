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
        Here I do care about performance, as I will have options in the
        REST endpoint to get News with/without votes and comments.
        If I do need comments, I shouldn't read them from the database
        in the first place.
        This means having a LAZY fetch (which is the default).
        Recall: in a bidirectional relationship, you have to use
        "mappedBy" in the @OneToMany annotation.
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
