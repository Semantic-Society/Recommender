package de.rwth.dbis.neologism.recommender.server;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import java.io.InputStream;
import java.lang.annotation.*;
import java.lang.reflect.Type;

//inspired from https://stackoverflow.com/a/1767494

@Provider
@Consumes({"application/rdf+xml", "text/plain", "application/x-turtle", "text/rdf+n3", "text/plain"})
//@Consumes({ "*/*"})
public class RequestToModel implements MessageBodyReader<Model> {

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        if (!Model.class.isAssignableFrom(type)) {
            return false;
        }
        boolean hasAnnotation = false;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(RDFOptions.class)) {
                hasAnnotation = true;
                break;
            }
        }
        if (!hasAnnotation) {
            System.err.println("A model parameter must have the RDFOptions annotation");
        }
        return hasAnnotation;
    }

    @Override
    public Model readFrom(Class<Model> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                          MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws WebApplicationException {

        RDFOptions options = null;
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(RDFOptions.class)) {
                options = (RDFOptions) annotation;
                break;
            }
        }
        if (options == null) {
            throw new Error("The annotation which was promised was later not found.");
        }

        Model model = ModelFactory.createDefaultModel();
        model = model.read(entityStream, null, options.language());

        if (!options.canBeEmpty() && model.isEmpty()) {
            throw new javax.ws.rs.WebApplicationException("The RDF model cannot be empty", HttpStatus.SC_BAD_REQUEST);
        }

        return model;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.PARAMETER})
    public @interface RDFOptions {
        boolean canBeEmpty();

        String language() default "TURTLE";
    }

}
