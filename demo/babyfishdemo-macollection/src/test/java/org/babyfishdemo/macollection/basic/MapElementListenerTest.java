package org.babyfishdemo.macollection.basic;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.MapReader;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementListener;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MapElementListenerTest {

    private MANavigableMap<Integer, String> map;
    
    private StringBuilder eventHistory;
    
    @Before
    public void setUp() {
        this.map = new MATreeMap<>();
        this.map.addMapElementListener(new MapElementListener<Integer, String>() {

            @Override
            public void modifying(MapElementEvent<Integer, String> e)
                    throws Throwable {
                MapElementListenerTest that = MapElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":pre-attach(key=")
                    .append(e.getKey(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.ATTACH))
                    .append(')');
                }
            }

            @Override
            public void modified(MapElementEvent<Integer, String> e)
                    throws Throwable {
                MapElementListenerTest that = MapElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey(PropertyVersion.DETACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":post-attach(key=")
                    .append(e.getKey(PropertyVersion.ATTACH))
                    .append(",value=")
                    .append(e.getValue(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
        this.map.addConflictVoter(new MapConflictVoter<Integer, String>() {
            @Override
            public void vote(MapConflictVoterArgs<Integer, String> args) {
                MapReader<Integer, String> reader = args.reader();
                while (reader.read()) {
                    if (args.valueUnifiedComparator().equals(args.newValue(), reader.value())) {
                        args.conflictFound(reader.key());
                    }
                }
            }
        });
    }
    
    @Test
    public void test() {
        this.eventHistory = new StringBuilder();
        this.map.put(1, "One");
        assertMap(this.map, 1, "One");
        Assert.assertEquals(
                ":pre-attach(key=1,value=One):post-attach(key=1,value=One)", 
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(1, "I");
        m.put(2, "II");
        m.put(3, "III");
        m.put(4, "IV");
        m.put(5, "V");
        m.put(6, "VI");
        this.map.putAll(m);
        assertMap(this.map, 1, "I", 2, "II", 3, "III", 4, "IV", 5, "V", 6, "VI");
        Assert.assertEquals(
                ":pre-detach(key=1,value=One)" +
                ":pre-attach(key=1,value=I)" +
                ":pre-attach(key=2,value=II)" +
                ":pre-attach(key=3,value=III)" +
                ":pre-attach(key=4,value=IV)" +
                ":pre-attach(key=5,value=V)" +
                ":pre-attach(key=6,value=VI)" +
                ":post-detach(key=1,value=One)" +
                ":post-attach(key=1,value=I)" +
                ":post-attach(key=2,value=II)" +
                ":post-attach(key=3,value=III)" +
                ":post-attach(key=4,value=IV)" +
                ":post-attach(key=5,value=V)" +
                ":post-attach(key=6,value=VI)", 
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        this.map.subMap(3, true, 4, true).clear();
        assertMap(this.map, 1, "I", 2, "II", 5, "V", 6, "VI");
        Assert.assertEquals(
                ":pre-detach(key=3,value=III)" +
                ":pre-detach(key=4,value=IV)" +
                ":post-detach(key=3,value=III)" +
                ":post-detach(key=4,value=IV)",
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        m = new HashMap<>();
        m.put(3, "III");
        m.put(4, "IV");
        m.put(-5, "V");
        this.map.putAll(m);
        assertMap(this.map, -5, "V", 1, "I", 2, "II", 3, "III", 4, "IV", 6, "VI");
        //The old entry{ key: 4, vaue: "IV" } will be deleted because of MapConflictVoter
        Assert.assertEquals(
                ":pre-detach(key=5,value=V)" +
                ":pre-attach(key=3,value=III)" +
                ":pre-attach(key=4,value=IV)" +
                ":pre-attach(key=-5,value=V)" +
                ":post-detach(key=5,value=V)" +
                ":post-attach(key=3,value=III)" +
                ":post-attach(key=4,value=IV)" +
                ":post-attach(key=-5,value=V)",
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        // Remove all key that are odd number and between 1 and 5(1 and 3 will be deleted).
        Iterator<Integer> keyIterator = this.map.keySet().subSet(1, true, 5, true).iterator();
        while (keyIterator.hasNext()) {
            int key = keyIterator.next();
            if (key % 2 != 0) {
                keyIterator.remove();
            }
        }
        assertMap(this.map, -5, "V", 2, "II", 4, "IV", 6, "VI");
        //The old entry{ key: 4, vaue: "IV" } will be deleted because of MapConflictVoter
        Assert.assertEquals(
                ":pre-detach(key=1,value=I)" +
                ":post-detach(key=1,value=I)" +
                ":pre-detach(key=3,value=III)" +
                ":post-detach(key=3,value=III)",
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        this.map.values().retainAll(MACollections.wrap("VI", "VII", "VIII"));
        assertMap(this.map, 6, "VI");
        Assert.assertEquals(
                ":pre-detach(key=-5,value=V)" +
                ":pre-detach(key=2,value=II)" +
                ":pre-detach(key=4,value=IV)" +
                ":post-detach(key=-5,value=V)" +
                ":post-detach(key=2,value=II)" +
                ":post-detach(key=4,value=IV)",
                this.eventHistory.toString());
        
        this.eventHistory = new StringBuilder();
        this.map.descendingMap().entrySet().iterator().next().setValue("Six");
        assertMap(this.map, 6, "Six");
        Assert.assertEquals(
                ":pre-detach(key=6,value=VI)" +
                ":pre-attach(key=6,value=Six)" +
                ":post-detach(key=6,value=VI)" +
                ":post-attach(key=6,value=Six)",
                this.eventHistory.toString()
        );
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
