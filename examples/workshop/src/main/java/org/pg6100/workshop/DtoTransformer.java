package org.pg6100.workshop;

public class DtoTransformer {

    public static UserDto transform(UserEntity entity){

        UserDto dto = new UserDto();
        dto.name = entity.getName();
        dto.surname = entity.getSurname();
        dto.address = entity.getAddress();

        return dto;
    }
}
