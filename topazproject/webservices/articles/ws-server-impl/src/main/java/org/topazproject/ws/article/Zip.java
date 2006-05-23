
package org.topazproject.ws.article;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
   * @return the entry's contents, or null if no entry by the given name exists
   * @throws IOException if an error occurred getting the contents
   */
  public InputStream getStream(String name) throws IOException;

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

    public InputStream getStream(String name) throws IOException {
      ZipEntry ze = zf.getEntry(name);
      return (ze != null) ? zf.getInputStream(ze) : null;
    }
  }

  /**
   * An implementation of {@link Zip Zip} where the zip archive is stored in memory.
   */
  public static class MemoryZip implements Zip {
    private final ByteArrayInputStream zs;

    /** 
     * Create a new instance. 
     * 
     * @param zipBytes the zip archive as an array of bytes
     */
    public MemoryZip(byte[] zipBytes) {
      zs = new ByteArrayInputStream(zipBytes);
    }

    public Enumeration getEntries() {
      zs.reset();
      final ZipInputStream zis = new ZipInputStream(zs);

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
            return cur;
          } catch (IOException ioe) {
            throw new RuntimeException(ioe);
          }
        }
      };
    }

    public InputStream getStream(String name) throws IOException {
      zs.reset();
      final ZipInputStream zis = new ZipInputStream(zs);

      ZipEntry ze;
      while ((ze = zis.getNextEntry()) != null) {
        if (ze.getName().equals(name))
          return zis;
      }

      return null;
    }
  }
}
