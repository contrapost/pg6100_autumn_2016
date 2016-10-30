package org.pg6100.rest.redirect;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("A numeric counter, with name")
public class CounterDto {

    @ApiModelProperty("The numeric value of the counter")
    public Integer value;

    @ApiModelProperty("The name of the counter")
    public String name;

    @ApiModelProperty("The unique id that identifies this counter resource")
    public Long id;

    public CounterDto(){}

    public CounterDto(Long id, String name, Integer value){
        this.id = id;
        this.name = name;
        this.value = value;
    }
}
