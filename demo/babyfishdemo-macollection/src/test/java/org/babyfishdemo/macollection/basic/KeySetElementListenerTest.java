package org.babyfishdemo.macollection.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAOrderedMap;
import org.babyfish.collection.MAOrderedMap.MAOrderedKeySetView;
import org.babyfish.collection.event.KeySetElementEvent;
import org.babyfish.collection.event.KeySetElementListener;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class KeySetElementListenerTest {

    private MAOrderedMap<Integer, String> map;
    
    private MAOrderedKeySetView<Integer, String> keySet;
    
    private StringBuilder eventHistory;
    
    @Before
    public void setUp() {
        this.map = new MALinkedHashMap<>();
        this.keySet = this.map.keySet();
        this.keySet.addKeySetElementListener(new KeySetElementListener<Integer, String>() {
            @Override
            public void modifying(KeySetElementEvent<Integer, String> e) throws Throwable {
                KeySetElementListenerTest that = KeySetElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":pre-detach(key=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue())
                    .append(")");
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":pre-detach(key=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue())
                    .append(")");
                }
            }
            @Override
            public void modified(KeySetElementEvent<Integer, String> e) throws Throwable {
                KeySetElementListenerTest that = KeySetElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":post-detach(key=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue())
                    .append(")");
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":post-detach(key=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue())
                    .append(")");
                }
            }
        });
    }
    
    @Test
    public void test() {
        this.map.put(1, "I");
        this.map.put(2, "II");
        this.map.put(3, "III");
        this.map.put(4, "IV");
        this.map.put(5, "V");
        this.map.put(6, "VI");
        
        this.eventHistory = new StringBuilder();
        Iterator<Integer> keyIterator = this.map.keySet().descendingIterator();
        while (keyIterator.hasNext()) {
            int key = keyIterator.next();
            if (key % 2 != 0) {
                keyIterator.remove();
            }
        }
        assertMap(this.map, 2, "II", 4, "IV", 6, "VI");
        Assert.assertEquals(
                ":pre-detach(key=5,value=V)" +
                ":post-detach(key=5,value=V)" +
                ":pre-detach(key=3,value=III)" +
                ":post-detach(key=3,value=III)" +
                ":pre-detach(key=1,value=I)" +
                ":post-detach(key=1,value=I)",
                this.eventHistory.toString());
    }
    
    private static void assertMap(Map<Integer, String> map, Object ... keyAndValues) {
        if (keyAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("The length of \"keyAndValues\" must be even number");
        }
        Assert.assertEquals(keyAndValues.length / 2, map.size());
        int index = 0;
        for (Entry<Integer, String> entry : map.entrySet()) {
            Assert.assertEquals(keyAndValues[index++], entry.getKey());
            Assert.assertEquals(keyAndValues[index++], entry.getValue());
        }
    }
}
