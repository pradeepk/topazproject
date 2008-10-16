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

package org.topazproject.ambra.models;

import java.io.Serializable;
import java.net.URL;

import org.topazproject.otm.annotations.Entity;
import org.topazproject.otm.annotations.GeneratedValue;
import org.topazproject.otm.annotations.Id;
import org.topazproject.otm.annotations.Predicate;
import org.topazproject.otm.annotations.UriPrefix;

/**
 * RatingContent is the body of a Rating.
 * It stores, insight, reliability, style and comment values.
 *
 * @author stevec
 * @author Jeff Suttor
 */
@UriPrefix("topaz:TrackbackContent/")
@Entity(model = "ri", types = {"topaz:TrackbackContent"})
public class TrackbackContent implements Serializable {
  static final long serialVersionUID = -4310540950708482559L;

  private String id;
  private String title;
  private URL url;
  private String blog_name;
  private String excerpt;

  /**
   * Creates a new TrackbackContent object with default values.
   */
  public TrackbackContent() {
    this(null, null, null, null);
  }


  /**
   * Creates a new TrackbackContent object with specified values.
   *
   * @param title the title from the track back
   * @param excerpt the excerpt from the track back
   * @param blog_name the name of the blog
   * @param url the track back url
   */
  public TrackbackContent(String title, String excerpt, String blog_name, URL url) {
    this.title = title;
    this.excerpt = excerpt;
    this.blog_name = blog_name;
    this.url = url;
  }

  /**
   * @return Returns the blog_name.
   */
  public String getBlog_name() {
    return blog_name;
  }

  /**
   * @param blog_name The blog_name to set.
   */
  @Predicate
  public void setBlog_name(String blog_name) {
    this.blog_name = blog_name;
  }

  /**
   * @return Returns the excerpt.
   */
  public String getExcerpt() {
    return excerpt;
  }

  /**
   * @param excerpt The excerpt to set.
   */
  @Predicate
  public void setExcerpt(String excerpt) {
    this.excerpt = excerpt;
  }

  /**
   * @return Returns the id.
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The id to set.
   */
  @Id
  @GeneratedValue(uriPrefix = "id:trackbackContent/")
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title The title to set.
   */
  @Predicate
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the url.
   */
  public URL getUrl() {
    return url;
  }

  /**
   * @param url The url to set.
   */
  @Predicate
  public void setUrl(URL url) {
    this.url = url;
  }
}
