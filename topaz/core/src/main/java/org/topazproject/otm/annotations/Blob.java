/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.otm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to mark a Blob field. It may only be applied to byte array fields.
 * Only one field in a class may be marked as a Blob field and 
 * it must be a scalar field. The {@link Id @Id} represents the id of the Blob
 * and must be unique in the {@link org.topazproject.otm.BlobStore BlobStore} where
 * this Blob is persisted.
 * <p>
 * Blob fields are considered literal values and therefore no other RDF persistable
 * fields can be defined with the same blob id. This means there is no need to have an
 * {@link Entity @Entity} annotation on the class and also there can not be any fields 
 * with {@link Predicate @Predicate} annotation on them
 *
 * @author Pradeep Krishnan
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Blob {
}
