package org.plos.admin.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ImageResizeService {
	
	private static final Log log = LogFactory.getLog(ImageResizeService.class);	
	private BufferedImage image;
	
	public ImageResizeService() {
		ImageIO.setUseCache(true);
		ImageIO.setCacheDirectory(null);
	}
	
	private BufferedImage createResizedCopy(BufferedImage originalImage, 
            int scaledWidth, int scaledHeight)
	{
		boolean noscale = false;
		if (0 == scaledWidth && 0 == scaledHeight) {
			scaledWidth = originalImage.getWidth(null);
			scaledHeight = originalImage.getHeight(null);
			noscale = true;
		}
		BufferedImage scaledBI = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = scaledBI.createGraphics();
		if (noscale) {
			g.drawImage(originalImage, 0, 0, null); 
		} else
			g.drawImage(originalImage, 0, 0, scaledWidth, scaledHeight, null); 
		g.dispose();
		return scaledBI;
	}
	
	private BufferedImage readImage(String url) throws HttpException, IOException {
		BufferedImage bi = null;		
		HttpClient httpclient = new HttpClient();
		GetMethod getmethod = new GetMethod(url);
		if (200 == httpclient.executeMethod(getmethod)) {
			bi = ImageIO.read(getmethod.getResponseBodyAsStream());
		} else {
			log.error("Cannot read image from: " + url);
		}
		return bi;
	}

	public void captureImage(String url) throws HttpException, IOException {
		image = readImage(url);		
		// log.info("Captured image width = " + image.getWidth() + " height= " + image.getHeight());
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
			scale = 0;
		width = (int)(width * scale);
		height = (int)(height * scale);
		return imageToByteArray(createResizedCopy(image, width, height));
	}
	
	private byte[] imageToByteArray(BufferedImage image) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
		byte[] array;
	    ImageIO.write(image, "png", bos);
	    bos.flush();
		array = bos.toByteArray();
		bos.close();
		return array;
	}
	
	public byte[] getSmallImage() throws FileNotFoundException, IOException {
		return scaleImage(70.0f, 0.0f);
	}
	
	public byte[] getMediumImage() throws FileNotFoundException, IOException {
		return scaleImage(600.0f, 0.0f);
	}
	
	public byte[] getLargeImage() throws FileNotFoundException, IOException {
		return scaleImage(0.0f, 0.0f);
	}
	
	public BufferedImage getImage() {
		return image;
	}
	
	public void setImage(BufferedImage bi) {
		image = bi;
	}
}
