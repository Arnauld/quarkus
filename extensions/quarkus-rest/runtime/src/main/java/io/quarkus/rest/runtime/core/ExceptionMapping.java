package io.quarkus.rest.runtime.core;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.jboss.logging.Logger;

import io.quarkus.rest.runtime.model.ResourceExceptionMapper;

public class ExceptionMapping {

    private static final Logger log = Logger.getLogger(ExceptionMapping.class);

    private Map<Class<? extends Throwable>, ResourceExceptionMapper<? extends Throwable>> mappers = new HashMap<>();

    public Response mapException(Throwable throwable) {
        Class<?> klass = throwable.getClass();
        do {
            ResourceExceptionMapper<? extends Throwable> mapper = mappers.get(klass);
            if (mapper != null) {
                ExceptionMapper<Throwable> instance = (ExceptionMapper<Throwable>) mapper.getFactory()
                        .createInstance().getInstance();
                return instance.toResponse(throwable);
            }
            klass = klass.getSuperclass();
        } while (klass != null);
        if (throwable instanceof WebApplicationException) {
            return ((WebApplicationException) throwable).getResponse();
        }
        log.error("Request failed ", throwable);
        // FIXME: configurable? stack trace?
        return Response.serverError().build();
    }

    public <T extends Throwable> void addExceptionMapper(Class<T> exceptionClass, ResourceExceptionMapper<T> mapper) {
        mappers.put(exceptionClass, mapper);
    }

}