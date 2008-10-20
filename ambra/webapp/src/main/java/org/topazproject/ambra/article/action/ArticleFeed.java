/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2007 by Topaz, Inc.
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
package org.topazproject.ambra.article.action;

import java.io.UnsupportedEncodingException;
import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.ServletActionContext;
import org.jdom.Element;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Required;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.action.BaseActionSupport;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.cache.Cache;
import org.topazproject.ambra.cache.AbstractObjectListener;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Category;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.DublinCore;
import org.topazproject.ambra.models.Representation;
import org.topazproject.ambra.models.UserProfile;
import org.topazproject.ambra.models.Journal;
import org.topazproject.ambra.util.ArticleXMLUtils;
import org.topazproject.ambra.web.VirtualJournalContext;
import org.topazproject.otm.Session;
import org.topazproject.otm.ClassMetadata;
import org.topazproject.otm.Interceptor;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;

/**
 * Get a variety of Article Feeds.
 *
 * @author Jeff Suttor
 * @author Eric Brown
 */
public class ArticleFeed extends BaseActionSupport {

  private ArticleOtmService articleOtmService;
  private Cache feedCache;

  // WebWorks will set from URI param
  private String startDate;
  private String endDate;
  private String category;
  private String author;
  private int maxResults = -1;
  private boolean relativeLinks = false;
  private boolean extended = false;
  private String title;
  private String selfLink;

  // WebWorks AmbraFeedResult parms
  private WireFeed wireFeed;

  private JournalService journalService;
  private static Invalidator invalidator;


  /**
   * Ambra Configuration
   */
  private static final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

  private static final Log log = LogFactory.getLog(ArticleFeed.class);

  private static final String ATOM_NS = "http://www.w3.org/2005/Atom"; // Tmp hack for categories

  private ArticleXMLUtils articleXmlUtils;

  /**
   * Returns a feed based on interpreting the URI.
   *
   * @return webwork status code
   * @throws Exception Exception
   */
  @Transactional(readOnly = true)
  public String execute() throws Exception {

    // use native HTTP to avoid WebWorks
    HttpServletRequest request = ServletActionContext.getRequest();
    String pathInfo = request.getPathInfo();
    final URI uri = (pathInfo == null) ? URI.create("/") : URI.create(pathInfo);

    int feedDuration = 3;
    try {
      feedDuration = Integer.valueOf(journalConfGetString(configuration, getCurrentJournal(),
                                                          "ambra.services.feed.defaultDuration", "3"));
    } catch (NumberFormatException nfe) {
      log.error("NumberFormatException occurred parsing ambra.services.feed.defaultDuration from configuration", nfe);
    }

    // Compute startDate default as it is needed to compute cache-key (its default is tricky)
    if (startDate == null) {
        // default is to go back <= N months
        GregorianCalendar monthsAgo = new GregorianCalendar();
        monthsAgo.add(Calendar.MONTH, -feedDuration);
        monthsAgo.set(Calendar.HOUR_OF_DAY, 0);
        monthsAgo.set(Calendar.MINUTE, 0);
        monthsAgo.set(Calendar.SECOND, 0);
        startDate = monthsAgo.getTime().toString();
    }
    if (startDate.length() == 0)
      startDate = null; // shortuct for no startDate, show all articles
    if (log.isDebugEnabled()) {
      log.debug("generating feed w/startDate=" + startDate);
    }

    // Get feed if cached or generate feed by querying OTM
    Key cacheKey = new Key(getCurrentJournal(), ArticleOtmService.parseDateParam(startDate), ArticleOtmService.parseDateParam(endDate) ,category, author, maxResults, relativeLinks, extended, title, selfLink);


    wireFeed = feedCache.get(cacheKey, -1,
        new Cache.SynchronizedLookup<WireFeed, ApplicationException>(cacheKey) {
          public WireFeed lookup() throws ApplicationException {
            return getFeed(uri);
          }
        });

    // Action response type is AmbraFeedResult, it will return wireFeed as a response.

    // tell WebWorks success
    return SUCCESS;
  }

  /*
   * Parse the URI to generate an OTM query.  Return the results as a WireFeed.
   *
   * @parm uri URI to parse to generate an OTM query.
   * @return Query results as a WireFeed.
   */
  private WireFeed getFeed(URI uri) throws ApplicationException {

    // get default values from config file
    final String journal = getCurrentJournal();
    // must end w/trailing slash
    String JOURNAL_URI  = configuration.getString("ambra.virtualJournals." + journal + ".url",
      configuration.getString("ambra.platform.webserver-url", "http://plosone.org/"));
    if (!JOURNAL_URI.endsWith("/")) {
        JOURNAL_URI += "/";
    }
    final String JOURNAL_NAME = journalConfGetString(configuration, journal,
            "ambra.platform.name", "Public Library of Science");
    final String JOURNAL_EMAIL_GENERAL = journalConfGetString(configuration, journal,
            "ambra.platform.email.general", "webmaster@plos.org");
    final String JOURNAL_COPYRIGHT = journalConfGetString(configuration, journal,
            "ambra.platform.copyright",
            "This work is licensed under a Creative Commons Attribution-Share Alike 3.0 License, http://creativecommons.org/licenses/by-sa/3.0/");
    final String FEED_TITLE = journalConfGetString(configuration, journal,
            "ambra.services.feed.title", "PLoS ONE");
    final String FEED_TAGLINE = journalConfGetString(configuration, journal,
            "ambra.services.feed.tagline", "Publishing science, accelerating research");
    final String FEED_ICON = journalConfGetString(configuration, journal,
            "ambra.services.feed.icon", JOURNAL_URI + "images/favicon.ico");
    final String FEED_ID = journalConfGetString(configuration, journal,
            "ambra.services.feed.id", "info:doi/10.1371/feed.pone");
    final String FEED_EXTENDED_NS = journalConfGetString(configuration, journal,
            "ambra.services.feed.extended.namespace", "http://www.plos.org/atom/ns#plos");
    final String FEED_EXTENDED_PREFIX = journalConfGetString(configuration, journal,
            "ambra.services.feed.extended.prefix", "plos");

    // use WebWorks to get Action URIs
    // TODO: WebWorks ActionMapper is broken, hand-code URIs
    final String fetchObjectAttachmentAction = "article/fetchObjectAttachment.action";

    // Atom 1.0 is default
    Feed feed = new Feed("atom_1.0");

    feed.setEncoding("UTF-8");
    feed.setXmlBase(JOURNAL_URI);

    String xmlBase = (relativeLinks ? "" : JOURNAL_URI);
    if (selfLink == null || selfLink.equals("")) {
      if (uri.toString().startsWith("/")) {
        selfLink = JOURNAL_URI.substring(0, JOURNAL_URI.length() - 1) + uri;
      } else {
        selfLink = JOURNAL_URI + uri;
      }
    }

    // must link to self
    Link self = new Link();
    self.setRel("self");
    self.setHref(selfLink);
    self.setTitle(FEED_TITLE);
    List<Link> otherLinks = new ArrayList<Link>();
    otherLinks.add(self);
    feed.setOtherLinks(otherLinks);

    String id = FEED_ID;
    if (category != null && category.length() > 0)
      id += "?category=" + category;
    if (author != null)
      id += "?author=" + author;
    feed.setId(id);

    if (title != null)
      feed.setTitle(title);
    else {
      String feedTitle = FEED_TITLE;
      if (category != null && category.length() > 0)
        feedTitle += " - Category " + category;
      if (author != null)
        feedTitle += " - Author " + author;
      feed.setTitle(feedTitle);
    }

    Content tagline = new Content();
    tagline.setValue(FEED_TAGLINE);
    feed.setTagline(tagline);
    feed.setUpdated(new Date());
    // TODO: bug in Rome ignores icon/logo :(
    feed.setIcon(FEED_ICON);
    feed.setLogo(FEED_ICON);
    feed.setCopyright(JOURNAL_COPYRIGHT);

    // make PLoS the author of the feed
    Person plos = new Person();
    plos.setEmail(JOURNAL_EMAIL_GENERAL);
    plos.setName(JOURNAL_NAME);
    plos.setUri(JOURNAL_URI);
    List<Person> feedAuthors = new ArrayList<Person>();
    feedAuthors.add(plos);
    feed.setAuthors(feedAuthors);

    // build up OTM query, take URI params first, then sensible defaults

    // ignore endDate, default is null

    // was category= URI param specified?
    List<String> categoriesList = new ArrayList<String>();
    if (category != null && category.length() > 0) {
      categoriesList.add(category);
      if (log.isDebugEnabled()) {
        log.debug("generating feed w/category=" + category);
      }
    }

    // was author= URI param specified?
    List<String> authorsList = new ArrayList<String>();
    if (author != null) {
      authorsList.add(author);
      if (log.isDebugEnabled()) {
        log.debug("generating feed w/author=" + author);
      }
    }

    // was maxResults= URI param specified?
    if (maxResults <= 0) {
      maxResults = 30;  // default
    }

    List<Article> articles;
    try {
      articles = articleOtmService.getArticles(
        startDate,             // start date
        endDate,               // end date
        categoriesList.toArray(new String[categoriesList.size()]),  // categories
        authorsList.toArray(new String[authorsList.size()]),        // authors
        Article.ACTIVE_STATES, // states
        false,                 // sort by descending date
        maxResults);           // max results
    } catch (ParseException ex) {
      throw new ApplicationException(ex);
    }

    if (log.isDebugEnabled()) {
      log.debug("feed query returned " + articles.size() + " articles");
    }

    // add each Article as a Feed Entry
    List<Entry> entries = new ArrayList<Entry>();
    for (Article article : articles) {
      Entry entry = new Entry();
      DublinCore dc = article.getDublinCore();

      // TODO: how much more meta-data is possible
      // e.g. article.getDc_type(), article.getFormat(), etc.

      entry.setId(dc.getIdentifier());

      // respect Article specific rights
      String rights = dc.getRights();
      if (rights != null) {
        entry.setRights(rights);
      } else {
        // default is CC BY SA 3.0
        entry.setRights(JOURNAL_COPYRIGHT);
      }

      entry.setTitle(dc.getTitle());
      entry.setPublished(dc.getAvailable());
      entry.setUpdated(dc.getAvailable());

      // links
      List<Link> altLinks = new ArrayList<Link>();

      // must link to self, do it first so link is favored
      Link entrySelf = new Link();
      entrySelf.setRel("alternate");
      try {
        entrySelf.setHref(xmlBase + "article/" + URLEncoder.encode(dc.getIdentifier(), "UTF-8"));
      } catch(UnsupportedEncodingException uee) {
        entrySelf.setHref(xmlBase + "article/" + dc.getIdentifier());
        log.error("UTF-8 not supported?", uee);
      }
      entrySelf.setTitle(dc.getTitle());
      altLinks.add(entrySelf);

      // alternative representation links
      Set<Representation> representations = article.getRepresentations();
      if (representations != null) {
        for (Representation representation : representations) {
          Link altLink = new Link();
          altLink.setHref(xmlBase + fetchObjectAttachmentAction + "?uri=" + dc.getIdentifier() + "&representation=" + representation.getName());
          altLink.setRel("related");
          altLink.setTitle("(" + representation.getName() + ") " + dc.getTitle());
          altLink.setType(representation.getContentType());
          altLinks.add(altLink);
        }
      }

      // set all alternative links
      entry.setAlternateLinks(altLinks);

      // Authors
      String authorNames = ""; // Sometimes added to article content for feed
      List<Person> authors = new ArrayList<Person>();
      Citation bc = article.getDublinCore().getBibliographicCitation();
      if (bc != null) {
        List<UserProfile> authorProfiles = bc.getAuthors();
        for (UserProfile profile: authorProfiles) {
          Person person = new Person();
          person.setName(profile.getRealName());
          authors.add(person);

          if (authorNames.length() > 0)
            authorNames += ", ";
          authorNames += profile.getRealName();
        }
      } else // This should only happen for older, unmigrated articles
        log.warn("No bibliographic citation (is article '" + article.getId() + "' migrated?)");

      // We only want one author on the regular feed
      if (extended)
        entry.setAuthors(authors);
      else if (authors.size() >= 1) {
        List<Person> oneAuthor = new ArrayList<Person>(1);
        String name = authors.get(0).getName();
        Person person = new Person();
        if (authors.size() > 1)
          person.setName(name + " et al.");
        else
          person.setName(name);
        oneAuthor.add(person);
        entry.setAuthors(oneAuthor);
      }

      // Contributors - TODO: Get ordered list when available
      List<Person> contributors = new ArrayList<Person>();
      for (String contributor: article.getDublinCore().getContributors()) {
        Person person = new Person();
        person.setName(contributor);
        contributors.add(person);
      }
      entry.setContributors(contributors);

      // Add foreign markup
      if (extended) {

        // All our foreign markup
        List<Element> foreignMarkup = new ArrayList<Element>();

        // Volume & issue
        if (extended && bc != null) {
          // Add volume
          if (bc.getVolume() != null) {
            Element volume = new Element("volume", FEED_EXTENDED_PREFIX, FEED_EXTENDED_NS);
            volume.setText(bc.getVolume());
            foreignMarkup.add(volume);
          }
          // Add issue
          if (bc.getIssue() != null) {
            Element issue = new Element("issue", FEED_EXTENDED_PREFIX, FEED_EXTENDED_NS);
            issue.setText(bc.getIssue());
            foreignMarkup.add(issue);
          }
        }

        Set<Category> categories = article.getCategories();
        if (categories != null) {
          for (Category category : categories) {
            // TODO: How can we get NS to be automatically filled in from Atom?
            Element feedCategory = new Element("category", ATOM_NS);
            feedCategory.setAttribute("term", category.getMainCategory());
            // TODO: what's the URI for our categories
            // feedCategory.setScheme();
            feedCategory.setAttribute("label", category.getMainCategory());

            // subCategory?
            String subCategory = category.getSubCategory();
            if (subCategory != null) {
              Element feedSubCategory = new Element("category", ATOM_NS);
              feedSubCategory.setAttribute("term", subCategory);
              // TODO: what's the URI for our categories
              // feedSubCategory.setScheme();
              feedSubCategory.setAttribute("label", subCategory);
              feedCategory.addContent(feedSubCategory);
            }

            foreignMarkup.add(feedCategory);
          }
        }

        if (foreignMarkup.size() > 0) {
          entry.setForeignMarkup(foreignMarkup);
        }
      }

      // atom:content
      List <Content> contents = new ArrayList<Content>();
      Content description = new Content();
      description.setType("html");
      try {
        StringBuffer text = new StringBuffer();
        // If this is a nomral feed (not extended) and there's more than one author, add to content
        if ((!extended) && authors.size() > 1) {
          text.append("<p>by ").append(authorNames).append("</p>\n");
        }
        if (dc.getDescription() != null) {
          text.append(articleXmlUtils.transformArticleDescriptionToHtml(dc.getDescription()));
        }
        description.setValue(text.toString());
      } catch (Exception e) {
        log.error(e);
        description.setValue("<p>Internal server error.</p>");
        // keep generating feed
      }
      contents.add(description);
      entry.setContents(contents);

      // add completed Entry to List
      entries.add(entry);
    }

    // set feed entries to the articles
    feed.setEntries(entries);

   return feed;
  }

  /**
   * Set articleOtmService
   *
   * @param articleOtmService articleOtmService
   */
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  /**
   * Set ehcache instance via spring
   *
   * @param feedCache the ehcache instance
   */
  public void setFeedCache(Cache feedCache) {
    this.feedCache = feedCache;
    if (invalidator == null) {
      invalidator = new Invalidator();
      this.feedCache.getCacheManager().registerListener(invalidator);
    }
  }

  /**
   * Allow WebWorks to get the param wireFeed
   *
   * @return The WireFeed.
   */
  public WireFeed getWireFeed() {
    return wireFeed;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setStartDate(final String startDate) {
    this.startDate = startDate;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setEndDate(final String endDate) {
    this.endDate = endDate;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setCategory(final String category) {
    this.category = category;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setAuthor(final String author) {
    this.author = author;
  }

  /**
   * WebWorks will set from URI param.
   */
  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setRelativeLinks(final boolean relativeLinks) {
    this.relativeLinks = relativeLinks;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setExtended(final boolean extended) {
    this.extended = extended;
  }

  /**
   * WebWroks will set from URI param
   */
  public void setTitle(final String title) {
    this.title = title;
  }

  /**
   * WebWorks will set from URI param
   */
  public void setSelfLink(final String selfLink) {
    this.selfLink = selfLink;
  }

  /**
   *  Get a String from the Configuration looking first for a Journal override.
   *
   * @param configuration to use.
   * @param journal name.
   * @param key to lookup.
   * @param defaultValue if key is not found.
   * @return value for key.
   */
  private String journalConfGetString(Configuration configuration, String journal, String key,
          String defaultValue) {
    return configuration.getString("ambra.virtualJournals." + journal + "." + key,
            configuration.getString(key, defaultValue));
  }

  private String getCurrentJournal() {
    return ((VirtualJournalContext) ServletActionContext.getRequest().
      getAttribute(VirtualJournalContext.PUB_VIRTUALJOURNAL_CONTEXT)).getJournal();
  }

  /**
   * Called by Spring to initialize an articleXmlUtils reference.
   *
   * @param articleXmlUtils The articleXmlUtils to set.
   */
  public void setArticleXmlUtils(ArticleXMLUtils articleXmlUtils) {
    this.articleXmlUtils = articleXmlUtils;
  }

  /**
   * @param journalService the journal service to use
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * Cache key for article feeds.
   */
  public class Key implements Serializable, Comparable {

    private String journal;
    private Date startDate;
    private Date endDate;
    private String category;
    private String author;
    private int count;
    private boolean relativeLinks;
    private boolean extended;
    private String title;
    private String selfLink;

    private int hashCode;

    public Key(String journal, Date startDate, Date endDate, String category, String author, int count, boolean relativeLinks, boolean extended, String title, String selfLink) {
      this.journal = journal;
      this.startDate = startDate;
      this.endDate = endDate;
      if (category != null && category.length() > 0)
        this.category = category;
      if (author != null && author.length() > 0)
        this.author = author.trim();
      this.count = count;
      this.relativeLinks = relativeLinks;
      this.extended = extended;
      if (title != null && title.length() > 0)
        this.title = title;
      if (selfLink != null && selfLink.length() > 0)
        this.selfLink = selfLink;

      this.hashCode = calculateHashKey();
    }

    private int calculateHashKey() {
      int hash = 0;
      if (this.journal != null)
        hash += this.journal.hashCode();
      if (this.startDate != null)
        hash += this.startDate.hashCode();
      if (this.endDate != null)
        hash += this.endDate.hashCode();
      if (this.category != null)
        hash += this.category.hashCode();
      if (this.author != null)
        hash += this.author.hashCode();
      hash += this.count;
      hash += this.relativeLinks ? 1 : 0;
      hash += this.extended ? 1 : 0;
      if (this.title != null)
        hash += this.title.hashCode();
      if (this.selfLink != null)
        hash += this.selfLink.hashCode();

      return hash;
    }

    public String getJournal() {
      return journal;
    }

    public Date getStartDate() {
      return startDate;
    }

    public Date getEndDate() {
      return endDate;
    }

    public String getCategory() {
      return category;
    }

    public String getAuthor() {
      return author;
    }

    public int getCount() {
      return count;
    }

    public boolean isRelativeLinks() {
      return relativeLinks;
    }

    public boolean isExtended() {
      return extended;
    }

    public String getTitle() {
      return title;
    }

    public String getSelfLink() {
      return selfLink;
    }

    @Override
    public int hashCode() {
      return this.hashCode;
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || !(o instanceof Key)) return false;
      Key key = (Key) o;
      return (
        key.hashCode == this.hashCode
        &&
        (key.getJournal() == null && this.journal == null
            || key.getJournal() != null && key.getJournal().equals(this.journal))
        &&
        (key.getStartDate() == null && this.startDate == null
            || key.getStartDate() != null && key.getStartDate().equals(this.startDate))
        &&
        (key.getEndDate() == null && this.endDate == null
            || key.getEndDate() != null && key.getEndDate().equals(this.endDate))
        &&
        (key.getCategory() == null && this.category == null
            || key.getCategory() != null && key.getCategory().equals(this.category))
        &&
        key.isExtended() == this.extended
        &&
        key.isRelativeLinks() == this.relativeLinks
        &&
        (key.getAuthor() == null && this.author == null
            || key.getAuthor() != null && key.getAuthor().equals(this.author))
        &&
        key.getCount() == this.count
        &&
        (key.getSelfLink() == null && this.selfLink == null
            || key.getSelfLink() != null && key.getSelfLink().equals(this.selfLink))
        &&
        (key.getTitle() == null && this.title == null
            || key.getTitle() != null && key.getTitle().equals(this.title))
      );
    }


    @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("journal=");
      builder.append(journal);
      if (startDate != null) {
        builder.append("; startDate=");
        builder.append(startDate);
      }
      if (endDate != null) {
        builder.append("; endDate=");
        builder.append(endDate);
      }
      if (category != null) {
        builder.append("; category=");
        builder.append(category);
      }
      if (extended)
        builder.append("; extended=true");
      if (relativeLinks)
        builder.append("; relativeLinks=true");
      if (author != null) {
        builder.append("; author=");
        builder.append(author);
      }
      if (title != null) {
        builder.append("; title=");
        builder.append(title);
      }
      if (count != -1) {
        builder.append("; count=");
        builder.append(count);
      }
      if (selfLink != null) {
        builder.append("; selfLink=");
        builder.append(selfLink);
      }

      return builder.toString();
    }

    public int compareTo(Object o) {
      if (o == null)
        return 1;
      return toString().compareTo(o.toString());
    }
  }

  /**
   * Invalidate feedCache on every injest or delete
   */
  private class Invalidator extends AbstractObjectListener {

    @Override
    public void objectChanged(Session session, ClassMetadata cm, String id, Object object,
                              Interceptor.Updates updates) {
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE) {
        invalidateFeedCacheForArticle((Article)object);
      }
      else if (object instanceof Journal) {
        invalidateFeedCacheForJournal((Journal)object, updates);
      }
    }

    @Override
    public void removing(Session session, ClassMetadata cm, String id, Object object) throws Exception {
      if (object instanceof Article && ((Article) object).getState() == Article.STATE_ACTIVE)
        invalidateFeedCacheForArticle((Article)object);
    }

    private void invalidateFeedCacheForJournal(Journal journal, Interceptor.Updates updates) {
      List<Article> articles = new ArrayList<Article>();

      for (URI articleKey : journal.getSimpleCollection()) {
        try {
          articles.add(articleOtmService.getArticle(articleKey));
        } catch (NoSuchArticleIdException e) {
          if (log.isDebugEnabled())
            log.debug("Ignoring "+articleKey);
        }
      }

      List<String> oldArticles = updates.getOldValue("simpleCollection");
      if (oldArticles != null) {
        for (String oldArticleUri : oldArticles) {
          try {
            articles.add(articleOtmService.getArticle(URI.create(oldArticleUri)));
          } catch (NoSuchArticleIdException e) {
            if (log.isDebugEnabled())
              log.debug("Ignoring old "+oldArticleUri);
          }
        }
      }

      if (!articles.isEmpty())
        invalidateFeedCacheForJournalArticle(journal, articles);

    }

    private void invalidateFeedCacheForArticle(Article article) {
      for (Key key : (Set<Key>) feedCache.getKeys()) {
        if (matches(key, article, true))
          feedCache.remove(key);
      }
    }

    private void invalidateFeedCacheForJournalArticle(Journal journal, List<Article> articles) {
      for (Key key : (Set<Key>) feedCache.getKeys()) {
        if (key.getJournal().equals(journal.getKey())) {
          for (Article article : articles) {
            if (matches(key, article, false))
              feedCache.remove(key);
          }
        }
      }
    }

    private boolean matches(ArticleFeed.Key key, Article article, boolean checkJournal) {

      if (checkJournal && matchesJournal(key, article))
        return false;

      DublinCore dc = article.getDublinCore();

      if (matchesDates(key, dc))
        return false;

      if (matchesCategory(key, article))
        return false;

      if (matchesAuthor(key, dc))
        return false;

      if (matchesTitle(key, dc))
        return false;

      return true;
    }

    private boolean matchesTitle(Key key, DublinCore dc) {
      return key.getTitle() != null && !key.getTitle().equalsIgnoreCase(dc.getTitle());
    }

    private boolean matchesAuthor(Key key, DublinCore dc) {
      if (key.getAuthor() != null) {
        boolean matches = false;
        for (String author : dc.getCreators()) {
          if (key.getAuthor().equalsIgnoreCase(author)) {
            matches = true;
            break;
          }
        }

        // author from the key is not found in the article
        if (!matches)
          return true;
      }
      return false;
    }

    private boolean matchesCategory(Key key, Article article) {
      if (key.getCategory() != null) {
        boolean matches = false;
        for (Category category : article.getCategories()) {
          if (category.getMainCategory().equals(key.getCategory())) {
            matches = true;
            break;
          }
        }

        // article is not in the category specified in the cache key
        if (!matches)
          return true;
      }
      return false;
    }

    private boolean matchesDates(Key key, DublinCore dc) {
      Date articleDate = dc.getDate();
      if (articleDate != null) {
        if (key.getStartDate() != null && key.getStartDate().after(articleDate))
          return true;
        if (key.getEndDate() != null && key.getEndDate().before(articleDate))
          return true;
      }
      return false;
    }

    private boolean matchesJournal(Key key, Article article) {
      if (key.getJournal() != null) {
        boolean matches = false;
        for (Journal journal : journalService.getJournalsForObject(article.getId())) {
          if (journal.getKey().equals(key.getJournal())) {
            matches = true;
            break;
          }
        }

        if (!matches)
          return true;
      }
      return false;
    }
  }

}
