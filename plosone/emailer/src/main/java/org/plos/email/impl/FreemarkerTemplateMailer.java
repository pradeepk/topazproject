/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.email.impl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.email.TemplateMailer;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringBufferInputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Freemarker template based emailer.
 */
public class FreemarkerTemplateMailer implements TemplateMailer {
  private JavaMailSender mailSender;
  private Configuration configuration;
  private String fromEmailAddress;

  private final String MIME_TYPE_TEXT_PLAIN = "text/plain";
  private final String MIME_TYPE_TEXT_HTML = "text/html";
  private final Map<String, String> mailContentTypes = new HashMap<String, String>();
  {
    mailContentTypes.put(MIME_TYPE_TEXT_PLAIN, "text");
    mailContentTypes.put(MIME_TYPE_TEXT_HTML, "HTML");
  }

  private Map<String, String> verifyEmailMap;
  private Map<String, String> forgotPasswordVerificationEmailMap;
  private static final String TEXT = "text";
  private static final String HTML = "html";
  private static final String URL = "url";
  private static final String SUBJECT = "subject";
  
  private static final Log log = LogFactory.getLog(FreemarkerTemplateMailer.class);

  /**
   * Mail the email formatted using the given templates
   * @param toEmailAddress toEmailAddress
   * @param fromEmailAddress fromEmailAddress
   * @param subject subject of the email
   * @param context context to set the values from for the template
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  public void mail(final String toEmailAddress, final String fromEmailAddress, final String subject, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename) {
    final MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(final MimeMessage mimeMessage) throws MessagingException, IOException {
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
        mimeMessage.setFrom(new InternetAddress(fromEmailAddress));
        mimeMessage.setSubject(subject);

        // Create a "text" Multipart message
        final Multipart mp = createPartForMultipart(textTemplateFilename, context, "alternative", MIME_TYPE_TEXT_PLAIN);

        // Create a "HTML" Multipart message
        final Multipart htmlContent = createPartForMultipart(htmlTemplateFilename, context, "related", MIME_TYPE_TEXT_HTML);

        final BodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(htmlContent);
        mp.addBodyPart(htmlPart);

        mimeMessage.setContent(mp);
      }
    };
    mailSender.send(preparator);
    log.debug("Mail sent to:" + toEmailAddress);
  }

  private Multipart createPartForMultipart(final String templateFilename, final Map<String, Object> context, final String multipartType, final String mimeType) throws IOException, MessagingException {
    final Multipart multipart = new MimeMultipart(multipartType);
    multipart.addBodyPart(createBodyPart(mimeType, templateFilename, context));
    return multipart;
  }

  private BodyPart createBodyPart(final String mimeType, final String htmlTemplateFilename, final Map<String, Object> context) throws IOException, MessagingException {
    final BodyPart htmlPage = new MimeBodyPart();
    final Template htmlTemplate = configuration.getTemplate(htmlTemplateFilename);
    final StringWriter htmlWriter = new StringWriter();
    try {
      htmlTemplate.process(context, htmlWriter);
    } catch (TemplateException e) {
      throw new MailPreparationException("Can't generate " + mailContentTypes.get(mimeType) + " subscription mail", e);
    }

    htmlPage.setDataHandler(new BodyPartDataHandler(htmlWriter, mimeType));
    return htmlPage;
  }

  /**
   * @see org.plos.email.TemplateMailer#massMail(java.util.Map, java.lang.String, java.lang.String, java.lang.String)
   */
  public void massMail(final Map<String, Map<String, Object>> emailAddressContextMap, final String subject, final String textTemplateFilename, final String htmlTemplateFilename) {
    for (final Map.Entry<String, Map<String, Object>> entry : emailAddressContextMap.entrySet()) {
      mail( entry.getKey(),
            getFromEmailAddress(),
            subject,
            entry.getValue(),
            textTemplateFilename,
            htmlTemplateFilename);
    }
  }

  /**
   * Set the free marker configurer
   * @param freeMarkerConfigurer freeMarkerConfigurer
   */
  public void setFreeMarkerConfigurer(final FreeMarkerConfigurer freeMarkerConfigurer) {
    this.configuration = freeMarkerConfigurer.getConfiguration();
  }

  private String getFromEmailAddress() {
    return fromEmailAddress;
  }

  /**
   * Set the from email address
   * @param fromEmailAddress fromEmailAddress
   */
  public void setFromEmailAddress(final String fromEmailAddress) {
    this.fromEmailAddress = fromEmailAddress;
  }

  /**
   * Set the mail sender
   * @param mailSender mailSender
   */
  public void setMailSender(final JavaMailSender mailSender) {
    this.mailSender = mailSender;
  }

  /**
   * @param toEmailAddress toEmailAddress
   * @param mapValues contains the url for verification and html + text template names
   */
  public void sendEmail(final String toEmailAddress, final Map<String, Object> mapValues) {
    mail(toEmailAddress, getFromEmailAddress(), (String)mapValues.get(SUBJECT), mapValues, (String)mapValues.get(TEXT), (String)mapValues.get(HTML));
  }

  /**
   * @param toEmailAddress toEmailAddress
   * @param fromEmailAddress fromEmailAddress
   * @param mapValues contains the url for verification and html + text template names
   */
  public void sendEmail(final String toEmailAddress, final String fromEmailAddress, final Map<String, String> mapValues) {
    final Map<String, Object> context = new HashMap<String, Object>();

    final Set<Map.Entry<String,String>> mapIter = mapValues.entrySet();
    for (final Map.Entry<String, String> entry : mapIter) {
      context.put(entry.getKey(), entry.getValue());
    }
    
    mail(toEmailAddress, fromEmailAddress, mapValues.get(SUBJECT), context, mapValues.get(TEXT), mapValues.get(HTML));
  }

}

class BodyPartDataHandler extends DataHandler {
  public BodyPartDataHandler(final StringWriter writer, final String contentType) {
    super(new DataSource() {
      public InputStream getInputStream() throws IOException {
        return new StringBufferInputStream(writer.toString());
      }

      public OutputStream getOutputStream() throws IOException {
        throw new IOException("Read-only data");
      }

      public String getContentType() {
        return contentType;
      }

      public String getName() {
        return "main";
      }
    });
  }
}