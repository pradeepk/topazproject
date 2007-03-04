package org.topazproject.otm.annotations;

import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

/**
 * An annotation to mark an inverse association. 
 * Instead of x p y, load/save y inverse(p) x from/to the triple store.
 * @author Pradeep Krishnan
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface Inverse {
}
