package org.plos.admin.service;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.*;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ImageResizeService {
	
	private static final Log log = LogFactory.getLog(ImageResizeService.class);	
	private BufferedImage image;
	ImageWriter writer;
	
	public ImageResizeService() throws IOException {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
		if (writers.hasNext()) {
			
		} else {
			throw new IOException("No image writers for PNG files");
		}
		writer = writers.next();
	}
	
	private BufferedImage createResizedCopy(Image originalImage, 
            int scaledWidth, int scaledHeight, 
            boolean preserveAlpha)
	{
		int imageType = preserveAlpha ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB;
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, imageType);
		Graphics2D g = scaledBI.createGraphics();
		
		if (preserveAlpha) {
			g.setComposite(AlphaComposite.Src);
		}
		g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
		g.dispose();
		return scaledBI;
	}
	
	private BufferedImage readImage(String url) throws HttpException, IOException {
		BufferedImage bi = null;		
		HttpClient httpclient = new HttpClient();
		GetMethod getmethod = new GetMethod(url);
		if (200 == httpclient.executeMethod(getmethod)) {
			InputStream is = getmethod.getResponseBodyAsStream();
			String mimetype = getmethod.getResponseHeader("content-type").getValue().trim();
			bi = readImage(is, mimetype);
		} else {
			log.error("Cannot read image from: " + url);
		}
		return bi;
	}

	private BufferedImage readImage(InputStream is, String mimetype) throws IOException {
		Iterator<ImageReader> readers = ImageIO.getImageReadersByMIMEType(mimetype);
		ImageReader reader;
		ImageInputStream iis = new FileCacheImageInputStream(is, new File("C:\\etc\\topaz\\"));
		BufferedImage bi = null;
		
		if (! readers.hasNext()) {
      log.error("No image readers for type: " + mimetype);
			return null;
		}
    log.debug("found image reader for type: " + mimetype);
		reader = readers.next();
		reader.setInput(iis);
		bi = reader.read(0);
		is.close();
		iis.close();
    log.debug("BufferdImage.height = " + bi.getHeight() + " image.width = " + bi.getWidth());
		return bi;
	}
	
	public void captureImage(String url) throws HttpException, IOException {
		image = readImage(url);		
	}
	
	private byte[] scaleImage(float fixWidth, float fixHeight) throws IOException {
		int width = image.getWidth();
		int height = image.getHeight();		
		float scale;
		
		if (fixWidth > 0) {
			scale = fixWidth / width;
		} else if (fixHeight > 0) {
			scale = fixHeight / height;
		} else 
			return null;
		width = (int)(width * scale);
		height = (int)(height * scale);
		return imageToByteArray(createResizedCopy(image, width, height, true));
	}
	
	private byte[] imageToByteArray(BufferedImage image) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bos);
    return bos.toByteArray();
    /*log.debug("imageToByteArray: width= " + image.getWidth() + " height: " + image.getHeight());
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//writer.setOutput(new FileCacheImageOutputStream(bos, new File("C:\\etc\\topaz\\")));
    writer.setOutput(new MemoryCacheImageOutputStream(bos));
		writer.write(image);
		//bos.flush();
    log.debug("imagetobytearray size: " + bos.toString());
    log.debug("imagetobytearray size: " + bos.size());
    bos.close();
		return bos.toByteArray();*/
	}
	
	public byte[] getSmallImage() throws FileNotFoundException, IOException {
		return scaleImage(70.0f, 0.0f);
	}
	
	public byte[] getMediumImage() throws FileNotFoundException, IOException {
		return scaleImage(600.0f, 0.0f);
	}
	
	public byte[] getLargeImage() throws FileNotFoundException, IOException {
		return imageToByteArray(image);
	}
		
}
