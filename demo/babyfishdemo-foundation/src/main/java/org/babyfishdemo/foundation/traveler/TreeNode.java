package org.babyfishdemo.foundation.traveler;

import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
public class TreeNode {

    private String name;
    
    private List<TreeNode> childNodes;
    
    public TreeNode(String name) {
        this(name, (TreeNode[])null);
    }
    
    public TreeNode(String name, TreeNode ... childNodes) {
        Arguments.mustNotContainNullElements("childNodes", childNodes);
        this.name = name;
        if (Nulls.isNullOrEmpty(childNodes)) { 
            this.childNodes = MACollections.emptyList();
        } else {
            this.childNodes = MACollections.wrap(childNodes);
        }
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<TreeNode> getChildNodes() {
        return this.childNodes;
    }
}
