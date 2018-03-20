package de.rwth.dbis.neologism.recommender.server;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.http.HttpStatus;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

//inspired from https://stackoverflow.com/a/1767494

@Provider
@Consumes({ "application/rdf+xml", "text/plain", "application/x-turtle", "text/rdf+n3", "text/plain" })
//@Consumes({ "*/*"})
public class RequestToModel implements MessageBodyReader<Model> {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.PARAMETER })
	public @interface RDFOptions {
		boolean canBeEmpty();
		String langue() default "TURTLE";
	}

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
			throws IOException, WebApplicationException {

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

		Model model = (Model) ModelFactory.createDefaultModel();
		model = model.read(entityStream, null, options.langue());

		if (!options.canBeEmpty() && model.isEmpty()) {
			throw new javax.ws.rs.WebApplicationException("The RDF model cannot be empty", HttpStatus.SC_BAD_REQUEST);
		}

		return model;
	}
	
}

// import org.apache.jena.rdf.model.Model;
// import org.jdom.Document;
// import javax.ws.rs.ext.MessageBodyReader;
// import javax.ws.rs.ext.Provider;
// import javax.ws.rs.ext.MediaType;
// import javax.ws.rs.ext.MultivaluedMap;
// import java.lang.reflect.Type;
// import java.lang.annotation.Annotation;
// import java.io.InputStream;
//
// @Provider // this annotation is necessary!
// @ConsumeMime("application/xml") // this is a hint to the system to only
// consume xml mime types
// public class XMLMessageBodyReader implements MessageBodyReader<Document> {
// private SAXBuilder builder = new SAXBuilder();
//
// public boolean isReadable(Class type, Type genericType, Annotation[]
// annotations, MediaType mediaType) {
// // check if we're requesting a jdom Document
// return Document.class.isAssignableFrom(type);
// }
//
// public Document readFrom(Class type, Type genericType, Annotation[]
// annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders,
// InputStream entityStream) {
// try {
// return builder.build(entityStream);
// }
// catch (Exception e) {
// // handle error somehow
// }
// }
// }