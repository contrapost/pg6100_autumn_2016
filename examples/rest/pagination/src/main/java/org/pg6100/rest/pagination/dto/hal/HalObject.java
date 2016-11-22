package org.pg6100.rest.pagination.dto.hal;

import io.swagger.annotations.ApiModelProperty;

/*
    HAL (Hypertext Application Language) is a proposal (not a standard, at least not yet...)
    to how to define links in JSON objects
    for HATEOAS ( Hypermedia As The Engine Of Application State).

    Here, I am just using a subset of it.

    More info at:
    https://en.wikipedia.org/wiki/Hypertext_Application_Language
    https://en.wikipedia.org/wiki/HATEOAS
 */
public class HalObject {

    @ApiModelProperty("HAL links")
    public HalLinkSet _links;
}
