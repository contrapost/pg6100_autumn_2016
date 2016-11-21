package org.pg6100.rest.pagination.dto.collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.pg6100.rest.pagination.dto.base.NewsDto;
import org.pg6100.rest.pagination.dto.hal.HalLink;

import static org.junit.Assert.*;

public class ListDtoTest {

    private ObjectMapper mapper = new ObjectMapper();


    @Test
    public void testBase() throws Exception {

        String href = "someHref";

        ListDto<NewsDto> list = new ListDto<>();
        list._links = new ListDto.ListLinks();
        list._links.next = new HalLink(href);

        String json = mapper.writeValueAsString(list);
        System.out.println(json);

        ListDto<NewsDto> back = mapper.readValue(json, ListDto.class);
        assertEquals(href, back._links.next.href);
    }
}