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

import org.topazproject.otm.ClassMetadata;

/** 
 * This describes the type of an expression. It can be an untyped literal, a typed literal,
 * a URI, or a class.
 * 
 * @author Ronald Tschalär
 */
public class ExprType {
  /** valid expression types */
  public enum Type { CLASS, URI, UNTYPED_LIT, TYPED_LIT };

  private final Type          type;
  private final ClassMetadata meta;
  private final String        datatype;

  private ExprType(Type type, ClassMetadata meta, String datatype) {
    this.type     = type;
    this.meta     = meta;
    this.datatype = datatype;
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
    return new ExprType(Type.CLASS, meta, null);
  }

  /** 
   * Create a new expression type representing a URI. 
   * 
   * @return the new type
   */
  public static ExprType uriType() {
    return new ExprType(Type.URI, null, null);
  }

  /** 
   * Create a new expression type representing an untyped literal. 
   * 
   * @return the new type
   */
  public static ExprType literalType() {
    return new ExprType(Type.UNTYPED_LIT, null, null);
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
    return new ExprType(Type.TYPED_LIT, null, datatype);
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
      case TYPED_LIT:
        return datatype.equals(o.datatype);
      default:
        return true;
    }
  }

  @Override
  public String toString() {
    return type + (type == Type.CLASS ? "[" + meta.getSourceClass().getName() + "]" :
                   type == Type.TYPED_LIT ? "[" + datatype + "]" :
                   "");
  }
}
