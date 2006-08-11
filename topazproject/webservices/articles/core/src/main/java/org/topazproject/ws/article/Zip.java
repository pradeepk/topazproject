/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.ws.article;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.activation.DataSource;

/** 
 * A simple abstraction of a zip archive, as needed by the ingester. 
 * 
 * @author Ronald Tschalär
 */
public interface Zip {
  /** 
   * Get a list of all the entries in the archive. 
   * 
   * @return an Enumeration of {@link java.util.zip.ZipEntry ZipEntry} that enumerates all the
   *         entries
   * @throws IOException if an error occurred getting the enumeration
   */
  public Enumeration getEntries() throws IOException;

  /** 
   * Get the given entry's contents as an InputStream.
   * 
   * @param name the full pathname of the entry to retrieve
   * @param size (output) must be an array of length &gt;= 1; on return the 0'th
   *             element will contain the size of the contents, or -1 if unknown.
   * @return the entry's contents, or null if no entry by the given name exists
   * @throws IOException if an error occurred getting the contents
   */
  public InputStream getStream(String name, long[] size) throws IOException;

  /**
   * An implementation of {@link Zip Zip} where the zip archive is stored in a file.
   */
  public static class FileZip implements Zip {
    private final ZipFile zf;

    /** 
     * Create a new instance. 
     * 
     * @param zipFile the file-name of the zip archive
     * @throws IOException if an error occurred accessing the file
     */
    public FileZip(String zipFile) throws IOException {
      zf = new ZipFile(zipFile);
    }

    public Enumeration getEntries() {
      return zf.entries();
    }

    public InputStream getStream(String name, long[] size) throws IOException {
      ZipEntry ze = zf.getEntry(name);
      if (ze != null) {
        size[0] = ze.getSize();
        return zf.getInputStream(ze);
      }

      size[0] = -1;
      return null;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive is available from a stream.
   */
  public static abstract class StreamZip implements Zip {
    protected abstract InputStream getStream() throws IOException;

    public Enumeration getEntries() throws IOException {
      final ZipInputStream zis = new ZipInputStream(getStream());

      return new Enumeration() {
        private ZipEntry ze;

        {
          nextElement();
        }

        public boolean hasMoreElements() {
          return (ze != null);
        }

        public Object nextElement() {
          try {
            ZipEntry cur = ze;

            ze = zis.getNextEntry();
            if (ze == null)
              zis.close();

            return cur;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      };
    }

    public InputStream getStream(String name, long[] size) throws IOException {
      final ZipInputStream zis = new ZipInputStream(getStream());

      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        if (ze.getName().equals(name)) {
          size[0] = ze.getSize();
          return zis;
        }
      }

      zis.close();
      size[0] = -1;
      return null;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive is stored in memory.
   */
  public static class MemoryZip extends StreamZip {
    private final ByteArrayInputStream zs;

    /** 
     * Create a new instance. 
     * 
     * @param zipBytes the zip archive as an array of bytes
     */
    public MemoryZip(byte[] zipBytes) {
      zs = new ByteArrayInputStream(zipBytes);
    }

    protected InputStream getStream() {
      zs.reset();
      return zs;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive available as a DataSource.
   * NOTE: this assumes that either A) the data-source's input-stream supports reset(), or
   * that B) each DataSource.getInputStream() returns a new stream that starts at the beginning
   * (like Axis's ManagedMemoryDataSource does).
   */
  public static class DataSourceZip extends StreamZip {
    private final DataSource zs;

    /** 
     * Create a new instance. 
     * 
     * @param zipSource the zip archive as a datasource
     */
    public DataSourceZip(DataSource zipSource) {
      zs = zipSource;
    }

    protected InputStream getStream() throws IOException {
      InputStream is = zs.getInputStream();
      if (is.markSupported())
        is.reset();
      return is;
    }
  }
}
