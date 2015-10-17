package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
public class Text extends CharacterData {
    
    public Text(String data) {
        super(data);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.TEXT;
    }
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitText(this);
    }
}
