package org.babyfishdemo.xcollection.mcv;

import java.util.List;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.MapReader;
 
/**
 * @author Tao Chen
 */
public class MapConflictVoterTest extends AbstractMapConflictVoterTest {
 
    @Override
    protected MapConflictVoter<Long, String> createVoter() {
        return new MapConflictVoter<Long, String>() {
            @Override
            public void vote(MapConflictVoterArgs<Long, String> args) {    
            	
            	String newValue = args.newValue();
                String newDir = newValue.substring(0, newValue.lastIndexOf('/'));
                List<Long> keys = new ArrayList<>();
                
                /*
                 * Create an org.babyfish.collection.conflict.MapReader to iterate the key-value pairs.
                 * Be different with java.util.Iterator
                 * (1) org.babyfish.collection.conflict.MapReader always skips the key-value pairs 
                 * that have been voted to be conflict by this conflict voter.
                 * (2) org.babyfish.collection.conflict.MapReader can iterator the key-value pairs
                 * that are going to be added(but haven't been added) into the current map 
                 * and have been processed by this conflict voter.
                 */
                MapReader<Long, String> reader = args.reader();
                while (reader.read()) {
                    Long key = reader.key();
                    String value = reader.value();
                    String dir = value.substring(0, value.lastIndexOf('/'));
                    if (dir.equals(newDir)) {
                        keys.add(key);
                    }
                }
                int maxFileChangeEventCount = newDir.equals(SPECIAL_DIR) ? 4 : 3;
                int size = keys.size();
                int conflictCount = size - (maxFileChangeEventCount - 1); // "-1" for new value to be added.
                if (conflictCount > 0) {
                    args.conflictFound(keys.subList(0, conflictCount));
                }
            }
        };
    }
}
