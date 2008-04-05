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

package org.plos.models;

import java.net.URI;
import java.util.Date;

/**
 * ArticleAnnotation interface represents a model Class that provides the necessary information to 
 * annotate an article. It is implemented by Comment, MinorCorrection, and FormalCorrection, and may be
 * implemented by other model classes that shall represent annotation types that are overlaid on an Article. 
 * Since Comment and Correction are semantically different, and inherit from a different semantic hierarchy,
 * this interface allows the UI to process different annotation types more generically.   
 * 
 * @author Alex Worden
 *
 */
public interface ArticleAnnotation {

  public URI getId();
  public void setId(URI newId);
  
  public URI getBody();
  public void setBody(URI body);

  public String getType();
  public void setType(String type);

  public URI getAnnotates();
  public void setAnnotates(URI annotated);
  
  public String getContext();
  public void setContext(String context);

  public String getCreator();
  public void setCreator(String user);
  
  public String getAnonymousCreator();
  public void setAnonymousCreator(String user);

  public Date getCreated();
  public void setCreated(Date date);

  public Annotation getSupersedes();
  public void setSupersedes(Annotation a);
  
  public Annotation getSupersededBy();
  public void setSupersededBy(Annotation a);

  public String getTitle();
  public void setTitle(String title);

  public String getMediator();
  public void setMediator(String applicationId);

  public int getState();
  public void setState(int newState);
}
