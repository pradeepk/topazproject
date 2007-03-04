package org.topazproject.otm.annotations;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An annotation to mark a class as embeddable. All members of this class 
 * will have the same subject as the embedding class.
 *
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface Embeddable {
}
