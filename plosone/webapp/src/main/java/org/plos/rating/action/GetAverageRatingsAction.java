/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2007 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.rating.action;

import static org.plos.Constants.PLOS_ONE_USER_KEY;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.configuration.OtmConfiguration;
import org.plos.rating.otm.Rating;
import org.plos.rating.otm.RatingSummary;
import org.plos.user.PlosOneUser;
import org.topazproject.otm.OtmException;
import org.topazproject.otm.Session;
import org.topazproject.otm.Transaction;
import org.topazproject.otm.criterion.Restrictions;

import com.opensymphony.webwork.ServletActionContext;

/**
 * @author stevec
 *
 */
public class GetAverageRatingsAction extends BaseActionSupport {
  private OtmConfiguration otmFactory;
  
  private String articleURI;
  
  private double insightAverage;
  private double styleAverage;  
  private double reliabilityAverage;  
  private double overallAverage;  

  private double insightRoundedAverage;
  private double styleRoundedAverage;  
  private double reliabilityRoundedAverage;  
  private double overallRoundedAverage;  
 
  private int numInsightRatings;
  private int numStyleRatings;  
  private int numReliabilityRatings;  
  private int numOverallRatings;  

  private double totalInsight;
  private double totalStyle;
  private double totalReliability;
  private double totalOverall;

  private boolean hasRated = false;;
  
  private static final Log log = LogFactory.getLog(GetAverageRatingsAction.class);
  
  public String execute() {
    Session     session = otmFactory.getFactory().openSession();
    Transaction tx      = null;
    RatingSummary insightSummary = null;
    RatingSummary styleSummary = null; 
    RatingSummary reliabilitySummary = null;    
    RatingSummary overallSummary = null;
    PlosOneUser user = (PlosOneUser) ServletActionContext.getRequest().getSession().getAttribute(PLOS_ONE_USER_KEY);
    
    
    
    try {
      tx = session.beginTransaction();
      
      List summaryList = session.createCriteria(RatingSummary.class).add(Restrictions.eq("annotates", articleURI)).list();
      Iterator iter = summaryList.iterator();             
      
      while (iter.hasNext()) {
        RatingSummary ratingSummary = (RatingSummary)iter.next();

        if (Rating.INSIGHT_TYPE.equals(ratingSummary.getType())) {
          insightSummary= ratingSummary; 
          insightAverage = insightSummary.retrieveAverage();
          insightRoundedAverage = roundTo(insightAverage, 0.5);
          numInsightRatings = insightSummary.retrieveNumRatings();
          totalInsight = insightSummary.retrieveTotal();
        } else if (Rating.STYLE_TYPE.equals(ratingSummary.getType())) { 
          styleSummary= ratingSummary;
          styleAverage = styleSummary.retrieveAverage();
          styleRoundedAverage = roundTo(styleAverage, 0.5);
          numStyleRatings = styleSummary.retrieveNumRatings();
          totalStyle = styleSummary.retrieveTotal();
        } else if (Rating.RELIABILITY_TYPE.equals(ratingSummary.getType())) {
          reliabilitySummary= ratingSummary;
          reliabilityAverage = reliabilitySummary.retrieveAverage();
          reliabilityRoundedAverage = roundTo(reliabilityAverage, 0.5);
          numReliabilityRatings = reliabilitySummary.retrieveNumRatings();
          totalReliability = reliabilitySummary.retrieveTotal();
        } else if (Rating.OVERALL_TYPE.equals(ratingSummary.getType())) {
          overallSummary= ratingSummary;
          overallAverage = overallSummary.retrieveTotal();
          overallRoundedAverage = roundTo(overallAverage, 0.5);
          numOverallRatings = overallSummary.retrieveNumRatings();
          totalOverall = overallSummary.retrieveTotal();
        }
      }
      
      if (user != null) {
        List ratingsList = session.createCriteria(Rating.class).add(Restrictions.eq("annotates", articleURI)).
        add(Restrictions.eq("creator", user.getUserId())).list();
        
        if (ratingsList.size() > 0) {
          hasRated = true;
        }
      }
      tx.commit(); // Flush happens automatically
    } catch (OtmException e) {
      try {
        if (tx != null)
          tx.rollback();
      } catch (OtmException re) {
        log.warn("rollback failed", re);
      }
      throw e; // or display error message
    } finally {
      try {
        session.close();
      } catch (OtmException ce) {
        log.warn("close failed", ce);
      }
    }
    return SUCCESS;
  }

  private double roundTo (double x, double r) {
    if (r == 0) { 
      return x; 
    }
    return Math.round(x * (1 / r)) / (1 / r);
 }
  
  
  /**
   * @return Returns the articleURI.
   */
  public String getArticleURI() {
    return articleURI;
  }

  /**
   * @param articleURI The articleUri to set.
   */
  public void setArticleURI(String articleURI) {
    this.articleURI = articleURI;
  }

  /**
   * @return Returns the insightAverage.
   */
  public double getInsightAverage() {
    return insightAverage;
  }

  /**
   * @param insightAverage The insightAverage to set.
   */
  public void setInsightAverage(double insightAverage) {
    this.insightAverage = insightAverage;
  }

  /**
   * @return Returns the numInsightRatings.
   */
  public int getNumInsightRatings() {
    return numInsightRatings;
  }

  /**
   * @param numInsightRatings The numInsightRatings to set.
   */
  public void setNumInsightRatings(int numInsightRatings) {
    this.numInsightRatings = numInsightRatings;
  }

  /**
   * @return Returns the numOverallRatings.
   */
  public int getNumOverallRatings() {
    return numOverallRatings;
  }

  /**
   * @param numOverallRatings The numOverallRatings to set.
   */
  public void setNumOverallRatings(int numOverallRatings) {
    this.numOverallRatings = numOverallRatings;
  }

  /**
   * @return Returns the numReliabilityRatings.
   */
  public int getNumReliabilityRatings() {
    return numReliabilityRatings;
  }

  /**
   * @param numReliabilityRatings The numReliabilityRatings to set.
   */
  public void setNumReliabilityRatings(int numReliabilityRatings) {
    this.numReliabilityRatings = numReliabilityRatings;
  }

  /**
   * @return Returns the numStyleRatings.
   */
  public int getNumStyleRatings() {
    return numStyleRatings;
  }

  /**
   * @param numStyleRatings The numStyleRatings to set.
   */
  public void setNumStyleRatings(int numStyleRatings) {
    this.numStyleRatings = numStyleRatings;
  }

  /**
   * @return Returns the otmFactory.
   */
  public OtmConfiguration getOtmFactory() {
    return otmFactory;
  }

  /**
   * @param otmFactory The otmFactory to set.
   */
  public void setOtmFactory(OtmConfiguration otmFactory) {
    this.otmFactory = otmFactory;
  }

  /**
   * @return Returns the overallAverage.
   */
  public double getOverallAverage() {
    return overallAverage;
  }

  /**
   * @param overallAverage The overallAverage to set.
   */
  public void setOverallAverage(double overallAverage) {
    this.overallAverage = overallAverage;
  }

  /**
   * @return Returns the reliabilityAverage.
   */
  public double getReliabilityAverage() {
    return reliabilityAverage;
  }

  /**
   * @param reliabilityAverage The reliabilityAverage to set.
   */
  public void setReliabilityAverage(double reliabilityAverage) {
    this.reliabilityAverage = reliabilityAverage;
  }

  /**
   * @return Returns the styleAverage.
   */
  public double getStyleAverage() {
    return styleAverage;
  }

  /**
   * @param styleAverage The styleAverage to set.
   */
  public void setStyleAverage(double styleAverage) {
    this.styleAverage = styleAverage;
  }

  /**
   * @return Returns the totalInsight.
   */
  public double getTotalInsight() {
    return totalInsight;
  }

  /**
   * @param totalInsight The totalInsight to set.
   */
  public void setTotalInsight(double totalInsight) {
    this.totalInsight = totalInsight;
  }

  /**
   * @return Returns the totalOverall.
   */
  public double getTotalOverall() {
    return totalOverall;
  }

  /**
   * @param totalOverall The totalOverall to set.
   */
  public void setTotalOverall(double totalOverall) {
    this.totalOverall = totalOverall;
  }

  /**
   * @return Returns the totalReliability.
   */
  public double getTotalReliability() {
    return totalReliability;
  }

  /**
   * @param totalReliability The totalReliability to set.
   */
  public void setTotalReliability(double totalReliability) {
    this.totalReliability = totalReliability;
  }

  /**
   * @return Returns the totalStyle.
   */
  public double getTotalStyle() {
    return totalStyle;
  }

  /**
   * @param totalStyle The totalStyle to set.
   */
  public void setTotalStyle(double totalStyle) {
    this.totalStyle = totalStyle;
  }

  /**
   * @return Returns the hasRated.
   */
  public boolean isHasRated() {
    return hasRated;
  }

  /**
   * @param hasRated The hasRated to set.
   */
  public void setHasRated(boolean hasRated) {
    this.hasRated = hasRated;
  }

  /**
   * @return Returns the insightRoundedAverage.
   */
  public double getInsightRoundedAverage() {
    return insightRoundedAverage;
  }

  /**
   * @param insightRoundedAverage The insightRoundedAverage to set.
   */
  public void setInsightRoundedAverage(double insightRoundedAverage) {
    this.insightRoundedAverage = insightRoundedAverage;
  }

  /**
   * @return Returns the overallRoundedAverage.
   */
  public double getOverallRoundedAverage() {
    return overallRoundedAverage;
  }

  /**
   * @param overallRoundedAverage The overallRoundedAverage to set.
   */
  public void setOverallRoundedAverage(double overallRoundedAverage) {
    this.overallRoundedAverage = overallRoundedAverage;
  }

  /**
   * @return Returns the reliabilityRoundedAverage.
   */
  public double getReliabilityRoundedAverage() {
    return reliabilityRoundedAverage;
  }

  /**
   * @param reliabilityRoundedAverage The reliabilityRoundedAverage to set.
   */
  public void setReliabilityRoundedAverage(double reliabilityRoundedAverage) {
    this.reliabilityRoundedAverage = reliabilityRoundedAverage;
  }

  /**
   * @return Returns the styleRoundedAverage.
   */
  public double getStyleRoundedAverage() {
    return styleRoundedAverage;
  }

  /**
   * @param styleRoundedAverage The styleRoundedAverage to set.
   */
  public void setStyleRoundedAverage(double styleRoundedAverage) {
    this.styleRoundedAverage = styleRoundedAverage;
  }
}
