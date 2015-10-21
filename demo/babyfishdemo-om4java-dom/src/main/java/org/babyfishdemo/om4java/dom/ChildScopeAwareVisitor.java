package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen
 */
public interface ChildScopeAwareVisitor extends Visitor {

    void enterChildScope(Element element);
    
    void leaveChildScope(Element element);
}
