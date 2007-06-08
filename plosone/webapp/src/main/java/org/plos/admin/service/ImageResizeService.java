/* $$HeadURL::                                                                            $$
 * $$Id$$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */

package org.plos.admin.service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.apache.commons.configuration.Configuration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.plos.configuration.ConfigurationStore;

public class ImageResizeService {
  private static final Log log = LogFactory.getLog(ImageResizeService.class);

  private int height;

  private int width;

  private String directory;

  private String inputImageFileName;

  private String outputImageFileName;

  private File location;

  private File inputImageFile;

  private File outputImageFile;

  public ImageResizeService() throws ImageResizeException {
    final Configuration configuration = ConfigurationStore.getInstance().getConfiguration();

    if (configuration.isEmpty()) {
      throw new ImageResizeException("ERROR: configuration has no property values");
    }

    final String directory = configuration.getString("topaz.utilities.image-magick.temp-directory");

    if (directory == null) {
      throw new ImageResizeException("ERROR: configuration failed to associate a value with property topaz.utilities.image-magick.temp-directory");
    }

    setDirectory(directory);

    final Integer disambiguation = new Integer(hashCode());
    setInputImageFileName("_"+disambiguation+"current");
    setOutputImageFileName("_"+disambiguation+"final.png");
  }

  public void setDirectory(final String directory) throws ImageResizeException {
    this.directory = directory;

    // side effects: what we would really like is a File object
    //               which represents the directory.
    location = new File(this.directory);

    if (!location.isDirectory()) {
      throw new ImageResizeException("ERROR: "+this.getClass().getCanonicalName()+" requires a valid directory.");
    }

    if (!location.canRead()) {
      throw new ImageResizeException("ERROR: "+this.getClass().getCanonicalName()+" requires a directory from which the process may read.");
    }

    if (!location.canWrite()) {
      throw new ImageResizeException("ERROR: "+this.getClass().getCanonicalName()+" requires a directory to which the process may write.");
    }
  }

  public void setInputImageFileName(final String inputImageFileName) {
    this.inputImageFileName = inputImageFileName;
  }

  public String getInputImageFileName() {
    return inputImageFileName;
  }

  public void setOutputImageFileName(final String outputImageFileName) {
    this.outputImageFileName = outputImageFileName;
  }

  public String getOutputImageFileName() {
    return outputImageFileName;
  }

  private void setHeight(final int height) {
    this.height = height;
  }

  private int getHeight() {
    return height;
  }

  private void setWidth(final int width) {
    this.width = width;
  }

  private int getWidth() {
    return width;
  }

  private void preOperation(final byte[] image) throws ImageResizeException {
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    inputImageFile = new File(location,getInputImageFileName());
    outputImageFile = new File(location,getOutputImageFileName());

    try {
      if (!inputImageFile.createNewFile()) {
        try {
          inputImageFile.delete();
        } catch (SecurityException e) {}

        throw new ImageResizeException("ERROR: unable to create temporary file: "+inputImageFile.getCanonicalPath());
      }

      if (outputImageFile.exists()) {
        throw new ImageResizeException("ERROR: temporary file: "+outputImageFile.getCanonicalPath()+" already exists");
      }

      final ByteArrayInputStream in = new ByteArrayInputStream(image);

      try {
        final FileOutputStream out = new FileOutputStream(inputImageFile);

        try {
          imageRetrievalService.transferImage(in,out);
        } catch (ImageRetrievalServiceException e) {
          throw new ImageResizeException(e);
        } finally {
          out.close();
        }

        // yes, this is inefficient. we are writing from memory to a file and then
        // reading it back into memory. the only way to inspect the image in memory
        // with jai is to use internal com.sun classes and i cannot do this.
        final RenderedOp srcImage = JAI.create("fileload",inputImageFile.getCanonicalPath());
        setWidth(srcImage.getWidth());
        setHeight(srcImage.getHeight());
      } catch (SecurityException e) {
          throw new ImageResizeException(e);
      } finally {
        in.close();
      }
    } catch (SecurityException e) {
      throw new ImageResizeException(e);
    } catch (IOException e) {
      throw new ImageResizeException(e);
    }
  }

  private void postOperation() throws ImageResizeException {
    String path;

    try {
      path = inputImageFile.getCanonicalPath();
    } catch (IOException e) {
      path = "bad file";
    }

    try {
      if (inputImageFile.exists() && !inputImageFile.delete()) {
        throw new ImageResizeException("ERROR: unable to delete temporary file: "+path);
      }
    } catch (SecurityException e) {
      throw new ImageResizeException(e);
    } finally {
      try {
        if (outputImageFile.exists() && !outputImageFile.delete()) {
          throw new ImageResizeException("ERROR: unable to delete temporary file: "+path);
        }
      } catch (SecurityException e) {
        throw new ImageResizeException(e);
      }
    }
  }

  // NOTE: resizing operations are logically different from format conversion.
  //       nonetheless, both operations are performed simultaneously by ImageMagick.

  ///////////////////////////
  // 2 resizing operations //
  ///////////////////////////

  // resize operation #1
  private void createScaledImage() throws ImageResizeException {
    createScaledImage(getWidth(),getHeight());
  }

  // resize operation #2
  private void createScaledImage(final int newWidth,final int newHeight) throws ImageResizeException {
    final ImageMagicExecUtil util = new ImageMagicExecUtil();
    final boolean status = util.convert(this.inputImageFile,outputImageFile,newWidth,newHeight);

    if (!status) {
      log.fatal("ERROR: operation convert failed");
      throw new ImageResizeException(new Exception("operation convert failed"));
    }
  }

  ////////////////////////////
  // 1 conversion operation //
  ////////////////////////////

  // implicit conversion operation
  private synchronized byte[] getPNGByteArray() throws ImageResizeException {
    assert(outputImageFile != null && outputImageFile.exists() && outputImageFile.length() > 0);

    final byte[] result;
    final ImageRetrievalService imageRetrievalService = new ImageRetrievalService();

    try {
      final InputStream in = new FileInputStream(this.outputImageFile);
      final long fileSize = this.outputImageFile.length();

      try {
        final ByteArrayOutputStream out = new ByteArrayOutputStream((int) fileSize);

        try {
          imageRetrievalService.transferImage(in,out);
          result = out.toByteArray();

          if (log.isDebugEnabled()) {
            log.debug("file size: "+result.length);
          }
        } catch (ImageRetrievalServiceException e) {
          throw new ImageResizeException(e);
        } finally {
          out.close();
        }
      } finally {
        in.close();
      }
    } catch (FileNotFoundException e) {
      throw new ImageResizeException(e);
    } catch (IOException e) {
      throw new ImageResizeException(e);
    }

    return result;
  }

  //////////////////////////
  // 4 scaling operations //
  //////////////////////////

  // scaling operation #1
  private byte[] performNoScaling () throws ImageResizeException {
    createScaledImage();
    return getPNGByteArray();
  }

  // scaling operation #2
  private byte[] scaleFixHeight(final float fixHeight) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (getHeight() > fixHeight) {
      scale = fixHeight / getHeight();
      newHeight = (int) fixHeight;
      newWidth = (int) (getWidth() * scale);
    } else {
      scale = 1;
      newHeight = getHeight();
      newWidth = getWidth();
    }

    // perform precisely one of the resize operations.
    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }

  // scaling operation #3
  private byte[] scaleFixWidth(final float fixWidth) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if (getWidth() > fixWidth) {
      scale = fixWidth / getWidth();
      newWidth = (int) fixWidth;
      newHeight = (int) (getHeight() * scale);
    } else {
      scale = 1;
      newWidth = getWidth();
      newHeight = getHeight();
    }

    // perform precisely one of the resize operations.
    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }

  // scaling operation #4
  private byte[] scaleLargestDim(final float oneSide) throws ImageResizeException {
    final float scale;
    final int newHeight;
    final int newWidth;

    if ((getHeight() > getWidth()) && (getHeight() > oneSide)) {
      scale = oneSide / getHeight();
      newHeight = (int) oneSide;
      newWidth = (int) (getWidth() * scale);
    } else if (getWidth() > oneSide) {
      scale = oneSide / getWidth();
      newWidth = (int) oneSide;
      newHeight = (int) (getHeight() * scale);
    } else {
      scale = 1;
      newWidth = getWidth();
      newHeight = getHeight();
    }

    // perform precisely one of the resize operations.
    if (scale == 1) {
      createScaledImage();
    } else {
      createScaledImage(newWidth,newHeight);
    }

    return getPNGByteArray();
  }
  
  /**
   * Scale the image to 70 pixels in width into PNG
   * 
   * @return byte array of the new small PNG image
   * @throws ImageResizeException
   */

  public byte[] getSmallScaledImage(final byte[] initialImage) throws ImageResizeException {
    try {
      preOperation(initialImage);
      return scaleFixWidth(70.0f);
    } finally {
      postOperation();
    }
  }

  /**
   * Scale the image to at most 600 pixels in either direction into a PNG
   * 
   * @return byte array of the new medium size PNG image
   * @throws ImageResizeException
   */
  public byte[] getMediumScaledImage(final byte[] initialImage) throws ImageResizeException {
    try {
      preOperation(initialImage);
      return scaleLargestDim(600.0f);
    } finally {
      postOperation();
    }
  }

  /**
   * Don't scale the image, just return a PNG version of it
   * 
   * @return byte array of the new PNG version of the image
   * @throws ImageResizeException
   */
  public byte[] getLargeScaledImage(final byte[] initialImage) throws ImageResizeException {
    try {
      preOperation(initialImage);
      return performNoScaling();
    } finally {
      postOperation();
    }
  }
}
