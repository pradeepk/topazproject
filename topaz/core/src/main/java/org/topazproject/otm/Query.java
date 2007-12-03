/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.topazproject.otm;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.topazproject.otm.query.GenericQueryImpl;
import org.topazproject.otm.query.Results;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/** 
 * This represents an OQL query. Instances are obtained via {@link Session#createQuery
 * Session.createQuery()}.
 * 
 * @author Ronald Tschalär
 */
public abstract class Query extends AbstractParameterizable<Query> {

  public abstract Results execute() throws OtmException;
}
