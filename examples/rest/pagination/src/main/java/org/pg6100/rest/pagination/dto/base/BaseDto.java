package org.pg6100.rest.pagination.dto.base;

/*
    DTOs can extend each other.
    Only thing to keep in mind is that in Java you can
    override methods, but NOT fields.
    In a subclass you can have a public field with same name (eg "id"),
    but it would not override the one here, rather "hide" it (it would
    still accessible based on the reference type of the instance)
 */
public abstract class BaseDto {

    public Long id;
}
