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
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for view classes. View's are expressed using OQL queries, with every field
 * corresponding to an element in the query's projection list. The fields must be marked using
 * the {@link Projection @Projection} annotation.
 *
 * <p>Subqueries may be used to fill in fields with type collection or array. If the subquery has
 * only a single projection element then nothing further is needed and the projection element is
 * directly converted to the array/collection's component type. If the subquery has more than one
 * element in the projection list then the field's component type must be a class that has the
 * {@link SubView @SubView} annotation; {@link Projection @Projection} annotations on the fields
 * of that class are used as normal to tie the subquery's projection elements to that class'
 * fields.
 *
 * <p>Example:
 * <pre>
 *   @View(query = "select a.uri id, (select oi.uri pid, (select oi.representations from ObjectInfo oi2) reps from ObjectInfo oi where oi = a.parts order by pid) parts from Article a where a.categories = :cat order by id;")
 *   class MyView {
 *     @Projection("id")
 *     String id;
 *
 *     @Projection("parts")
 *     List&lt;MyPart&gt; parts;
 *   }
 *
 *   @SubView
 *   class MyPart {
 *     @Projection("pid")
 *     String id;
 *
 *     @Projection("reps")
 *     Set&lt;String&gt; representations;
 *   }
 * </pre>
 *
 * @author Ronald Tschalär
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface View {
  /**
   * View name. Defaults to class name (without the package prefix).
   */
  String name() default "";

  /**
   * The OQL query.
   */
  String query();
}
