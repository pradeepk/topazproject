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
package org.topazproject.otm.mapping.java;

import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.BinderFactory;
import org.topazproject.otm.metadata.BlobDefinition;
import org.topazproject.otm.metadata.Definition;
import org.topazproject.otm.metadata.EmbeddedDefinition;
import org.topazproject.otm.metadata.RdfDefinition;
import org.topazproject.otm.metadata.VarDefinition;
import org.topazproject.otm.serializer.Serializer;

/**
 * A binder factory that can create a Binder for a given property.
 *
 * @author Pradeep Krishnan
 */
public class PropertyBinderFactory implements BinderFactory {
  private final String   propertyName;
  private final Property property;

  /**
   * Creates a new PropertyBinderFactory object.
   *
   * @param propertyName the property definition name
   * @param property the java bean property
   */
  public PropertyBinderFactory(String propertyName, Property property) {
    this.propertyName   = propertyName;
    this.property       = property;
  }

  /*
   * inherited javadoc
   */
  public String getPropertyName() {
    return propertyName;
  }

  /*
   * inherited javadoc
   */
  public EntityMode getEntityMode() {
    return EntityMode.POJO;
  }

  /*
   * inherited javadoc
   */
  public Binder createBinder(SessionFactory sf) throws OtmException {
    Definition pd = sf.getDefinition(propertyName);

    if (pd instanceof EmbeddedDefinition)
      return new EmbeddedClassFieldBinder(property);

    Serializer serializer;
    Class<?>   type = property.getComponentType();

    if (pd instanceof BlobDefinition)
      serializer = null;
    else if (pd instanceof RdfDefinition) {
      RdfDefinition rd = (RdfDefinition) pd;
      serializer = (rd.isAssociation()) ? null
                   : sf.getSerializerFactory().getSerializer(type, rd.getDataType());

      if ((serializer == null) && sf.getSerializerFactory().mustSerialize(type))
        throw new OtmException("No serializer found for '" + type + "' with dataType '"
                               + rd.getDataType() + "' for " + property);
    } else if (pd instanceof VarDefinition) {
      VarDefinition vd = (VarDefinition) pd;
      serializer = (vd.getAssociatedEntity() == null) ? null
                   : sf.getSerializerFactory().getSerializer(type, null);
    } else
      serializer = sf.getSerializerFactory().getSerializer(type, null);

    return AbstractFieldBinder.getBinder(property, serializer);
  }
}
