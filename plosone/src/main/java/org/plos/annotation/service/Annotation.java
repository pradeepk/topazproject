/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.plos.annotation.service;

public interface Annotation {
  /**
   * Get annotation type.
   *
   * @return annotation type as String.
   */
  String getType();

  /**
   * Set annotation type.
   *
   * @param type the value to set.
   */
  void setType(String type);

  /**
   * Get annotates.
   *
   * @return annotates as String.
   */
  String getAnnotates();

  /**
   * Set annotates.
   *
   * @param annotates the value to set.
   */
  void setAnnotates(String annotates);

  /**
   * Get context.
   *
   * @return context as String.
   */
  String getContext();

  /**
   * Set context.
   *
   * @param context the value to set.
   */
  void setContext(String context);

  /**
   * Get creator.
   *
   * @return creator as String.
   */
  String getCreator();

  /**
   * Set creator.
   *
   * @param creator the value to set.
   */
  void setCreator(String creator);

  /**
   * Get created.
   *
   * @return created as String.
   */
  String getCreated();

  /**
   * Set created.
   *
   * @param created the value to set.
   */
  void setCreated(String created);

  /**
   * Get body.
   *
   * @return body as String.
   */
  String getBody();

  /**
   * Set body.
   *
   * @param body the value to set.
   */
  void setBody(String body);

  /**
   * Get supersedes.
   *
   * @return supersedes as String.
   */
  String getSupersedes();

  /**
   * Set supersedes.
   *
   * @param supersedes the value to set.
   */
  void setSupersedes(String supersedes);

  /**
   * Get id.
   *
   * @return id as String.
   */
  String getId();

  /**
   * Set id.
   *
   * @param id the value to set.
   */
  void setId(String id);

  /**
   * Get title.
   *
   * @return title as String.
   */
  String getTitle();

  /**
   * Set title.
   *
   * @param title the value to set.
   */
  void setTitle(String title);

  /**
   * Get supersededBy.
   *
   * @return supersededBy as String.
   */
  String getSupersededBy();

  /**
   * Set supersededBy.
   *
   * @param supersededBy the value to set.
   */
  void setSupersededBy(String supersededBy);

  /**
   * Get mediator.
   *
   * @return mediator as String.
   */
  String getMediator();

  /**
   * Set mediator.
   *
   * @param mediator the value to set.
   */
  void setMediator(String mediator);

  /**
   * Get state.
   *
   * @return state as int.
   */
  int getState();

  /**
   * Set state.
   *
   * @param state the value to set.
   */
  void setState(int state);
}
