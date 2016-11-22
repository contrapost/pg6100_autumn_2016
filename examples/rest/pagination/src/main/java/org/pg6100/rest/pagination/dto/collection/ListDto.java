package org.pg6100.rest.pagination.dto.collection;

import org.pg6100.rest.pagination.dto.hal.HalLink;
import org.pg6100.rest.pagination.dto.hal.HalLinkSet;
import org.pg6100.rest.pagination.dto.hal.HalObject;

import java.util.List;

/*
    A genetic DTO in HAL format to represent a list of DTOs,
    with all needed links and pagination info
 */
public class ListDto<T> extends HalObject{


    public List<T>  list;

    public Integer rangeMin;

    public Integer rangeMax;

    public Integer totalSize;

    /*
        Note: this technically does not override the _links in the superclass
        (that is not possible in Java), but rather hides it away.
        Jackson is smart enough to properly handle this situation, but
        other JSON libraries might not.
     */
    public ListLinks _links;


    public static class ListLinks extends HalLinkSet{

        public HalLink next;
        public HalLink previous;
    }
}
