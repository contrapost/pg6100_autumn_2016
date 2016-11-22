package org.pg6100.rest.pagination.dto.hal;

/*
    Note: a HAL link can have more data, but here I am only
     interested in the "href"
 */
public class HalLink {

    public String href;

    public HalLink(){}

    public HalLink(String href){
        this.href = href;
    }
}
