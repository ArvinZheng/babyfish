package org.babyfishdemo.macollection.basic;

import java.util.Iterator;

import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MASet;
import org.babyfish.collection.ReplacementRule;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.ElementListener;
import org.babyfish.modificationaware.event.PropertyVersion;
 
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ElementListenerTest {
 
    private MASet<Integer> set;
    
    private StringBuilder eventHistory;
    
    @Before
    public void setUp() {
        this.set = new MALinkedHashSet<>(ReplacementRule.NEW_REFERENCE_WIN);
        this.set.addElementListener(new ElementListener<Integer>() {
            @Override
            public void modifying(ElementEvent<Integer> e) {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    ElementListenerTest
                    .this
                    .eventHistory
                    .append(":pre-detach(")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    ElementListenerTest
                    .this
                    .eventHistory
                    .append(":pre-attach(")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
            @Override
            public void modified(ElementEvent<Integer> e) {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    ElementListenerTest
                    .this
                    .eventHistory
                    .append(":post-detach(")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    ElementListenerTest
                    .this
                    .eventHistory
                    .append(":post-attach(")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
    }
 
    @Test
    public void test() {
    
        this.eventHistory = new StringBuilder();
        this.set.add(56);
        Assert.assertEquals(":pre-attach(56):post-attach(56)", this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        this.set.addAll(MACollections.wrap(34, 78, -37, 56, 61, -23, -82));
        Assert.assertEquals(
                ":pre-attach(34):pre-attach(78):pre-attach(-37):pre-detach(56):pre-attach(56):pre-attach(61):pre-attach(-23):pre-attach(-82)" +
                ":post-attach(34):post-attach(78):post-attach(-37):post-detach(56):post-attach(56):post-attach(61):post-attach(-23):post-attach(-82)", 
                this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        this.set.removeAll(MACollections.wrap(-100, 34, 78, -82, 230, 259));
        Assert.assertEquals(
                ":pre-detach(34):pre-detach(78):pre-detach(-82)" +
                ":post-detach(34):post-detach(78):post-detach(-82)", 
                this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        Iterator<Integer> itr = this.set.iterator();
        while (itr.hasNext()) {
            Integer value = itr.next();
            if (value < 0) {
                itr.remove();
            }
        }
        Assert.assertEquals(
                ":pre-detach(-37):post-detach(-37)" +
                ":pre-detach(-23):post-detach(-23)", 
                this.eventHistory.toString()
        );
    }
}
