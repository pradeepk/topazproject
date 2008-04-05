/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.otm.mapping;

import java.util.Map;

import org.topazproject.otm.CascadeType;
import org.topazproject.otm.CollectionType;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.FetchType;
import org.topazproject.otm.id.IdentifierGenerator;
import org.topazproject.otm.metadata.RdfDefinition;

/**
 * Implementation of an RdfMapper property.
 *
 * @author Pradeep krishnan
 */
public class RdfMapperImpl extends AbstractMapper implements RdfMapper {
  private final RdfDefinition def;

  /**
   * Creates a new RdfMapperImpl object for a regular class field.
   *
   * @param def     the property definition
   * @param binders the binders for this property
   */
  public RdfMapperImpl(RdfDefinition def, Map<EntityMode, Binder> binders) {
    super(binders);
    this.def = def;
  }

  /*
   * inherited javadoc
   */
  public String getUri() {
    return getDefinition().getUri();
  }

  /*
   * inherited javadoc
   */
  public boolean typeIsUri() {
    return getDefinition().typeIsUri();
  }

  /*
   * inherited javadoc
   */
  public String getDataType() {
    return getDefinition().getDataType();
  }

  /*
   * inherited javadoc
   */
  public boolean isAssociation() {
    return getDefinition().isAssociation();
  }

  /*
   * inherited javadoc
   */
  public boolean isPredicateMap() {
    return getDefinition().isPredicateMap();
  }

  /*
   * inherited javadoc
   */
  public boolean hasInverseUri() {
    return getDefinition().hasInverseUri();
  }

  /*
   * inherited javadoc
   */
  public String getModel() {
    return getDefinition().getModel();
  }

  /*
   * inherited javadoc
   */
  public CollectionType getColType() {
    return getDefinition().getColType();
  }

  /*
   * inherited javadoc
   */
  public boolean isEntityOwned() {
    return getDefinition().isEntityOwned();
  }

  /*
   * inherited javadoc
   */
  public CascadeType[] getCascade() {
    return getDefinition().getCascade();
  }

  /*
   * inherited javadoc
   */
  public boolean isCascadable(CascadeType op) {
    for (CascadeType ct : getCascade())
      if (ct.implies(op))
        return true;

    return false;
  }

  /*
   * inherited javadoc
   */
  public FetchType getFetchType() {
    return getDefinition().getFetchType();
  }

  /*
   * inherited javadoc
   */
  public IdentifierGenerator getGenerator() {
    return getDefinition().getGenerator();
  }

  /*
   * inherited javadoc
   */
  public String getAssociatedEntity() {
    return getDefinition().getAssociatedEntity();
  }

  /*
   * inherited javadoc
   */
  public RdfDefinition getDefinition() {
    return def;
  }
}
