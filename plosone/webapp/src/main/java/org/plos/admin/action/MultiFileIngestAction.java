package org.plos.admin.action;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.activation.DataHandler;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.action.BaseActionSupport;
import org.plos.admin.service.CrossRefPosterService;
import org.plos.article.service.ArticleWebService;

public class MultiFileIngestAction extends BaseActionSupport {
	
	private static final Log log = LogFactory.getLog(MultiFileIngestAction.class);
	
	/**
	 * So - this is all totally bogus for now since opensymphony does not seem 
	 * to support multiple file uploads in a meaningful way. What should really
	 * happen is FileUploadInterceptor gets subclassed to pass in arrays of
	 * files & stuff. Will do as time permits.
	 */
	
	private File file_0, file_1, file_2, file_3, file_4;
	private String file_0ContentType, file_1ContentType, file_2ContentType, file_3ContentType, file_4ContentType;
	private String file_0FileName, file_1FileName, file_2FileName, file_3FileName, file_4FileName;
	
	ArticleWebService articleWebService;

	private CrossRefPosterService crossRefPosterService;

	public MultiFileIngestAction() {
		file_0 = null;
		file_1 = null;
		file_2 = null;
		file_3 = null;
		file_4 = null;
	}
	
	/**
	 * Note we don't need to clean up the temporary files - the framework will
	 * do that. For each uploaded file ingest and Xref.
	 */
	public String execute() throws Exception {
		log.info("Starting MultiFile ingest"); 
		try {
			if (null != file_0) {
				process(file_0, file_0ContentType, file_0FileName);
			}
			if (null != file_1) {
				process(file_1, file_1ContentType, file_1FileName);
			}
			if (null != file_2) {
				process(file_2, file_2ContentType, file_2FileName);
			}
			if (null != file_3) {
				process(file_3, file_3ContentType, file_3FileName);
			}
			if (null != file_4) {
				process(file_4, file_4ContentType, file_4FileName);
			}
		} catch (Exception e) {
			addActionMessage("There was an error: ");
			addActionMessage(e.toString());
			return ERROR;
		}
		log.info("MultiFile ingest succeeded ");
		return SUCCESS;
	}	  

	/** Set the article web service
	 * @param articleWebService articleWebService
	 */
	public void setArticleWebService(final ArticleWebService articleWebService) {
		this.articleWebService = articleWebService;
	}

	public void setCrossRefPosterService(final CrossRefPosterService crossRefPosterService) {
		this.crossRefPosterService = crossRefPosterService;
	}
	
	private void process(File file, String contenttype, String filename) throws Exception  {
		int xrefresult;
		StringBuffer msg = new StringBuffer();
		try {
			log.info(new StringBuffer("Ingesting ").append(filename).append(" type = ").append(contenttype).toString());
			articleWebService.ingest(new DataHandler(file.toURL()));
			msg.append(filename).append(" -- ingested");
			xrefresult = crossRefDocument(file);
			if (200 == xrefresult)
				msg.append(" -- succesfully submitted to CrossRef");
			else
				msg.append(" -- CrossRef submittal failed: ").append(xrefresult);
			log.info(new StringBuffer("Crossref returned: ").append(xrefresult).append(" for file: ").append(filename).toString());
			addActionMessage(msg.toString());
		} catch (Exception e) {
			throw(e);
		}
	}

	private int crossRefDocument(File zipfile) throws ZipException, IOException, TransformerException {
		ZipFile zip = new ZipFile(zipfile);
		Enumeration entries = zip.entries();
		int result = 0;
		
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (entry.getName().toLowerCase().endsWith(".xml")) {
				File source_xml = File.createTempFile("xref-doi-src", ".xml");
				File target_xml;		
				
				BufferedInputStream fis = new BufferedInputStream(zip.getInputStream(entry));
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(source_xml));
				byte[] buf = new byte[(int)entry.getSize()];
				int size;
				while (-1 != (size = fis.read(buf))) {
						bos.write(buf, 0, size);
				}
				bos.flush();
				bos.close();
				try {
					// First transform xml file from archive
					target_xml = crossRefXML(source_xml);
					// Fix up target
					// .. to be done
					//
					// then submit it
					log.info("Target xml is in: " + target_xml.getAbsolutePath());
					result = crossRefPosterService.post(entry.getName(), target_xml);
				} finally {
					source_xml.delete();
					// target_xml.delete();
				}
				break;
			}
		}
		return result;
	}
	
	private File crossRefXML(File src) throws IOException, TransformerFactoryConfigurationError, TransformerException {
		File crxml = File.createTempFile("xref-doi-target", ".xml");		
		
		Transformer t = TransformerFactory.newInstance().newTransformer(new StreamSource(crossRefPosterService.getXslFile()));
		StreamSource s_source = new StreamSource(src);
		StreamResult s_result = new StreamResult(crxml);
		t.transform(s_source, s_result);
		return crxml;	
	}

	/**
	 * Yuck
	 */
	public void setFile_0(File file_0) {
		this.file_0 = file_0;
	}

	public void setFile_1(File file_1) {
		this.file_1 = file_1;
	}

	public void setFile_2(File file_2) {
		this.file_2 = file_2;
	}

	public void setFile_3(File file_3) {
		this.file_3 = file_3;
	}

	public void setFile_4(File file_4) {
		this.file_4 = file_4;
	}
	public void setFile_0ContentType(String file_1ContentType) {
		this.file_0ContentType = file_1ContentType;
	}
	public void setFile_0FileName(String file_0FileName) {
		this.file_0FileName = file_0FileName;
	}
	public void setFile_1ContentType(String file_1ContentType) {
		this.file_1ContentType = file_1ContentType;
	}
	public void setFile_1FileName(String file_1FileName) {
		this.file_1FileName = file_1FileName;
	}
	public void setFile_2ContentType(String file_2ContentType) {
		this.file_2ContentType = file_2ContentType;
	}
	public void setFile_2FileName(String file_2FileName) {
		this.file_2FileName = file_2FileName;
	}
	public void setFile_3ContentType(String file_3ContentType) {
		this.file_3ContentType = file_3ContentType;
	}
	public void setFile_3FileName(String file_3FileName) {
		this.file_3FileName = file_3FileName;
	}
	public void setFile_4ContentType(String file_4ContentType) {
		this.file_4ContentType = file_4ContentType;
	}
	public void setFile_4FileName(String file_4FileName) {
		this.file_4FileName = file_4FileName;
	}


}
