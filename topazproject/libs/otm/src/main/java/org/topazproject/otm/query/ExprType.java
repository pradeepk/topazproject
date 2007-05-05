/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm.query;

import java.util.ArrayList;
import java.util.List;

import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.mapping.EmbeddedClassMapper;

/** 
 * This describes the type of an expression. It can be an untyped literal, a typed literal,
 * a URI, or class, or an embedded class.
 * 
 * @author Ronald Tschalär
 */
public class ExprType {
  /** valid expression types */
  public enum Type { CLASS, EMB_CLASS, URI, UNTYPED_LIT, TYPED_LIT };

  private final Type                      type;
  private final ClassMetadata             meta;
  private final List<EmbeddedClassMapper> embFields;
  private final String                    datatype;

  private ExprType(Type type, ClassMetadata meta, List<EmbeddedClassMapper> embFields,
                   String datatype) {
    this.type      = type;
    this.meta      = meta;
    this.embFields = embFields;
    this.datatype  = datatype;
  }

  /** 
   * Create a new expression type representing a class. 
   * 
   * @param meta the class metadata
   * @return the new type
   * @throws NullPointerException if <var>meta</var> is null
   */
  public static ExprType classType(ClassMetadata meta) {
    if (meta == null)
      throw new NullPointerException("class metadata may not be null");
    return new ExprType(Type.CLASS, meta, null, null);
  }

  /** 
   * Create a new expression type representing an embedded class. 
   * 
   * @param meta  the container class' metadata
   * @param field the field name pointing to the embedded class
   * @return the new type
   * @throws NullPointerException if <var>meta</var> or <var>field</var> is null
   */
  public static ExprType embeddedClassType(ClassMetadata meta, EmbeddedClassMapper field) {
    if (meta == null)
      throw new NullPointerException("class metadata may not be null");
    if (field == null)
      throw new NullPointerException("class field may not be null");

    List<EmbeddedClassMapper> fields = new ArrayList<EmbeddedClassMapper>();
    fields.add(field);
    return new ExprType(Type.EMB_CLASS, meta, fields, null);
  }

  /** 
   * Create a new expression type representing a URI. 
   * 
   * @return the new type
   */
  public static ExprType uriType() {
    return new ExprType(Type.URI, null, null, null);
  }

  /** 
   * Create a new expression type representing an untyped literal. 
   * 
   * @return the new type
   */
  public static ExprType literalType() {
    return new ExprType(Type.UNTYPED_LIT, null, null, null);
  }

  /** 
   * Create a new expression type representing a typed literal. 
   * 
   * @param datatype the literal's datatype
   * @return the new type
   * @throws NullPointerException if <var>datatype</var> is null
   */
  public static ExprType literalType(String datatype) {
    if (datatype == null)
      throw new NullPointerException("datatype may not be null for a typed literal");
    return new ExprType(Type.TYPED_LIT, null, null, datatype);
  }

  /**
   * Get the expression type.
   *
   * @return the expression type
   */
  public Type getType()
  {
      return type;
  }

  /**
   * Get the class metadata.
   *
   * @return the class metadata if this is a class; null otherwise
   */
  public ClassMetadata getMeta() {
    return meta;
  }

  /**
   * Get the mappers describing the fields containing the (nested) embedded classes.
   *
   * @return the mappers if this is an embedded class; null otherwise
   */
  public List<EmbeddedClassMapper> getEmbeddedFields() {
    return embFields;
  }

  /**
   * Get the datatype.
   *
   * @return datatype if this is a typed literal; null otherwise
   */
  public String getDataType()
  {
      return datatype;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ExprType))
      return false;

    ExprType o = (ExprType) other;

    if (type != o.type)
      return false;

    switch (type) {
      case CLASS:
        return meta == o.meta || meta.equals(o.meta);
      case EMB_CLASS:
        return meta == o.meta || meta.equals(o.meta) &&
               embFields == o.embFields || embFields.equals(o.embFields);
      case TYPED_LIT:
        return datatype.equals(o.datatype);
      default:
        return true;
    }
  }

  @Override
  public String toString() {
    return type + (type == Type.CLASS ? "[" + meta.getSourceClass().getName() + "]" :
                   type == Type.EMB_CLASS ? "[" + meta.getSourceClass().getName() + 
                                                  embFieldNames() + "]" :
                   type == Type.TYPED_LIT ? "[" + datatype + "]" :
                   "");
  }

  private String embFieldNames() {
    StringBuilder res = new StringBuilder();
    for (EmbeddedClassMapper m : embFields)
      res.append('.').append(m.getName());
    return res.toString();
  }
}
