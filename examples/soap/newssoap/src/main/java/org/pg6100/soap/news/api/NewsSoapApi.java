package org.pg6100.soap.news.api;

import org.pg6100.soap.news.dto.NewsDto;

import javax.jws.WebService;
import java.util.List;

/**
 * Created by arcuri82 on 11/2/2016.
 */
@WebService( name = "NewsSoap")
public interface NewsSoapApi {

    List<NewsDto> get(String country, String authorId);

    Long createNews(NewsDto dto);
}
