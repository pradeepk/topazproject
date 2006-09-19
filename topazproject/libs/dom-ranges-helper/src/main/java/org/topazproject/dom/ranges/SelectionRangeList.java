/* $HeadURL::                                                                          $
 * $Id$
 *
 * Copyright (c) 2006 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Educational Community License version 1.0
 * http://opensource.org/licenses/ecl1.php
 */
package org.topazproject.dom.ranges;

import java.util.ArrayList;
import java.util.List;

/**
 * Maintains a document ordered non overlapping list of ranges. See {@link #insert insert} for how
 * the non-overlapping constraint is enforced.
 *
 * @author Pradeep Krishnan
 */
public class SelectionRangeList {
  private ArrayList selectionRanges = new ArrayList();

  /**
   * Returns the size of this list.
   *
   * @return the size
   */
  public int size() {
    return selectionRanges.size();
  }

  /**
   * Gets the SelectionRange at the given index.
   *
   * @param i the index
   *
   * @return the SelectionRange
   */
  public SelectionRange get(int i) {
    return (SelectionRange) selectionRanges.get(i);
  }

  /**
   * Inserts a range in this ordered list; splitting it or an already inserted range as necessary
   * so that none of the ranges in this list overlap. UserData is copied (not cloned) into each
   * fragment. Therefore the constraint (size() == number of inserts) does not hold good when
   * overlapping ranges are inseted.
   *
   * @param selectionRange the range to insert
   */
  public void insert(SelectionRange selectionRange) {
    insertAtOrAfter(0, selectionRange);
  }

  private void insertAtOrAfter(int i, SelectionRange newSelectionRange) {
    int            length = selectionRanges.size();

    SelectionRange selectionRange = null;

    // scan past selectionRanges that are before the new selectionRange
    while ((i < length)
            && newSelectionRange.isAfter(selectionRange = (SelectionRange) selectionRanges.get(i)))
      i++;

    // if the next selectionRange is clearly after or at end, then insert this before
    if ((i >= length) || selectionRange.isAfter(newSelectionRange)) {
      selectionRanges.add(i, newSelectionRange);

      return;
    }

    // there is an overlap. break that into 'before, 'shared' and 'after'
    // first create a new selectionRange for 'before'
    SelectionRange before = null;

    if (selectionRange.startsBefore(newSelectionRange))
      before = selectionRange.splitBefore(newSelectionRange);
    else if (newSelectionRange.startsBefore(selectionRange))
      before = newSelectionRange.splitBefore(selectionRange);

    if (before != null)
      selectionRanges.add(i++, before);

    // now both 'selectionRange' and 'newSelectionRange' start at the same point
    if (selectionRange.endsAfter(newSelectionRange)) {
      // 'shared' is the 'newSelectionRange'. copy userDatas and insert before 'selectionRange'
      newSelectionRange.addAllUserData(selectionRange.getUserDataList());
      selectionRanges.add(i++, newSelectionRange);

      //  'selectionRange' now starts where 'newSelectionRange' ends (ie. it is the 'after' fragment)
      selectionRange.setAsContinuationOf(newSelectionRange);
    } else {
      // 'shared' is the 'selectionRange'. copy userDatas
      selectionRange.addAllUserData(newSelectionRange.getUserDataList());

      if (newSelectionRange.endsAfter(selectionRange)) {
        //  'newSelectionRange' starts where 'selectionRange' ends
        newSelectionRange.setAsContinuationOf(selectionRange);

        // at this point 'newSelectionRange' is after 'selectionRange' and so repeat the whole process
        insertAtOrAfter(i + 1, newSelectionRange);
      }
    }
  }
}
