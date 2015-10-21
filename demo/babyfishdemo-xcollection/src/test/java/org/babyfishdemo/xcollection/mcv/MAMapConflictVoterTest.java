package org.babyfishdemo.xcollection.mcv;

import java.util.List;
import java.util.Map;
 
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.HashMap;                    
import org.babyfish.collection.conflict.MAMapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
 
/**
 * @author Tao Chen
 */
public class MAMapConflictVoterTest extends AbstractMapConflictVoterTest {
 
    @Override
    protected MAMapConflictVoter<Long, String> createVoter() {
        return new MAMapConflictVoter<Long, String>() {
        
            private Map<String, List<Long>> keysGroupByDir = new HashMap<>();
 
            @Override
            public void vote(MapConflictVoterArgs<Long, String> args) {
            
                /*
                 * Be different with the implementation class of org.babyfish.collection.conflict.MapConflictVoter<K, V>
                 * you can NOT invoke "args.reader()" in this method of the derived classes of 
                 * org.babyfish.collection.conflict.MAMapConflictVoter, the exception "java.lang.UnsupportedOperationException"
                 * will be raise if you do thant. 
                 */
                 
                String newValue = args.newValue();
                String newDir = newValue.substring(0, newValue.lastIndexOf('/'));
                List<Long> keys = this.keysGroupByDir.get(newDir);
                if (keys != null) {
                    int maxFileChangeEventCount = newDir.equals(SPECIAL_DIR) ? 4 : 3;
                    int size = keys.size();
                    int conflictCount = size - (maxFileChangeEventCount - 1); // "-1" for new value to be added.
                    if (conflictCount > 0) {
                        args.conflictFound(keys.subList(0, conflictCount));
                    }
                }
            }
 
            @Override
            protected void detach(Long key, String value) {
                String dir = value.substring(0, value.lastIndexOf('/'));
                List<Long> keys = this.keysGroupByDir.get(dir);
                keys.remove(key);
            }
 
            @Override
            protected void attach(Long key, String value) {
                String dir = value.substring(0, value.lastIndexOf('/'));
                List<Long> keys = this.keysGroupByDir.get(dir);
                if (keys == null) {
                    this.keysGroupByDir.put(dir, keys = new ArrayList<>());
                }
                keys.add(key);
            }
        };
    }
}
