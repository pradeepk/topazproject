package org.topazproject.otm.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to mark an id field to be used as the subject of the rdf triples.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Id {
}
