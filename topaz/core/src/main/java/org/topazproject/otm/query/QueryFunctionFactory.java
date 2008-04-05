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

package org.topazproject.otm.query;

import java.util.List;
import antlr.RecognitionException;
import org.topazproject.otm.SessionFactory;

/** 
 * The interface a factory for OQL query function instances must implement.
 *
 * @author Ronald Tschalär
 */
public interface QueryFunctionFactory {
  /**
   * Get the names of the functions for which this factory can create instances. These are the
   * names by which the functions can be accessed in the OQL query. To avoid clashes, all
   * non-topaz defined function names must be of the form &lt;prefix&gt;:&lt;name&gt;.
   *
   * @return the function names
   */
  String[] getNames();

  /**
   * Create an instance of the function. This should also do basic argument validation, such as
   * checking the number and types of the arguments.
   *
   * @param name  the name of the function to instantiate
   * @param args  the list of arguments to the function
   * @param types the argument types (each entry is type for the argument at the same index)
   * @param sf    the otm session-factory
   * @throws RecognitionException if an argument validation error occurred
   */
  QueryFunction createFunction(String name, List<OqlAST> args, List<ExprType> types,
                               SessionFactory sf)
    throws RecognitionException;
}
