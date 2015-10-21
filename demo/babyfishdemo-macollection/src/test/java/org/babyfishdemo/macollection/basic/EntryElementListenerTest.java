package org.babyfishdemo.macollection.basic;

import org.babyfish.collection.MALinkedHashMap;
import org.babyfish.collection.MAMap;
import org.babyfish.collection.MAMap.MAEntry;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.MapReader;
import org.babyfish.collection.event.EntryElementEvent;
import org.babyfish.collection.event.EntryElementListener;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * @author Tao Chen
 */
public class EntryElementListenerTest {

    private MAEntry<Integer, String> entry;
    
    private StringBuilder eventHistory;
    
    @Before
    public void setUp() {
        MAMap<Integer, String> map = new MALinkedHashMap<>();
        map.addConflictVoter(new MapConflictVoter<Integer, String>() {
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
        map.put(1, "One");
        map.put(2, "Two");
        this.entry = map.entrySet().iterator().next();
        this.entry.addEntryElementListener(new EntryElementListener<Integer, String>() {

            @Override
            public void modifying(EntryElementEvent<Integer, String> e) throws Throwable {
                EntryElementListenerTest that = EntryElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":pre-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":pre-attach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }

            @Override
            public void modified(EntryElementEvent<Integer, String> e) throws Throwable {
                EntryElementListenerTest that = EntryElementListenerTest.this;
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    that
                    .eventHistory
                    .append(":post-detach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    that
                    .eventHistory
                    .append(":post-attach(key=")
                    .append(e.getKey())
                    .append(",value=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
    }
    
    @Test
    public void test() {
        
        this.eventHistory = new StringBuilder();
        this.entry.setValue("Unknown");
        Assert.assertEquals(
                ":pre-detach(key=1,value=One)"
                + ":pre-attach(key=1,value=Unknown)"
                + ":post-detach(key=1,value=One)"
                + ":post-attach(key=1,value=Unknown)", 
                this.eventHistory.toString()
        );
        
        /*
         * The other entry { key: 2, value: Two } will be removed automatically
         * because of the MapConflictVoter
         */
        this.eventHistory = new StringBuilder();
        this.entry.setValue("Two");
        Assert.assertEquals(
                ":pre-detach(key=2,value=Two)"
                + ":pre-detach(key=1,value=Unknown)"
                + ":pre-attach(key=1,value=Two)"
                + ":post-detach(key=2,value=Two)"
                + ":post-detach(key=1,value=Unknown)"
                + ":post-attach(key=1,value=Two)", 
                this.eventHistory.toString()
        );
    }
}
