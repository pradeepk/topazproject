/*
 * Copyright 2008 Topaz Project, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * This class is used to process the contents of a directory and everything under it.
 *
 * @author Paul Gearon
 */
public class DirProcessor {
  /** The function for processing files. */
  private FileProcessor fileProc;

  /** The function for processing subdirectories. */
  private FileProcessor dirProc;

  /** A filter for identifying the files to be processed. */
  private FileFilter fileFilter;

  /** An optional filename extension to be used when determining what will be processed. */
  private String ext;

  /**
   * Creates a processor fo handling files and subdirectories under a directory.
   *
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   *        May be <code>null</code>.
   * @param fileFilter A filter for selecting which files are to be processed.
   *        If <code>null</code> then all files are processed.
   */
  public DirProcessor(FileProcessor fileProc, FileProcessor dirProc, FileFilter fileFilter) {
    if (fileProc == null)
      throw new IllegalArgumentException("Function for processing files must be provided.");
    this.fileProc = fileProc;
    this.dirProc = dirProc;
    this.fileFilter = (fileFilter == null) ? new AllStandardFiles() : fileFilter;;
  }

  /**
   * Creates a processor fo handling files and subdirectories under a directory.
   *
   * @param fileProc The function for processing any files found.
   * @param dirProc The function for processing any subdirectories found.
   */
  public DirProcessor(FileProcessor fileProc, FileProcessor dirProc) {
    this(fileProc, dirProc, null);
  }

  /**
   * Creates a processor for handling files under a directory.
   *
   * @param fileProc The function for processing any files found.
   */
  public DirProcessor(FileProcessor fileProc) {
    this(fileProc, null, null);
  }

  /**
   * Creates a processor for handling files with a given extension under a directory.
   *
   * @param fileProc The function for processing any files found.
   * @param ext The file extension to use.
   */
  public DirProcessor(FileProcessor fileProc, String ext) {
    this(fileProc, null, null);
    setFilenameExt(ext);
  }

  /**
   * Gets the filename extension to be processed by this instance.
   *
   * @return The filename extension to be processed. The "." character is included.
   */
  public String getFilenameExt(String ext) {
    return ext;
  }

  /**
   * Sets the filename extension to be processed by this instance.
   *
   * @param ext The filename extension to be processed. The "." character is optional.
   */
  public void setFilenameExt(String ext) {
    this.ext = ext.startsWith(".") ? ext : "." + ext;
    fileFilter = new FileExtFilter(this.ext);
  }

  /**
   * Process a directory by name and everything under it.
   *
   * @param dirName The name of the directory to process.
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public int process(String dirName) throws IOException {
    return process(new File(dirName), true);
  }

  /**
   * Process a directory and everything under it.
   *
   * @param dir The directory to process.
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public int process(File dir) throws IOException {
    return process(dir, true);
  }

  /**
   * Process a directory by name. The contents of subdirectories are optionally processed as well.
   *
   * @param dirName The directory name to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into subdirectories.
   *                       Otherwise, processing is confined to just this directory.
   *
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public int process(String dirName, boolean processSubDirs) throws IOException {
    return process(new File(dirName), processSubDirs);
  }

  /**
   * Process a directory. The contents of subdirectories are optionally processed as well.
   *
   * @param dir The directory to process.
   * @param processSubDirs When <code>true</code> then processing will proceed into subdirectories.
   *                       Otherwise, processing is confined to just this directory.
   *
   * @return The number of items processed.
   *
   * @throws IOException If there was an error processing the file.
   */
  public int process(File dir, boolean processSubDirs) throws IOException {
    int total = 0;
    for (File file: dir.listFiles(fileFilter)) total += fileProc.fn(file);
    for (File d: dir.listFiles(DirFilter.INSTANCE)) {
      if (dirProc != null)
        total += dirProc.fn(d);
      if (processSubDirs)
        total += process(d, true);
    }
    return total;
  }

  /**
   * A FileFilter that passes all standard files.
   */
  static class AllStandardFiles implements FileFilter {
    public boolean accept(File file) {
      return file.isFile();
    }
  }

  /**
   * A FileFilter that identifies all directories.
   */
  static class DirFilter implements FileFilter {
    public static DirFilter INSTANCE = new DirFilter();
    private DirFilter() { }
    public boolean accept(File file) {
      return file.isDirectory();
    }
  }
}
