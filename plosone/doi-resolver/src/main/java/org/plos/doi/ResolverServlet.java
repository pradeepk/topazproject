/**
 *
 */
package org.plos.doi;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * @author Stephen Cheng
 *
 */
public class ResolverServlet extends HttpServlet{
  private static final Log log = LogFactory.getLog(ResolverServlet.class);
  private static final Configuration myConfig = ConfigurationStore.getInstance().getConfiguration();
  /*private static final Pattern journalRegEx = Pattern.compile("/10\\.1371/journal\\.pone\\.\\d{7}");
  private static final Pattern figureRegEx = Pattern.compile("/10\\.1371/journal\\.pone\\.\\d{7}\\.[gt]\\d{3}");*/
  private static final String RDF_TYPE_ARTICLE = "http://rdf.topazproject.org/RDF/Article";
  private static final Pattern[] journalRegExs;
  private static final Pattern[] figureRegExs;
  private static final String[] urls;
  private static final int numJournals;
  private static final String errorPage;

  static {
    numJournals = myConfig.getList("pub.doi-journals.journal.url").size();
    urls = new String[numJournals];
    figureRegExs = new Pattern[numJournals];
    journalRegExs = new Pattern[numJournals];
    for (int i = 0; i < numJournals; i++) {
      urls[i] = myConfig.getString("pub.doi-journals.journal(" + i + ").url");
      StringBuilder pat = new StringBuilder("/").append(myConfig.getString("pub.doi-journals.journal(" + i + ").regex"));
      journalRegExs[i] = Pattern.compile(pat.toString());
      figureRegExs[i] = Pattern.compile(pat.append("\\.[gt]\\d{3}").toString());
    }
    errorPage = myConfig.getString("pub.webserver-url")+ myConfig.getString("pub.error-page");
    if (log.isTraceEnabled()) {
      for (int i = 0; i < numJournals; i++) {
        log.trace("JournalRegEx: " + journalRegExs[i].toString() +
                  "  ; figureRegEx: " + figureRegExs[i].toString() +
                  "  ; url: " + urls[i]);
      }
      log.trace( ("Error Page is: " + errorPage));
    }
  }


  /**
   * Tries to resolve a PLoS ONE doi from CrossRef into an application specific URL
   * First, tries to make sure the DOI looks like it is properly formed.  If it looks
   * like an article DOI, will attempt to do a type lookup in mulgara.  If it looks
   * like a figure or a table, will attempt to construct an article DOI and do that
   * lookup. Otherwise, will fail and send to PLoS ONE Page not Found error page.
   *
   * @param req the servlet request
   * @param resp the servlet response
   *
   */
  public void doGet(HttpServletRequest req, HttpServletResponse resp)  {
    String doi = req.getPathInfo();
    if (log.isTraceEnabled()) {
      log.trace ("Incoming doi = " + doi);
    }
    if (doi == null) {
      failWithError(resp);
      return;
    }
    doi = doi.trim();
    try {
     resp.sendRedirect (constructURL (doi));
    } catch (Exception e){
      log.warn("Could not resolve doi: " + doi, e);
      failWithError(resp);
    }
    return;
  }


  /**
   * Just forwards to the PLoS ONE Page Not Found error page
   *
   * @param req the servlet request
   * @param resp the servlet response
   */
  public void doPost (HttpServletRequest req, HttpServletResponse resp)  {
    failWithError(resp);
    return;
  }

  private String[] lookupDOI (String doi) {
    URI doiURI = null;
    try {
      doiURI = new URI ("info:doi" + doi);
    } catch (URISyntaxException use) {
      log.warn ("Couldn't create URI for doi:" + doi, use);
      return new String[0];
    }
    try {
      DOITypeResolver resolver = new DOITypeResolver(new URI(
                                myConfig.getString("topaz.services.itql.uri")));
      return resolver.getRdfTypes(doiURI);
    } catch (Exception e) {
      log.warn ("Couldn't retrieve rdf types for " + doiURI.toString(), e);
      return new String[0];
    }
  }


  private String constructURL (String doi) {
    StringBuilder redirectURL; //= new StringBuilder(myConfig.getString("pub.webserver-url"));
    String[] rdfTypes;

    Pattern journalRegEx, figureRegEx;

    for (int i = 0; i < numJournals; i++) {
      journalRegEx = journalRegExs[i];
      figureRegEx = figureRegExs[i];

      if (journalRegEx.matcher(doi).matches()) {
        rdfTypes = lookupDOI(doi);
        if (rdfTypes.length > 0) {
          Arrays.sort(rdfTypes);
          if (Arrays.binarySearch(rdfTypes, RDF_TYPE_ARTICLE) >= 0) {
            redirectURL = new StringBuilder(urls[i]);
            redirectURL.append(myConfig.getString("pub.article-action"))
                       .append("info:doi").append(doi);
            if (log.isDebugEnabled()) {
              log.debug ("Matched: " + doi + "; redirecting to: " + redirectURL.toString());
            }
            return redirectURL.toString();
          }
        }
      } else if (figureRegEx.matcher(doi).matches()) {
        String possibleArticleDOI = doi.substring(0, doi.length()-5);
        rdfTypes = lookupDOI(possibleArticleDOI);
        if (rdfTypes.length > 0) {
          Arrays.sort(rdfTypes);
          if (Arrays.binarySearch(rdfTypes, RDF_TYPE_ARTICLE) >= 0) {
            redirectURL = new StringBuilder(urls[i]);
            redirectURL.append(myConfig.getString("pub.figure-action1"))
                       .append("info:doi").append(possibleArticleDOI)
                       .append(myConfig.getString("pub.figure-action2"))
                       .append("info:doi").append(doi);
            if (log.isDebugEnabled()) {
              log.debug ("Matched: " + doi + "; redirecting to: " + redirectURL.toString());
            }
            return redirectURL.toString();
          }
        }
      }
    }
    if (log.isDebugEnabled()) {
      log.debug ("Could not match: " + doi + "; redirecting to: " + errorPage);
    }
    return errorPage;
  }

  private void failWithError(HttpServletResponse resp){
    try {
      resp.sendRedirect(errorPage);
    } catch (Exception e) {
      log.warn ("Couldn't redirect user to error page", e);
    }
  }
}
