package org.pg6100.rest.pagination.dto.base;

public class VoteDto  extends BaseDto {

    public String user;

    public VoteDto(){
    }

    public VoteDto(String user) {
        this.user = user;
    }
}
