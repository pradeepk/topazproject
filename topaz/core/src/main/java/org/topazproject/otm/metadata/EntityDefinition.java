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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.EntityMode;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.SessionFactory;
import org.topazproject.otm.mapping.Binder;
import org.topazproject.otm.mapping.BlobMapper;
import org.topazproject.otm.mapping.BlobMapperImpl;
import org.topazproject.otm.mapping.EmbeddedMapper;
import org.topazproject.otm.mapping.EmbeddedMapperImpl;
import org.topazproject.otm.mapping.EntityBinder;
import org.topazproject.otm.mapping.IdMapper;
import org.topazproject.otm.mapping.IdMapperImpl;
import org.topazproject.otm.mapping.RdfMapper;
import org.topazproject.otm.mapping.RdfMapperImpl;
import org.topazproject.otm.mapping.java.ClassBinder;
import org.topazproject.otm.mapping.java.FieldBinder;

/**
 * Defines entities.
 *
 * @author Pradeep Krishnan
 */
public class EntityDefinition extends ClassDefinition {
  private final String type;
  private final String graph;
  private final String sup;

  /**
   * Creates a new EntityDefinition object.
   *
   * @param name   The name of this definition.
   * @param type   The rdf:type or null
   * @param graph  The graph or null
   * @param sup    The super class or null
   */
  public EntityDefinition(String name, String type, String graph, String sup) {
    super(name);
    this.type    = type;
    this.graph   = graph;
    this.sup     = sup;
  }

  /**
   * Get type.
   *
   * @return rdf:type or null
   */
  public String getType() {
    return type;
  }

  /**
   * Get graph.
   *
   * @return graph or null
   */
  public String getGraph() {
    return graph;
  }

  /**
   * Get the super entity.
   *
   * @return graph or null
   */
  public String getSuper() {
    return sup;
  }

  /*
   * inherited javadoc
   */
  public ClassMetadata buildClassMetadata(SessionFactory sf)
                                      throws OtmException {
    Set<String>                types     = Collections.emptySet();
    String                     type      = null;
    String                     graph     = null;
    IdMapper                   idField   = null;
    BlobMapper                 blobField = null;
    Collection<RdfMapper>      fields    = new ArrayList<RdfMapper>();
    Collection<EmbeddedMapper> embeds    = new ArrayList<EmbeddedMapper>();

    if (sup != null) {
      ClassMetadata superMeta = sf.getClassMetadata(sup);
      graph       = superMeta.getModel();
      type        = superMeta.getType();
      types       = superMeta.getTypes();
      idField     = superMeta.getIdField();
      blobField   = superMeta.getBlobField();
      fields.addAll(superMeta.getRdfMappers());
      embeds.addAll(superMeta.getEmbeddedMappers());
    }

    if (this.type != null) {
      types = new HashSet<String>(types);

      if (!types.add(this.type))
        throw new OtmException("Duplicate rdf:type '" + this.type + "'in class hierarchy "
                               + getName());

      type = this.type;
    }

    if (this.graph != null)
      graph = this.graph;

    ClassBindings bin = sf.getClassBindings(getName());

    for (String prop : bin.getProperties()) {
      Definition def = sf.getDefinition(prop);

      if (def == null)
        throw new OtmException("No such property :'" + prop + "' bound to entity '" + getName());

      def.resolveReference(sf);

      Map<EntityMode, Binder> binders = bin.resolveBinders(prop, sf);

      if (def instanceof RdfDefinition) {
        if (def.getSupersedes() != null) {
          for (RdfMapper m : fields) {
            if (def.getSupersedes().equals(m.getDefinition().getName())) {
              fields.remove(m);

              break;
            }
          }
        }

        fields.add(new RdfMapperImpl((RdfDefinition) def, binders));
      } else if (def instanceof IdDefinition) {
        if (idField != null)
          throw new OtmException("Duplicate Id field " + def.getName() + " in " + getName());

        idField = new IdMapperImpl((IdDefinition) def, binders);
      } else if (def instanceof BlobDefinition) {
        if (blobField != null)
          throw new OtmException("Duplicate Blob field " + def.getName() + " in " + getName());

        blobField = new BlobMapperImpl((BlobDefinition) def, binders);
      } else if (def instanceof EmbeddedDefinition) {
        EmbeddedDefinition edef = (EmbeddedDefinition) def;
        ClassMetadata      ecm  = sf.getClassMetadata(edef.getEmbedded());

        if (ecm == null)
          throw new OtmException("Can't find definition for embedded entity " + edef.getEmbedded());

        EmbeddedMapperImpl em = new EmbeddedMapperImpl(edef, binders, ecm);
        embeds.add(em);

        for (RdfMapper p : ecm.getRdfMappers())
          fields.add((RdfMapper) em.promote(p));

        if (ecm.getIdField() != null) {
          if (idField != null)
            throw new OtmException("Duplicate Id field " + ecm.getIdField().getName() + " in "
                                   + getName());

          idField = (IdMapper) em.promote(ecm.getIdField());
        }

        if (ecm.getBlobField() != null) {
          if (blobField != null)
            throw new OtmException("Duplicate Blob field " + ecm.getBlobField().getName() + " in "
                                   + getName());

          blobField = (BlobMapper) em.promote(ecm.getBlobField());
        }
      } else {
        throw new OtmException("Invalid definition type '" + def.getClass() + "' for property "
                               + def.getName() + " in " + getName());
      }
    }

    return new ClassMetadata(bin.getBinders(), getName(), type, types, graph, idField, fields,
                             blobField, sup, embeds);
  }
}
