/* $HeadURL::                                                                            $
 * $Id$
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

package org.plos;

import org.plos.annotation.action.BodyFetchAction;
import org.plos.annotation.action.CreateAnnotationAction;
import org.plos.annotation.action.CreateFlagAction;
import org.plos.annotation.action.CreateReplyAction;
import org.plos.annotation.action.DeleteAnnotationAction;
import org.plos.annotation.action.DeleteFlagAction;
import org.plos.annotation.action.DeleteReplyAction;
import org.plos.annotation.action.GetAnnotationAction;
import org.plos.annotation.action.GetFlagAction;
import org.plos.annotation.action.GetReplyAction;
import org.plos.annotation.action.ListAnnotationAction;
import org.plos.annotation.action.ListFlagAction;
import org.plos.annotation.action.ListReplyAction;
import org.plos.annotation.action.UnflagAnnotationAction;
import org.plos.annotation.service.AnnotationService;
import org.plos.article.action.FetchArticleAction;
import org.plos.article.action.FetchObjectAction;
import org.plos.article.action.SecondaryObjectAction;
import org.plos.article.service.ArticleOtmService;
import org.plos.article.service.FetchArticleService;
import org.plos.permission.service.PermissionsService;
import org.plos.search.action.SearchAction;
import org.plos.user.action.AdminUserAlertsAction;
import org.plos.user.action.AdminUserProfileAction;
import org.plos.user.action.AssignAdminRoleAction;
import org.plos.user.action.DisplayUserAction;
import org.plos.user.action.MemberUserAlertsAction;
import org.plos.user.action.MemberUserProfileAction;
import org.plos.user.action.SearchUserAction;
import org.plos.user.service.UserService;
import org.plos.util.ProfanityCheckingService;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import javax.xml.rpc.ServiceException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public abstract class BasePlosoneTestCase extends AbstractDependencyInjectionSpringContextTests {
  private FetchArticleAction fetchArticleAction;
  private CreateAnnotationAction createAnnotationAction;
  private DeleteAnnotationAction deleteAnnotationAction;
  private ListAnnotationAction listAnnotationAction;
  private ListReplyAction listReplyAction;
  private DeleteReplyAction deleteReplyAction;
  private CreateReplyAction createReplyAction;
  private BodyFetchAction bodyFetchAction;
  private GetAnnotationAction getAnnotationAction;
  private GetReplyAction getReplyAction;
  private MemberUserProfileAction memberUserProfileAction;
  private AdminUserProfileAction adminUserProfileAction;
  private MemberUserAlertsAction memberUserAlertsAction;
  private AdminUserAlertsAction adminUserAlertsAction;
  private AssignAdminRoleAction assignAdminRoleAction;
  private FetchObjectAction fetchObjectAction;
  private SecondaryObjectAction secondaryObjectAction;
  private DisplayUserAction displayUserAction;
  private CreateFlagAction createFlagAction;
  private GetFlagAction getFlagAction;

  private FetchArticleService fetchArticleService;
  private ProfanityCheckingService profanityCheckingService;
  private ArticleOtmService articleOtmService;
  private PermissionsService permissionsService;
  private AnnotationService annotationService;
  private UserService userService;
  private DeleteFlagAction deleteFlagAction;
  private UnflagAnnotationAction unflagAnnotationAction;
  private ListFlagAction listFlagAction;
  private SearchAction searchAction;
  private SearchUserAction searchUserAction;

  protected String[] getConfigLocations() {
    return new String[]{"nonWebApplicationContext.xml", "testApplicationContext.xml", "propertyConfigurer.xml", "countryList.xml", "profaneWords.xml"};
  }

  protected PermissionsService getPermissionsService() {
    return permissionsService;
  }

  public void setPermissionsService(final PermissionsService permissionsService) {
    this.permissionsService = permissionsService;
  }

  protected ArticleOtmService getArticleOtmService() throws MalformedURLException, ServiceException {
    return articleOtmService;
  }

  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  public FetchArticleService getFetchArticleService() throws MalformedURLException, ServiceException {
    return fetchArticleService;
  }

  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  protected FetchArticleAction getFetchArticleAction() throws MalformedURLException, ServiceException {
    return fetchArticleAction;
  }

  public void setFetchArticleAction(final FetchArticleAction fetchArticleAction) {
    this.fetchArticleAction = fetchArticleAction;
  }

  public AnnotationService getAnnotationService() throws MalformedURLException, ServiceException {
    return annotationService;
  }

  public void setAnnotationService(final AnnotationService annotationService) {
    this.annotationService = annotationService;
  }

  public CreateAnnotationAction getCreateAnnotationAction() {
    return createAnnotationAction;
  }

  public void setCreateAnnotationAction(final CreateAnnotationAction createAnnotationAnnotationAction) {
    this.createAnnotationAction = createAnnotationAnnotationAction;
  }

  public DeleteAnnotationAction getDeleteAnnotationAction() {
    return deleteAnnotationAction;
  }

  public void setDeleteAnnotationAction(final DeleteAnnotationAction deleteAnnotationAnnotationAction) {
    this.deleteAnnotationAction = deleteAnnotationAnnotationAction;
  }

  public ListAnnotationAction getListAnnotationAction() {
    return listAnnotationAction;
  }

  public void setListAnnotationAction(final ListAnnotationAction listAnnotationAnnotationAction) {
    this.listAnnotationAction = listAnnotationAnnotationAction;
  }

  public ListReplyAction getListReplyAction() {
    return listReplyAction;
  }

  public void setListReplyAction(final ListReplyAction listReplyAction) {
    this.listReplyAction = listReplyAction;
  }

  public DeleteReplyAction getDeleteReplyAction() {
    return deleteReplyAction;
  }

  public void setDeleteReplyAction(final DeleteReplyAction deleteReplyAction) {
    this.deleteReplyAction = deleteReplyAction;
  }

  public CreateReplyAction getCreateReplyAction() {
    return createReplyAction;
  }

  public void setCreateReplyAction(final CreateReplyAction createReplyAction) {
    this.createReplyAction = createReplyAction;
  }

  public void setAnnotationBodyFetcherAction(final BodyFetchAction bodyFetchAction) {
    this.bodyFetchAction = bodyFetchAction;
  }

  public BodyFetchAction getAnnotationBodyFetcherAction() {
    return bodyFetchAction;
  }

  public GetAnnotationAction getGetAnnotationAction() {
    return getAnnotationAction;
  }

  public void setGetAnnotationAction(final GetAnnotationAction getAnnotationAction) {
    this.getAnnotationAction = getAnnotationAction;
  }

  public GetFlagAction getGetFlagAction() {
    return getFlagAction;
  }

  public void setGetFlagAction(final GetFlagAction getFlagAction) {
    this.getFlagAction = getFlagAction;
  }

  public GetReplyAction getGetReplyAction() {
    return getReplyAction;
  }

  public void setGetReplyAction(final GetReplyAction getReplyAction) {
    this.getReplyAction = getReplyAction;
  }

  /**
   * @return Returns the bodyFetchAction.
   */
  public BodyFetchAction getBodyFetchAction() {
    return bodyFetchAction;
  }

  /**
   * @param bodyFetchAction The bodyFetchAction to set.
   */
  public void setBodyFetchAction(BodyFetchAction bodyFetchAction) {
    this.bodyFetchAction = bodyFetchAction;
  }

  /**
   * @return Returns the memberUserProfileAction.
   */
  public MemberUserProfileAction getMemberUserProfileAction() {
    return memberUserProfileAction;
  }

  /**
   * @param memberUserProfileAction The memberUserProfileAction to set.
   */
  public void setMemberUserProfileAction(MemberUserProfileAction memberUserProfileAction) {
    this.memberUserProfileAction = memberUserProfileAction;
  }

  /**
   * Getter for adminUserProfileAction.
   * @return Value of adminUserProfileAction.
   */
  public AdminUserProfileAction getAdminUserProfileAction() {
    return adminUserProfileAction;
  }

  /**
   * Setter for adminUserProfileAction.
   * @param adminUserProfileAction Value to set for adminUserProfileAction.
   */
  public void setAdminUserProfileAction(final AdminUserProfileAction adminUserProfileAction) {
    this.adminUserProfileAction = adminUserProfileAction;
  }

  /**
   * @return Returns the displayUserAction.
   */
  public DisplayUserAction getDisplayUserAction() {
    return displayUserAction;
  }

  /**
   * @param displayUserAction The displayUserAction to set.
   */
  public void setDisplayUserAction(DisplayUserAction displayUserAction) {
    this.displayUserAction = displayUserAction;
  }

  /**
   * @return returns the fetchObjectAction
   */
  public FetchObjectAction getFetchObjectAction() {
    return fetchObjectAction;
  }

  /**
   * @param fetchObjectAction set the fetchObjectAction
   */
  public void setFetchObjectAction(final FetchObjectAction fetchObjectAction) {
    this.fetchObjectAction = fetchObjectAction;
  }

  /**
   * @return the SecondaryObjectAction
   */
  public SecondaryObjectAction getSecondaryObjectAction() {
    return secondaryObjectAction;
  }

  /**
   * @param secondaryObjectAction secondaryObjectAction
   */
  public void setSecondaryObjectAction(final SecondaryObjectAction secondaryObjectAction) {
    this.secondaryObjectAction = secondaryObjectAction;
  }

  /**
   * @return the CreateFlagAction
   */
  public CreateFlagAction getCreateFlagAction() {
    return createFlagAction;
  }

  /**
   * @param createFlagAction createFlagAction
   */
  public void setFlagAnnotationAction(final CreateFlagAction createFlagAction) {
    this.createFlagAction = createFlagAction;
  }

  /**
   * @return Returns the userService.
   */
  public UserService getUserService() {
    return userService;
  }

  /**
   * @param userService The userService to set.
   */
  public void setUserService(UserService userService) {
    this.userService = userService;
  }

  protected URL getAsUrl(final String resourceToIngest) throws MalformedURLException {
    URL article = getClass().getResource(resourceToIngest);
    if (null == article) {
      article = new File(resourceToIngest).toURL();
    }
    return article;
  }

  /** @return the DeleteFlagAction */
  public DeleteFlagAction getDeleteFlagAction() {
    return deleteFlagAction;
  }

  /**
   * Set the DeleteFlagAction
   * @param deleteFlagAction deleteFlagAction
   */
  public void setDeleteFlagAction(final DeleteFlagAction deleteFlagAction) {
    this.deleteFlagAction = deleteFlagAction;
  }

  /**
   * @return the unflagAnnotationAction
   */
  public UnflagAnnotationAction getUnflagAnnotationAction() {
    return unflagAnnotationAction;
  }

  /** Set the unflagAnnotationAction */
  public void setUnflagAnnotationAction(final UnflagAnnotationAction unflagAnnotationAction) {
    this.unflagAnnotationAction = unflagAnnotationAction;
  }

  /** Get the listFlagAction */
  public ListFlagAction getListFlagAction() {
    return listFlagAction;
  }

  /** Set the listFlagAction */
  public void setListFlagAction(final ListFlagAction listFlagAction) {
    this.listFlagAction = listFlagAction;
  }

  /** Set the assignAdminRoleAction */
  public void setAssignAdminRoleAction(final AssignAdminRoleAction assignAdminRoleAction) {
    this.assignAdminRoleAction = assignAdminRoleAction;
  }

  /** Get the assignAdminRoleAction */
  protected AssignAdminRoleAction getAssignAdminRoleAction() {
    return assignAdminRoleAction;
  }

  /**
   * Setter for property 'memberUserAlertsAction'.
   * @param memberUserAlertsAction Value to set for property 'memberUserAlertsAction'.
   */
  public void setMemberUserAlertsAction(final MemberUserAlertsAction memberUserAlertsAction) {
    this.memberUserAlertsAction = memberUserAlertsAction;
  }

  /** @return Value for property 'memberUserAlertsAction'. */
  public MemberUserAlertsAction getMemberUserAlertsAction() {
    return memberUserAlertsAction;
  }

  /**
   * Getter for adminUserAlertsAction.
   * @return Value of adminUserAlertsAction.
   */
  public AdminUserAlertsAction getAdminUserAlertsAction() {
    return adminUserAlertsAction;
  }

  /**
   * Setter for adminUserAlertsAction.
   * @param adminUserAlertsAction Value to set for adminUserAlertsAction.
   */
  public void setAdminUserAlertsAction(final AdminUserAlertsAction adminUserAlertsAction) {
    this.adminUserAlertsAction = adminUserAlertsAction;
  }

  /**
   * @return searchAction
   */
  public SearchAction getSearchAction() {
    return searchAction;
  }

  /**
   * Set searchAction
   * @param searchAction searchAction
   */
  public void setSearchAction(final SearchAction searchAction) {
    this.searchAction = searchAction;
  }

  /**
   * Getter for profanityCheckingService.
   * @return Value of profanityCheckingService.
   */
  public ProfanityCheckingService getProfanityCheckingService() {
    return profanityCheckingService;
  }

  /**
   * Setter for profanityCheckingService.
   * @param profanityCheckingService Value to set for profanityCheckingService.
   */
  public void setProfanityCheckingService(final ProfanityCheckingService profanityCheckingService) {
    this.profanityCheckingService = profanityCheckingService;
  }

  /** return SearchUserAction */
  public SearchUserAction getSearchUserAction() {
    return searchUserAction;
  }

  /** set SearchUserAction */
  public void setSearchUserAction(final SearchUserAction searchUserAction) {
    this.searchUserAction = searchUserAction;
  }
}
