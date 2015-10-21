package org.babyfishdemo.macollection.basic;

import java.util.ListIterator;

import org.babyfish.collection.MAArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.MAList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.collection.event.ListElementEvent;
import org.babyfish.collection.event.ListElementListener;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * (1) For detached element, 
 * ListElementEvent.getIndex(PropertyVersion.DETACH) means the index before deleting
 * (2) For attached element, 
 * ListElementEvent.getIndex(PropertyVersion.ATTACH) means the index after insertion
 *
 * @author Tao Chen
 */
public class ListElementListenerTest {
 
    private MAList<Integer> list;
    
    private StringBuilder eventHistory;
    
    @Before
    public void setUp() {
        this.list = new MAArrayList<>();
        this.list.addListElementListener(new ListElementListener<Integer>() {
            @Override
            public void modifying(ListElementEvent<Integer> e) {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    ListElementListenerTest
                    .this
                    .eventHistory
                    .append(":pre-detach(index=")
                    .append(e.getIndex(PropertyVersion.DETACH))
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    ListElementListenerTest
                    .this
                    .eventHistory
                    .append(":pre-attach(index=")
                    .append(e.getIndex(PropertyVersion.ATTACH))
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
            @Override
            public void modified(ListElementEvent<Integer> e) {
                if (e.getModificationType().contains(PropertyVersion.DETACH)) {
                    ListElementListenerTest
                    .this
                    .eventHistory
                    .append(":post-detach(index=")
                    .append(e.getIndex(PropertyVersion.DETACH))
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.DETACH))
                    .append(')');
                }
                if (e.getModificationType().contains(PropertyVersion.ATTACH)) {
                    ListElementListenerTest
                    .this
                    .eventHistory
                    .append(":post-attach(index=")
                    .append(e.getIndex(PropertyVersion.ATTACH))
                    .append(",element=")
                    .append(e.getElement(PropertyVersion.ATTACH))
                    .append(')');
                }
            }
        });
        this.list.addConflictVoter(new ListConflictVoter<Integer>() {
            @Override
            public void vote(ListConflictVoterArgs<Integer> args) {
                ListReader<Integer> reader = args.reader();
                while (reader.read()) {
                    if (args.unifiedComparator().equals(args.newElement(), reader.element())) {
                        args.conflictFound(reader.index());
                    }
                }
            }
        });
    }
 
    @Test
    public void test() {
    
        this.eventHistory = new StringBuilder();
        this.list.add(70);
        assertList(this.list, 70);
        Assert.assertEquals(
            ":pre-attach(index=0,element=70):post-attach(index=0,element=70)",
            this.eventHistory.toString()
        );
 
        this.eventHistory = new StringBuilder();
        this.list.add(0, -954);
        assertList(this.list, -954, 70);
        Assert.assertEquals(
            ":pre-attach(index=0,element=-954):post-attach(index=0,element=-954)",
            this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        this.list.addAll(1, MACollections.wrap(34, 93, 723, -36, 30672, -63));
        assertList(this.list, -954, 34, 93, 723, -36, 30672, -63, 70);
        Assert.assertEquals(
            ":pre-attach(index=1,element=34)" +
            ":pre-attach(index=2,element=93)" +
            ":pre-attach(index=3,element=723)" +
            ":pre-attach(index=4,element=-36)" +
            ":pre-attach(index=5,element=30672)" +
            ":pre-attach(index=6,element=-63)"
            +
            ":post-attach(index=1,element=34)" +
            ":post-attach(index=2,element=93)" +
            ":post-attach(index=3,element=723)" +
            ":post-attach(index=4,element=-36)" +
            ":post-attach(index=5,element=30672)" +
            ":post-attach(index=6,element=-63)",
            this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        this.list.subList(2, 4).clear();
        assertList(this.list, -954, 34, -36, 30672, -63, 70);
        Assert.assertEquals(
            ":pre-detach(index=2,element=93):pre-detach(index=3,element=723)" +
            ":post-detach(index=2,element=93):post-detach(index=3,element=723)",
            this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        this.list.subList(2, 4).addAll(1, MACollections.wrap(70, 45, -897, 34, 2754, 78));
        assertList(this.list, -954, -36, 70, 45, -897, 34, 2754, 78, 30672, -63);
        Assert.assertEquals(
            ":pre-detach(index=1,element=34)"
            + ":pre-detach(index=5,element=70)"
            + ":pre-attach(index=2,element=70)"
            + ":pre-attach(index=3,element=45)"
            + ":pre-attach(index=4,element=-897)"
            + ":pre-attach(index=5,element=34)"
            + ":pre-attach(index=6,element=2754)"
            + ":pre-attach(index=7,element=78)"
            + ":post-detach(index=1,element=34)"
            + ":post-detach(index=5,element=70)"
            + ":post-attach(index=2,element=70)"
            + ":post-attach(index=3,element=45)"
            + ":post-attach(index=4,element=-897)"
            + ":post-attach(index=5,element=34)"
            + ":post-attach(index=6,element=2754)"
            + ":post-attach(index=7,element=78)",
            this.eventHistory.toString()
        );
        
        this.eventHistory = new StringBuilder();
        ListIterator<Integer> listItr = this.list.subList(4, 10).listIterator();
        while (listItr.hasNext()) {
            Integer value = listItr.next();
            if (value < 0) {
                listItr.remove();
            } else {
                listItr.set(value / 10 * 10);
            }
        }
        assertList(this.list, -954, -36, 45, 30, 2750, 70, 30670);
        Assert.assertEquals(
            ":pre-detach(index=4,element=-897)" +
            ":post-detach(index=4,element=-897)" +
            ":pre-detach(index=4,element=34):pre-attach(index=4,element=30)" +
            ":post-detach(index=4,element=34):post-attach(index=4,element=30)" +
            ":pre-detach(index=5,element=2754):pre-attach(index=5,element=2750)" +
            ":post-detach(index=5,element=2754):post-attach(index=5,element=2750)" +
            ":pre-detach(index=2,element=70):pre-detach(index=6,element=78):pre-attach(index=5,element=70)" +
            ":post-detach(index=2,element=70):post-detach(index=6,element=78):post-attach(index=5,element=70)" +
            ":pre-detach(index=6,element=30672):pre-attach(index=6,element=30670)" +
            ":post-detach(index=6,element=30672):post-attach(index=6,element=30670)" +
            ":pre-detach(index=7,element=-63)" +
            ":post-detach(index=7,element=-63)",
            this.eventHistory.toString()
        );
    }
    
    private static void assertList(MAList<Integer> list, int ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (Integer element : list) {
            Assert.assertEquals(elements[index++], element.intValue());
        }
    }
}
