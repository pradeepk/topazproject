/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.util.impl;

import org.plos.util.TemplateMailer;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mail.MailPreparationException;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.MessagingException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.BodyPart;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.IOException;
import java.io.StringWriter;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.OutputStream;

/**
 * Freemarker template based emailer.
 */
public class FreemarkerTemplateMailer implements TemplateMailer {
  private JavaMailSender mailSender;
  private Configuration configuration;
  private String fromEmailAddress;
  private String subject;
  private String textTemplateFilename;
  private String htmlTemplateFilename;

  final String MIME_TYPE_TEXT_PLAIN = "text/plain";
  final String MIME_TYPE_TEXT_HTML = "text/html";
  final Map<String, String> mailContentTypes = new HashMap<String, String>();
  {
    mailContentTypes.put(MIME_TYPE_TEXT_PLAIN, "text");
    mailContentTypes.put(MIME_TYPE_TEXT_HTML, "HTML");
  }

  private Map<String, String> mapValues;
  private static final Log log = LogFactory.getLog(FreemarkerTemplateMailer.class);

  /**
   * Mail the email.
   * @param toEmailAddress toEmailAddress
   * @param context context to set the values from for the template
   */
  public void mail(final String toEmailAddress, final Map<String, Object> context) {
    mail(toEmailAddress, context, textTemplateFilename, htmlTemplateFilename);
  }

  /**
   * Mail the email formatted using the given templates
   * @param toEmailAddress toEmailAddress
   * @param context context to set the values from for the template
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  public void mail(final String toEmailAddress, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename) {
    final MimeMessagePreparator preparator = new MimeMessagePreparator() {
      public void prepare(final MimeMessage mimeMessage) throws MessagingException, IOException {
        mimeMessage.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));
        mimeMessage.setFrom(new InternetAddress(getFromEmailAddress()));
        mimeMessage.setSubject(getSubject());

        context.putAll(mapValues);

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
   * Mail to multiple email addresses.
   * @param toEmailAddresses toEmailAddresses
   * @param contexts contexts to set the values from for the template
   */
  public void massMail(final List<String> toEmailAddresses, final List<Map<String, Object>> contexts) {
    int i = 0;
    for (final String email: toEmailAddresses) {
      mail( email,
            contexts.get(i),
            getTextTemplateFilename(),
            getHtmlTemplateFilename());
      i++;
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

  private String getSubject() {
    return subject;
  }

  /**
   * Set the sibject for the email
   * @param subject subject
   */
  public void setSubject(final String subject) {
    this.subject = subject;
  }

  private String getTextTemplateFilename() {
    return textTemplateFilename;
  }

  /**
   * Set the textTemplateFilename
   * @param textTemplateFilename textTemplateFilename
   */
  public void setTextTemplateFilename(final String textTemplateFilename) {
    this.textTemplateFilename = textTemplateFilename;
  }

  private String getHtmlTemplateFilename() {
    return htmlTemplateFilename;
  }

  /**
   * Set the htmlTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  public void setHtmlTemplateFilename(final String htmlTemplateFilename) {
    this.htmlTemplateFilename = htmlTemplateFilename;
  }

  public Map<String, String> getMapValues() {
    return mapValues;
  }

  public void setMapValues(final Map<String, String> mapValues) {
    this.mapValues = mapValues;
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