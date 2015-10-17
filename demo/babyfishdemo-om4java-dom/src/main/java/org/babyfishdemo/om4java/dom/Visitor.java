package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface Visitor {

    void visitElement(Element element);
    
    void visitAttribute(Attribute attribute);
    
    void visitText(Text text);
    
    void visitComment(Comment comment);
}
