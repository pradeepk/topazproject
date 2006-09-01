/*
 * ContextLocationList.java
 *
 * Created on 30 marzo 2002, 8.50
 */

package org.apache.xpath.axes;

import xpointer.Location;
/**
 *
 * @author  tax
 * @version 
 */
public interface ContextLocationList {

    public Location getCurrentLocation();
    
    public int getCurrentPos();
    
    public void setCurrentPos(int i);
    
    public int size();
    
}

