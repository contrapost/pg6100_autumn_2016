package org.pg6100.rest.pagination.dto.base;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "A positive vote for a given news, representing a user that liked it")
public class VoteDto  extends BaseDto {

    @ApiModelProperty("The id of the user")
    public String user;

    public VoteDto(){
    }

    public VoteDto(String user) {
        this.user = user;
    }
}
