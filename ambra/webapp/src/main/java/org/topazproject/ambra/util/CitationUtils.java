/* $HeadURL::                                                                            $
 * $Id::$
 *
 * Copyright (c) 2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.topazproject.ambra.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.topazproject.ambra.annotation.service.WebAnnotation;
import org.topazproject.ambra.configuration.ConfigurationStore;
import org.topazproject.ambra.models.Citation;
import org.topazproject.ambra.models.UserProfile;

/**
 * CitationUtils - General citation related utility methods.
 *
 * @author jkirton
 */
public abstract class CitationUtils {
  private static final int MAX_AUTHORS_TO_DISPLAY = 5;

  /**
   * Appends to the given {@link StringBuilder} the article authors in a prescribed format.
   * @param ci Citation
   * @param sb StringBuilder to which the authors String is appended
   * @param correction Is this for an article correction citation?
   */
  private static void handleAuthors(Citation ci, StringBuilder sb, boolean correction) {
    // obtain a list of all author names
    List<UserProfile> authors = ci.getAuthors();
    if (authors != null) {
      int i = 0;
      for (UserProfile a : authors) {
        sb.append(a.getSurnames());
        sb.append(' ');

        String gns = a.getGivenNames();
        if (gns != null) {
          toShortFormat(sb, gns, correction);
        }

        if (a.getSuffix() != null) {
          sb.append(a.getSuffix());
          sb.append(' ');
        }

        if (i < authors.size() - 1)
          sb.append(", ");

        if (++i == MAX_AUTHORS_TO_DISPLAY) {
          break;
        }

      }

      if (authors.size() > MAX_AUTHORS_TO_DISPLAY) {
        sb.append(" et al.");
      }
      sb.append(' ');
    }
  }

  private static void toShortFormat(StringBuilder sb, String gns, boolean correction) {
    /* for formal corrections, we want the initial of the last given name followed by a period (.)
     * whereas for article citations, we want each the initial of each given name concatenated with
     * no periods
     */
    String[] givenNames = gns.split(" ");
    int gnc = 0;
    for (String gn :givenNames) {
      // TODO: similar code like in AuthorNameAbbreviationDirective, should be moved together
      if (gn.length() > 0 && ((correction && gnc++ == givenNames.length - 1) || !correction)) {
        // Handle dashes in name
        if (gn.matches(".*\\p{Pd}\\p{Lu}.*")) {
          String[] sarr = gn.split("\\p{Pd}");

          for (int j = 0; j < sarr.length; j++) {
            if (j>0)
              sb.append('-');
            sb.append(sarr[j].charAt(0));
          }
        } else {
          sb.append(gn.charAt(0));
        }
        if (correction)
          sb.append('.');
      }
    }
  }

  /**
   * Assembles a String representing an annotation citatation based on a prescribed format.
   * <p>
   * FORMAT:
   * <p>
   * {first five authors of the article}, et al. (<Year the annotation was created>) Correction:
   * {article title}. {journal abbreviated name} {annotation URL}
   *
   * @param ci The {@link Citation} pertaining to the article.
   * @param wa The {@link WebAnnotation}.
   * @return A newly created article annotation citation String. <br>
   *         Refer to: <a href="http://wiki.plos.org/pmwiki.php/Topaz/Corrections"
   *         >http://wiki.plos.org/pmwiki.php/Topaz/Corrections</a> for the format specification.
   */
  public static String generateArticleCorrectionCitationString(Citation ci, WebAnnotation wa) {
    assert ci != null;
    assert wa != null;

    StringBuilder sb = new StringBuilder();

    // authors
    handleAuthors(ci, sb, true);

    // comment post date
    sb.append(" (");
    sb.append(ci.getYear().toString());
    sb.append(") ");

    sb.append("Correction: ");

    // article title
    sb.append(ArticleFormattingDirective.format(ci.getTitle()));
    sb.append(". ");

    // journal title
    sb.append(ci.getJournal());
    sb.append(": ");

    // annotation URI
    sb.append(ConfigurationStore.getInstance().getConfiguration().getString("ambra.platform.doiUrlPrefix"));
    sb.append(StringUtils.replace(wa.getId(), ConfigurationStore.getInstance().getConfiguration()
                                                      .getString("ambra.aliases.doiPrefix"), ""));

    return sb.toString();
  }
}
