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

package org.topazproject.ambra.admin.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.FileDataSource;
import javax.activation.FileTypeMap;
import javax.xml.rpc.ServiceException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.article.service.ArticleOtmService;
import org.topazproject.ambra.article.service.DuplicateArticleIdException;
import org.topazproject.ambra.article.service.FetchArticleService;
import org.topazproject.ambra.article.service.IngestException;
import org.topazproject.ambra.article.service.NoSuchArticleIdException;
import org.topazproject.ambra.article.service.Zip;
import org.topazproject.ambra.journal.JournalService;
import org.topazproject.ambra.models.Article;
import org.topazproject.ambra.models.Journal;
import org.topazproject.xml.transform.cache.CachedSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author alan Manage documents on server. Ingest and access ingested documents.
 */
public class DocumentManagementService {
  private static final Log log = LogFactory.getLog(DocumentManagementService.class);
  private ArticleOtmService articleOtmService;
  private FetchArticleService fetchArticleService;
  private String documentDirectory;
  private String ingestedDocumentDirectory;
  private String documentPrefix;
  private CrossRefPosterService crossRefPosterService;
  private File xslTemplate;
  private JournalService journalService;
  private String plosDoiUrl;
  private String plosEmail;
  private boolean sendToXref;

  private FileTypeMap fileTypeMap = new FileTypeMap() {
    private final Map<String, String> extensions = new HashMap<String, String>();

    {
      extensions.put(".zip",     "application/zip");
      extensions.put(".tar",     "application/x-tar");
      extensions.put(".tar.gz",  "application/x-tar-gz");
      extensions.put(".tgz",     "application/x-tar-gz");
      extensions.put(".tar.bz2", "application/x-tar-bz");
      extensions.put(".tbz2",    "application/x-tar-bz");
      extensions.put(".tbz",     "application/x-tar-bz");
      extensions.put(".tb2",     "application/x-tar-bz");
    }

    @Override
    public String getContentType(File file) {
      return getContentType(file.getName());
    }

    @Override
    public String getContentType(String name) {
      name = name.toLowerCase();
      for (String ext : extensions.keySet())
        if (name.endsWith(ext))
          return extensions.get(ext);
      return "application/octet-stream";
    }
  };

  public DocumentManagementService() {
  }

  public void init() {
  }

  /**
   * Set the article web service
   *
   * @param articleOtmService articleOtmService
   */
  @Required
  public void setArticleOtmService(final ArticleOtmService articleOtmService) {
    this.articleOtmService = articleOtmService;
  }

  @Required
  public void setFetchArticleService(final FetchArticleService fetchArticleService) {
    this.fetchArticleService = fetchArticleService;
  }

  @Required
  public void setDocumentDirectory(final String documentDirectory) {
    this.documentDirectory = documentDirectory;
  }

  @Required
  public void setDocumentPrefix(final String documentPrefix) {
    this.documentPrefix = documentPrefix;
  }

  public String getDocumentDirectory() {
    return documentDirectory;
  }

  @Required
  public void setIngestedDocumentDirectory(final String ingestedDocumentDirectory) {
    this.ingestedDocumentDirectory = ingestedDocumentDirectory;
  }

  @Required
  public void setCrossRefPosterService(final CrossRefPosterService crossRefPosterService) {
    this.crossRefPosterService = crossRefPosterService;
  }

  @Required
  public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
    File file = getAsFile(xslTemplate);
    if (!file.exists()) {
      file = new File(xslTemplate);
    }
    this.xslTemplate = file;
  }

  /**
   * @param filenameOrURL filenameOrURL
   * @return the local or remote file or url as a java.io.File
   * @throws URISyntaxException
   */
  private File getAsFile(final String filenameOrURL) throws URISyntaxException {
    final URL resource = getClass().getResource(filenameOrURL);
    if (null == resource) {
      // access it as a local file resource
      return new File(org.topazproject.ambra.util.FileUtils.getFileName(filenameOrURL));
    } else {
      return new File(resource.toURI());
    }
  }

  /**
   * Deletes an article from Topaz, but does not flush any caches.
   * Useful for deleting a recently ingested article that hasn't been published
   *
   * @param objectURI URI of the article to delete
   * @throws RemoteException on an error from search service
   * @throws ServiceException on an error from search service
   * @throws NoSuchArticleIdException if id is invalid
   */
  @Transactional(rollbackFor = { Throwable.class })
  public void delete(String objectURI)
      throws RemoteException, ServiceException, NoSuchArticleIdException {
    articleOtmService.delete(objectURI);
    URI id = URI.create(objectURI);
    for (Journal j : journalService.getAllJournals()) {
      List<URI> col = j.getSimpleCollection();
      if (col != null)
        while (col.contains(id))
          col.remove(id);
    }
  }

  /**
   * Revert the data out of the ingested queue
   *
   * @param uri the article uri
   *
   * @throws IOException on an error
   */
  public void revertIngestedQueue(String uri) throws IOException {
    // delete any crossref submission file
    File queueDir        = new File(documentDirectory);
    File ingestedDir     = new File(ingestedDocumentDirectory);
    File ingestedXmlFile = new File(ingestedDir, uri.replaceAll("[:/.]", "_") + ".xml");

    if (log.isDebugEnabled())
      log.debug("Deleting '" + ingestedXmlFile + "'");

    try {
      FileUtils.forceDelete(ingestedXmlFile);
    } catch (FileNotFoundException fnfe) {
      log.info("'" + ingestedXmlFile + "' does not exist - cannot delete: ", fnfe);
    }

    // move zip back to ingestion queue
    if (!queueDir.equals(ingestedDir)) {
      String fname = uri.substring(documentPrefix.length()) + ".zip";        // strip 'info:doi/10.1371/journal.'
      File fromFile = new File(ingestedDir, fname);
      File toFile   = new File(queueDir,    fname);

      try {
        if (log.isDebugEnabled())
          log.debug("Moving '" + fromFile + "' to '" + toFile + "'");
        FileUtils.moveFile(fromFile, toFile);
      } catch (FileNotFoundException fnfe) {
        log.info("Could not move '" + fromFile + "' to '" + toFile + "': ", fnfe);
      }
    }
  }

  /**
   * Deletes articles from Topaz and flushes the servlet image cache and article cache
   *
   * @param objectURIs  URIs of the articles to delete
   * @return a list of messages describing what was successful and what failed
   */
  public List<String> delete(String[] objectURIs) {
    List<String> msgs = new ArrayList<String>();
    for (String objectURI : objectURIs) {
      try {
        delete(objectURI);
        msgs.add("Deleted: " + objectURI);
        if (log.isInfoEnabled())
          log.info("Deleted article: " + objectURI);
      } catch (Exception e) {
        log.error("Could not delete article: " + objectURI, e);
        msgs.add("Error deleting: " + objectURI + " - " + e);
      }
    }

    return msgs;
  }

  /**
   * Ingest the file. If successful create the Transformed CrossRef xml file and deposit that in the
   * Directory as well.
   *
   * @param file  file to be ingested
   * @param force if true don't check for duplicate and instead always (re-)ingest
   * @return the ingested article
   * @throws IngestException on an error in ingest
   * @throws DuplicateArticleIdException if the article exists and force is false
   * @throws IOException on any other error
   */
  @Transactional(rollbackFor = { Throwable.class })
  public Article ingest(File file, boolean force)
      throws IngestException, DuplicateArticleIdException, IOException {
    if (log.isInfoEnabled()) {
      log.info("Ingesting: " + file);
    }

    FileDataSource fd = new FileDataSource(file);
    fd.setFileTypeMap(fileTypeMap);
    Article article = articleOtmService.ingest(fd, force);

    if (log.isInfoEnabled()) {
      log.info("Ingested: " + file + ":" + article.getId());
    }

    return article;
  }

  /**
   * @return List of filenames of files in uploadable directory on server
   */
  public List<String> getUploadableFiles() {
    List<String> documents = new ArrayList<String>();
    File dir = new File(documentDirectory);
    if (dir.isDirectory()) {
      for (String name : dir.list()) {
        if (Zip.StreamZip.isArchive(fileTypeMap.getContentType(name)))
          documents.add(name);
      }
    }

    Collections.sort(documents);
    return documents;
  }

  /**
   * Move the file to the ingested directory and generate cross-ref.
   *
   * @param file the file to move
   * @param article the associated article
   *
   * @throws IOException on an error
   * @throws TransformerException on a cross ref generation error
   * @throws SAXException on a cross ref generation error
   */
  public void generateIngestedData(File file, Article article)
    throws IOException, TransformerException, SAXException {
    FileUtils.moveFileToDirectory(file, new File(ingestedDocumentDirectory), true);
    if (log.isInfoEnabled()) {
      log.info("Relocated: " + file + ":" + article.getId());
    }

    generateCrossRefInfoDoc(article);
    if (log.isInfoEnabled()) {
      log.info("Generated Xref: " + article.getId() + " ingested from '" + file + "'");
    }
  }

  /**
   * @return A list of URIs of ingested documents in ST_DISABLED
   * @throws ApplicationException on an error
   */
  public Collection<String> getPublishableFiles() throws ApplicationException {
    try {
      List<String> articles =
          fetchArticleService.getArticleIds(null, null, new int[] { Article.STATE_DISABLED });
      Collections.sort(articles);
      return articles;
    } catch (Exception e) {
      throw new ApplicationException(e);
    }
  }

  private void generateCrossRefInfoDoc(Article article)
      throws IOException, TransformerException, SAXException {
    Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(xslTemplate));
    t.setParameter("plosDoiUrl", plosDoiUrl);
    t.setParameter("plosEmail", plosEmail);

    InputSource artXml = new InputSource(
        new ByteArrayInputStream(article.getRepresentation("XML").getBody()));
    artXml.setSystemId(article.getId().toString());

    File target_xml =
        new File(ingestedDocumentDirectory, uriToFilename(article.getId().toString()) + ".xml");

    t.transform(new CachedSource(artXml), new StreamResult(target_xml));
  }

  /**
   * @param uri
   * @return a string usable as a distinct filename - ':', '/' and '.' -> '_'
   */
  private String uriToFilename(String uri) {
    return uri.replace(':', '_').replace('/', '_').replace('.', '_');
  }

  /**
   * @param uris  uris to be published. Send CrossRef xml file to CrossRef - if it is _received_ ok
   *              then set article stat to active
   * @param vjMap a map giving the set of virtual-journals each article is to be published in
   * @return a list of messages describing what was successful and what failed
   */
  @Transactional(rollbackFor = { Throwable.class })
  public List<String> publish(String[] uris, Map<String, Set<String>> vjMap) {
    final List<String> msgs             = new ArrayList<String>();

    // publish articles
    for (String article : uris) {
      try {
        // send to cross-ref
        if (sendToXref) {
          File xref = new File(ingestedDocumentDirectory, uriToFilename(article) + ".xml");
          if (!xref.exists())
            throw new IOException("Cannot find CrossRef xml: " + uriToFilename(article) + ".xml");

          try {
            int stat = crossRefPosterService.post(xref);
            if (200 != stat)
              throw new Exception("CrossRef status returned " + stat);
          } catch (HttpException he) {
            log.error ("Could not connect to CrossRef", he);
            throw new Exception("Could not connect to CrossRef. " + he, he);
          } catch (IOException ioe) {
            log.error ("Could not connect to CrossRef", ioe);
            throw new Exception("Could not connect to CrossRef. " + ioe, ioe);
          }
        }

        // mark article as active
        articleOtmService.setState(article, Article.STATE_ACTIVE);
        msgs.add("Published: " + article);
        if (log.isInfoEnabled())
          log.info("Published article: '" + article + "'");

        // register with journals
        Set<String> vjs = vjMap.get(article);
        if (vjs != null) {
          for (String virtualJournal : vjs) {
            // get Journal by name
            final Journal journal = journalService.getJournal(virtualJournal);
            if (journal == null)
              throw new Exception("Error adding article '" + article +
                                  "' to non-existent journal '" + virtualJournal + "'");

            // add Article to Journal
            journal.getSimpleCollection().add(URI.create(article));

            final String message =
              "Article '" + article + "' was also added to journal '" + virtualJournal + "'";
            msgs.add(message);
            if (log.isInfoEnabled())
              log.info(message);
          }
        }
      } catch (Exception e) {
        log.error("Could not publish article: '" + article + "'", e);
        msgs.add("Error publishing: '" + article + "' - " + e.toString());
      }
    }


    return msgs;
  }

  /**
   * Sets the JournalService.
   *
   * @param journalService The JournalService to set.
   */
  @Required
  public void setJournalService(JournalService journalService) {
    this.journalService = journalService;
  }

  /**
   * @param plosDoiUrl The plosDxUrl to set.
   */
  @Required
  public void setPlosDoiUrl(String plosDoiUrl) {
    this.plosDoiUrl = plosDoiUrl;
  }

  /**
   * @param sendToXref The sendToXref to set.
   */
  @Required
  public void setSendToXref(boolean sendToXref) {
    this.sendToXref = sendToXref;
  }

  /**
   * @param plosEmail The plosEmail to set.
   */
  @Required
  public void setPlosEmail(String plosEmail) {
    this.plosEmail = plosEmail;
  }
}
