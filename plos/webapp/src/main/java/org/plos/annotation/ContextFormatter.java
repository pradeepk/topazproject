/**
 * 
 */
package org.plos.annotation;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.plos.ApplicationException;
import org.plos.util.XPointerUtils;

/**
 * ContextFormatter - Responsible for String-izing a {@link Context}.
 * @author jkirton
 *
 */
public abstract class ContextFormatter {
  private static final Log log = LogFactory.getLog(ContextFormatter.class);
  
  /**
   * Returning an xpointer of the following forms:
   * <p>
   * 1)
   * string-range(/doc/chapter/title,'')[5]/range-to(string-range(/doc/chapter/para/em,'')[3])
   * <p>
   * 2) string-range(/article[1]/body[1]/sec[1]/p[2],"",194,344)
   * 
   * @return String-ized context in xpointer format
   * @throws org.plos.ApplicationException ApplicationException
   */
  public static String asXPointer(Context c) throws ApplicationException {
    final String startPath = c.getStartPath();
    final int startOffset = c.getStartOffset();
    final String endPath = c.getEndPath();
    final int endOffset = c.getEndOffset();
    
    if (StringUtils.isBlank(startPath)) {
      return null;
    }
    try {
      String context;
      if (startPath.equals(endPath)) {
        final int length = endOffset - startOffset;
        if (length < 0) {
          // addFieldError("endOffset", errorMessage);
          throw new ApplicationException("Invalid length: " + length + " of the annotated content");
        }
        context = XPointerUtils.createStringRangeFragment(startPath, "", startOffset, length);
      }
      else {
        context = XPointerUtils.createRangeToFragment(
            XPointerUtils.createStringRangeFragment(startPath, "", startOffset),
            XPointerUtils.createStringRangeFragment(endPath, "", endOffset));
      }
      if (log.isDebugEnabled()) log.debug("xpointer target context: " + context);
      return XPointerUtils.createXPointer(c.getTarget(), context, "UTF-8");
    }
    catch (final UnsupportedEncodingException e) {
      log.error(e);
      throw new ApplicationException(e);
    }
  }
}