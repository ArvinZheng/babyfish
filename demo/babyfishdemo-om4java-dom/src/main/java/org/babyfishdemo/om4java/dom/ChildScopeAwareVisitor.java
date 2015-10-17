package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public interface ChildScopeAwareVisitor extends Visitor {

    void enterChildScope(Element element);
    
    void leaveChildScope(Element element);
}
