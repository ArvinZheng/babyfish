package org.babyfishdemo.om4java.dom;

/**
 * @author Tao Chen
 */
public enum NodeType {

    ELEMENT(1, Element.class),
    
    ATTRIBUTE(2, Attribute.class),
    
    TEXT(3, Text.class),
    
    COMMENT(8, Comment.class),
    
    ;
    
    private int w3cType;
    
    private Class<? extends Node> javaType;
    
    private NodeType(int w3cType, Class<? extends Node> javaType) {
        this.w3cType = w3cType;
        this.javaType = javaType;
    }
    
    public int getW3cType() {
        return w3cType;
    }

    public Class<? extends Node> toJavaType() {
        return this.javaType;
    }
    
    public static NodeType fromW3cType(int w3cType) {
        for (NodeType nodeType : values()) {
            if (nodeType.w3cType == w3cType) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException(
                "The w3cType \""
                + w3cType
                + "\"is not accepted by this simplified implementation");
    }
}
