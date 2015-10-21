package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen
 */
public class Comment extends CharacterData {

    public Comment(String data) {
        super(data);
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.COMMENT;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visitComment(this);
    }
}
