package xpointer;
/*
 * TextPoint.java
 *
 * Created on 16 gennaio 2002, 9.08
 */

import org.w3c.dom.*;

/**
 * This class represents a character point according to the XPointer CR.
 * It has a container text node and an index.
 */
public class TextPoint {

    private Node container;
    
    private int index;
    
    private TextTree textTree;
    
      
    /** Creates new TextPoint
     * @param the tree which the point belongs to.  
     */
    public TextPoint(TextTree textTree) {
        this.textTree = textTree;
    }

    /** Getter for property container.
     * @return Value of property container.
     */
    public Node getContainer() {
        return container;
    }
    
    /** Setter for property container.
     * @param container New value of property container.
     */
    public void setContainer(Node container) {
        this.container = container;
    }
    
    /** Getter for property index.
     * @return Value of property index.
     */
    public int getIndex() {
        return index;
    }
    
    /** Setter for property index.
     * @param index New value of property index.
     */
    public void setIndex(int index) {
        this.index = index;
    }
    
  
    /**
     * Returns the number of characters preceding the point 
     * inside its tree.
     */
    public int retrievePrecedingCharacters()
    {
        TextNodeFragment [] nodiTesto = textTree.getTextNodeFragments();
        int numcar=0; /*i caratteri che stanno prima del punto*/
        int i;
        
        for(i=0;i<nodiTesto.length;i++)
        {
            if(nodiTesto[i].getNode()==container)
                break;
            else
                numcar += nodiTesto[i].getNode().getNodeValue().length()-nodiTesto[i].getStartIndex();
        }
        
        numcar += index - nodiTesto[i].getStartIndex();
         
        return numcar;
    }
    
    /**
     * Returns the tree which the point belongs to.
     */
    protected TextTree getTextTree()
    {
        return textTree; 
    }
   
}
