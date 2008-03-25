package org.plos.article.util;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * ArticleZip - Convenience wrapper for a File that is an article zip file.
 * @author jkirton
 */
public final class ArticleZip {

  /**
   * Filename prefix indicating a processed article zip file.
   */
  public static final String PROCESSED_FILENAME_PREFIX = "proc.";

  /**
   * Is the given File a processed article zip file?
   * @param f The file test test
   * @return <code>true</code> if the given File is indeed a processed article
   *         zip file.
   */
  public static boolean isProcessedArticleZipFile(File f) {
    assert f != null;
    return f.getName().startsWith(ArticleZip.PROCESSED_FILENAME_PREFIX);
  }

  /**
   * article zip File instance
   */
  private final File f;
  /**
   * The article zip file name w/o the '.zip' extension
   */
  private final String baseName;
  /**
   * article zip ZipFile instance
   */
  private volatile ZipFile zf;

  public ArticleZip(File f) {
    super();
    assert f != null;
    this.f = f;

    // determine the base name
    String n = f.getName();
    assert n.endsWith(".zip");
    baseName = n.substring(0, n.length() - 4);
  }

  /**
   * @return the File of the article zip file
   */
  public File getFile() {
    return f;
  }

  /**
   * @return the baseName of the article zip file (No .zip extension)
   */
  public String getBaseName() {
    return baseName;
  }

  /**
   * @return the ZipFile of the article zip file creating it lazily.
   * @throws ZipException
   * @throws IOException
   */
  public ZipFile getZipFile() throws ZipException, IOException {
    if (zf == null) zf = new ZipFile(f);
    return zf;
  }

  /**
   * Closes the zip file if open.
   * @throws IOException
   */
  public void close() throws IOException {
    if (zf != null) {
      zf.close();
      zf = null;
    }
  }

  @Override
  public String toString() {
    return f.getAbsolutePath();
  }
}