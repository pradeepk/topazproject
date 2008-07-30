/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
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

package org.topazproject.ambra.admin.action;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.models.Rating;
import org.topazproject.ambra.rating.service.RatingsService;


public class ViewRatingAction extends BaseActionSupport {

  private static final Log log = LogFactory.getLog(ViewRatingAction.class);

  private String ratingId;
  private Rating rating;
  private RatingsService ratingsService;


  @Transactional(readOnly = true)
  public String execute() throws Exception {

    rating = getRatingsService().getRating(ratingId);
    return SUCCESS;
  }

  public RatingsService getRatingsService() {
    return ratingsService;
  }

  public void setRatingsService(RatingsService ratingsService) {
    this.ratingsService = ratingsService;
  }

  public Rating getRating() {
    return rating;
  }

  public void setRatingId(String ratingId) {
    this.ratingId = ratingId;
  }
}