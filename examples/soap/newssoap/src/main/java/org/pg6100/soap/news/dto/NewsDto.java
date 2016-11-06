package org.pg6100.soap.news.dto;

import org.pg6100.news.constraint.Country;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/*
    As using XML instead of JSon, have to use the
    special XML tags to specify what and how fields
    should be un/marshaled.
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NewsDto {

    @XmlElement
    public Long newsId;

    @XmlElement
    public String authorId;

    @XmlElement
    public String text;

    @XmlElement
    @Country
    public String country;

    /*
        Handling this in XML is bit complicated, so let's
        just remove it from the example...
     */
//    @XmlElement
//    public ZonedDateTime creationTime;


    public NewsDto() {
    }

    public NewsDto(Long id, String authorId, String text, String country) {
        this.newsId = id;
        this.authorId = authorId;
        this.text = text;
        this.country = country;
    }
}
