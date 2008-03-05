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
 * Simple encapsulation of properties that define an annotation context.
 * 
 * @author jkirton
 */
public class Context {
  private static final Log log = LogFactory.getLog(Context.class);

  private final String startPath;
  private final int startOffset;
  private final String endPath;
  private final int endOffset;
  private final String target;

  /**
   * Constructor
   * 
   * @param startPath
   * @param startOffset
   * @param endPath
   * @param endOffset
   * @param target
   */
  public Context(String startPath, int startOffset, String endPath, int endOffset, String target) {
    super();
    this.startPath = startPath;
    this.startOffset = startOffset;
    this.endPath = endPath;
    this.endOffset = endOffset;
    this.target = target;
  }

  /**
   * Returning an xpointer of the following forms:
   * <p>
   * 1)
   * string-range(/doc/chapter/title,'')[5]/range-to(string-range(/doc/chapter/para/em,'')[3])
   * <p>
   * 2) string-range(/article[1]/body[1]/sec[1]/p[2],"",194,344)
   * 
   * @return the context for the annotation
   * @throws org.plos.ApplicationException ApplicationException
   */
  public String getXPointer() throws ApplicationException {
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
      return XPointerUtils.createXPointer(target, context, "UTF-8");
    }
    catch (final UnsupportedEncodingException e) {
      log.error(e);
      throw new ApplicationException(e);
    }
  }
}
