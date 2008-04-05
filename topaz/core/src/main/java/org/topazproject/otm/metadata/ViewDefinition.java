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
package org.topazproject.otm.metadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.IdMapperImpl;
import org.topazproject.otm.mapping.VarMapper;
import org.topazproject.otm.mapping.VarMapperImpl;

/**
 * Defines OQL views.
 *
 * @author Pradeep Krishnan
 */
public class ViewDefinition extends ClassDefinition {
  private final String query;

  /**
   * Creates a new ViewDefinition object.
   *
   * @param name   The name of this definition.
   * @param query  The view query or null for sub-views
   */
  public ViewDefinition(String name, String query) {
    super(name);
    this.query = query;
  }

  /**
   * Gets the query string.
   *
   * @return view query string or null for sub-views.
   */
  public String getQuery() {
    return query;
  }

  /*
   * inherited javadoc
   */
  protected ClassMetadata buildClassMetadata(SessionFactory sf, ClassDefinition ref)
                                      throws OtmException {
    IdMapper              idField = null;
    Collection<VarMapper> fields  = new ArrayList<VarMapper>();

    ClassBindings         bin     = sf.getClassBindings(ref.getName());

    for (String prop : bin.getProperties()) {
      Map<EntityMode, Binder> binders = bin.getBinders(prop);
      Definition              def     = sf.getDefinition(prop);

      if (def == null)
        throw new OtmException("No such property :" + prop);

      while (def instanceof PropertyReferenceDefinition) {
        String d = ((PropertyReferenceDefinition) def).getReferred();
        def = sf.getDefinition(d);

        if (def == null)
          throw new OtmException("No such property :" + d + " referred from " + prop);
      }

      if (def instanceof VarDefinition)
        fields.add(new VarMapperImpl((VarDefinition) def, binders));
      else if (def instanceof IdDefinition) {
        if (idField != null)
          throw new OtmException("Duplicate Id field " + def.getName());

        idField = new IdMapperImpl((IdDefinition) def, binders);
      } else {
        throw new OtmException("Invalid definition type '" + def.getClass() + "' for property "
                               + def.getName() + " in " + ref.getName());
      }
    }

    if ((query != null) && (idField == null))
      throw new OtmException("Missing Id field in " + getName());

    return new ClassMetadata(bin.getBinders(), ref.getName(), query, idField, fields);
  }
}
