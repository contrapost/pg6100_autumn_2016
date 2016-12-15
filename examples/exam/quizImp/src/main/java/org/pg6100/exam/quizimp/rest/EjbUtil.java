package org.pg6100.exam.quizimp.rest;

import com.google.common.base.Throwables;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import java.util.function.Supplier;

/**
 * Created by arcuri82 on 02-Dec-16.
 */
public class EjbUtil {

    public static <T> T ejbCall(Supplier<T> supplier) {

        try {
            T t = supplier.get();
            return t;
        } catch (Exception e) {
            throw wrapException(e);
        }
    }

    public static WebApplicationException wrapException(Exception e) throws WebApplicationException {

        Throwable cause = Throwables.getRootCause(e);
        if (cause instanceof ConstraintViolationException ||
                cause instanceof IllegalArgumentException) {
            return new WebApplicationException("Invalid constraints on input: " + cause.getMessage(), 400);
        } else {
            return new WebApplicationException("Internal error: "+cause.toString(), 500);
        }
    }
}
