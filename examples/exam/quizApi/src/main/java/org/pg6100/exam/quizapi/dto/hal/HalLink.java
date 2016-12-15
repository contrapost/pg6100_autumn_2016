package org.pg6100.exam.quizapi.dto.hal;

import io.swagger.annotations.ApiModelProperty;

/*
    Note: a HAL link can have more data, but here I am only
     interested in the "href"
 */
public class HalLink {

    @ApiModelProperty("URL of the link")
    public String href;

    public HalLink(){}

    public HalLink(String href){
        this.href = href;
    }
}
