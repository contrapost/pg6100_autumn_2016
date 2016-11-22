package org.pg6100.rest.pagination;

import org.pg6100.rest.pagination.dto.base.CommentDto;
import org.pg6100.rest.pagination.dto.base.NewsDto;
import org.pg6100.rest.pagination.dto.base.VoteDto;
import org.pg6100.rest.pagination.dto.collection.ListDto;
import org.pg6100.rest.pagination.jee.entity.Comment;
import org.pg6100.rest.pagination.jee.entity.News;
import org.pg6100.rest.pagination.jee.entity.Vote;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DtoTransformer {

    public static VoteDto transform(Vote vote){
        if(vote == null){
            return null;
        }
        VoteDto dto = new VoteDto();
        dto.id = vote.getId();
        dto.user = vote.getUser();
        return dto;
    }

    public static CommentDto transform(Comment comment){
        if(comment==null){
            return null;
        }
        CommentDto dto = new CommentDto();
        dto.id = comment.getId();
        dto.text = comment.getText();
        return dto;
    }

    public static NewsDto transform(News news,
                                    boolean withComments,
                                    boolean withVotes){
        if(news==null){
            return null;
        }
        NewsDto dto = new NewsDto();
        dto.id = news.getId();
        dto.text = news.getText();
        dto.country = news.getCountry();

        if(withComments){
            dto.comments = new ArrayList<>();
            news.getComments().stream()
                    .map(DtoTransformer::transform)
                    .forEach(n -> dto.comments.add(n));
        }

        if(withVotes){
            dto.votes = new ArrayList<>();
            news.getVotes().stream()
                    .map(DtoTransformer::transform)
                    .forEach(v -> dto.votes.add(v));
        }

        return dto;
    }


    /**
       This creates a HAL dto, but with the links (self, next, previous)
       that still have to be set
     */
    public static ListDto<NewsDto> transform(List<News> newsList,
                                             int offset,
                                             int limit,
                                      boolean withComments,
                                      boolean withVotes){
        List<NewsDto> dtoList = null;
        if(newsList != null){
            dtoList = newsList.stream()
                    .skip(offset) // this is a good example of how streams simplify coding
                    .limit(limit)
                    .map(n -> transform(n, withComments, withVotes))
                    .collect(Collectors.toList());
        }

        ListDto<NewsDto> dto = new ListDto<>();
        dto.list = dtoList;
        dto._links = new ListDto.ListLinks();
        dto.rangeMin = offset;
        dto.rangeMax = dto.rangeMin + dtoList.size() - 1;
        dto.totalSize = newsList.size();

        return dto;
    }
}
