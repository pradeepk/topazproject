/* $HeadURL::                                                                            $
* $Id$
*/
package org.plos.util;

import java.util.Map;
import java.util.List;

/**
 * A contract for all template based emailers.
 */
public interface TemplateMailer {

  /**
   * Send a mail with both a text and a HTML version.
   * @param toEmailAddress          the email address where to send the email
   * @param context        a {@link java.util.Map} of objects to expose to the template engine
   * @param textTemplateFilename textTemplateFilename
   * @param htmlTemplateFilename htmlTemplateFilename
   */
  void mail(final String toEmailAddress, final Map<String, Object> context, final String textTemplateFilename, final String htmlTemplateFilename);

  /**
   * Send a mail with both a text and a HTML version.
   * @param toEmailAddress          the email address where to send the email
   * @param context        a {@link java.util.Map} of objects to expose to the template engine
   */
  void mail(final String toEmailAddress, final Map<String, Object> context);

  /**
   * Send a mass-mailing with both a text and a HTML version.
   * @param emails         a {@link java.util.List} of email addresses where to send emails
   * @param contexts       a {@link java.util.List} of {@link java.util.Map}s of objects to expose to the template engine
   */
  void massMail(final List<String> emails, final List<Map<String, Object>> contexts);
}