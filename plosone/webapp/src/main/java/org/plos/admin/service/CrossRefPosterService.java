package org.plos.admin.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.plos.util.FileUtils;

public class CrossRefPosterService {
	private String doiXrefUrl;
	private File xslTemplate;
	
	public void init() {
	}
	
	public void setDoiXrefUrl(final String doiXrefUrl) {
		this.doiXrefUrl = doiXrefUrl;
	}

	public int post(String filename, File file) throws HttpException, IOException {
		PostMethod poster = new PostMethod(doiXrefUrl);
		HttpClient client = new HttpClient();

		Part[] parts = {new FilePart("fname", filename, file)};
		
		poster.setRequestEntity(
                new MultipartRequestEntity(parts, poster.getParams())
                );
		client.getHttpConnectionManager().getParams().setConnectionTimeout(25000);
		return client.executeMethod(poster);
	}

	public void setXslTemplate(final String xslTemplate) throws URISyntaxException {
		    File file = getAsFile(xslTemplate);
		    if (!file.exists()) {
		      file = new File(xslTemplate);
		    }
		    this.xslTemplate = file;
	}
	
	public File getXslFile() {
		return xslTemplate;
	}
	
	  /**
	   * @param filenameOrURL filenameOrURL
	   * @throws URISyntaxException URISyntaxException
	   * @return the local or remote file or url as a java.io.File
	   */
	  public File getAsFile(final String filenameOrURL) throws URISyntaxException {
	    final URL resource = getClass().getResource(filenameOrURL);
	    if (null == resource) {
	      //access it as a local file resource
	      return new File(FileUtils.getFileName(filenameOrURL));
	    } else {
	      return new File(resource.toURI());
	    }
	  }
}
