/* $HeadURL::                                                                            $
 * $Id$
 */
package org.plos.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.List;
import java.util.Map;

public class MockTemplateMailer implements TemplateMailer {
  public void mail(final String toEmailAddress, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename) {
  }

  public void mail(final String toEmailAddress, final Map<String, Object> context) {
  }

  public void massMail(final List<String> emails, final List<Map<String, Object>> contexts) {
  }

  public void setFreeMarkerConfigurer(final FreeMarkerConfigurer freeMarkerConfigurer) {
  }

  public void setFromEmailAddress(final String fromEmailAddress) {
  }

  public void setMailSender(final JavaMailSender mailSender) {
  }

  public void setSubject(final String subject) {
  }

  public void setTextTemplateFilename(final String textTemplateFilename) {
  }

  public void setHtmlTemplateFilename(final String htmlTemplateFilename) {
  }

  public void setMapValues(final Map<String, String> mapValues) {
  }
}
