package org.babyfishdemo.xcollection.mcv;

import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
 
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.lang.Arguments;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public abstract class AbstractMapConflictVoterTest {
 
    private static final Pattern LINUX_ABS_FILE_PATH = Pattern.compile("^(/[^/]{1,255})+$");
    
    protected static final String SPECIAL_DIR = "/usr/include";
 
    /*
     * In order to make our example is easy to understand, 
     * we use org.babyfish.collection.XOrderedMap,
     * NOT org.babyfish.collection.XMap.
     */
    private XOrderedMap<Long, String> fileChangeEventMap;
    
    /*
     * This voter guarantees:
     * 
     * (1) For each directory, its file change event count must <= 3. 
     * (2) Specially, for directory "/usr/include", its file change event count must <= 4.
     */
    protected abstract MapConflictVoter<Long, String> createVoter();
    
    @Before
    public void setUp() {
        this.fileChangeEventMap = new LinkedHashMap<>();
        
        /*
         * Add validators to the map.
         */
        this.fileChangeEventMap.addValueValidator(Validators.<String>notNull());
        this.fileChangeEventMap.addValueValidator(Validators.instanceOf(String.class));
        this.fileChangeEventMap.addValueValidator(new Validator<String>() {
            @Override
            public void validate(String e) {
                if (!LINUX_ABS_FILE_PATH.matcher(e).matches()) {
                    throw new IllegalArgumentException("Illegal formant of new value.");
                }
            }
        });
        
        /*
         * Add conflict voter to the map.
         */
        this.fileChangeEventMap.addConflictVoter(this.createVoter());
    }
    
    @Test
    public void testDirectoryContainsAtMost3Files() {
        
        this.fileChangeEventMap.put(1001L, "/var/tmp/1.cpp");
        this.fileChangeEventMap.put(1002L, "/var/tmp/2.cpp");
        this.fileChangeEventMap.put(1003L, "/var/tmp/3.cpp");
        assertOrderedMap(this.fileChangeEventMap, 1001L, "/var/tmp/1.cpp", 1002L, "/var/tmp/2.cpp", 1003L, "/var/tmp/3.cpp");
        
        /*
         * Add { 1004L, "/var/tmp/4.cpp" } into the map, the MapConflictVoter must make sure the
         * directory "/var/tmp" can contains at most 3 file change events so that { 1001L : "/var/tmp/1.cpp" } is
         * voted to be the conflict key-value pair and it is removed automatically 
         */
        this.fileChangeEventMap.put(1004L, "/var/tmp/4.cpp");
        assertOrderedMap(this.fileChangeEventMap, 1002L, "/var/tmp/2.cpp", 1003L, "/var/tmp/3.cpp", 1004L, "/var/tmp/4.cpp");
        
        /*
         * Add { 1005L, "/var/tmp/5.cpp" } and { 1006L, "/var/tmp/6.cpp"} into the map, the MapConflictVoter must make sure the
         * directory "/var/tmp" can contains at most 3 file change events so that { 1002L, "/var/tmp/2.cpp" } and 
         * { 1003L, "/var/tmp/3.cpp" } are voted to be the conflict key-value pairs and they are removed automatically 
         */
        this.fileChangeEventMap.putAll(prepareMap(1005L, "/var/tmp/5.cpp", 1006L, "/var/tmp/6.cpp"));
        assertOrderedMap(this.fileChangeEventMap, 1004L, "/var/tmp/4.cpp", 1005L, "/var/tmp/5.cpp", 1006L, "/var/tmp/6.cpp");
        
        /*
         * Add { 1007L, "/var/tmp/7.cpp" }, { 1008L, "/var/tmp/7.cpp" } into to the map, the MapConflictVoter must make sure the
         * directory "/var/tmp" can contains at most 3 file change events so that { 1004L, "/var/tmp/4.cpp" } and 
         * { 1005L, "/var/tmp/5.cpp" } are voted to be the conflict key-value pairs and they are removed automatically 
         */
        this.fileChangeEventMap.putAll(prepareMap(1007L, "/var/tmp/7.cpp", 1008L, "/var/tmp/7.cpp"));
        assertOrderedMap(this.fileChangeEventMap, 1006L, "/var/tmp/6.cpp", 1007L, "/var/tmp/7.cpp", 1008L, "/var/tmp/7.cpp");
        
        /*
         * Add { 1009L, "/var/tmp/A.cpp" }, { 1010L, "/var/tmp/B.cpp" }, { 1011L, "/var/tmp/C.cpp" }, { 1012L, "/var/tmp/D.cpp" }, 
         * { 1013L, "/var/tmp/E.cpp"}, { 1014L, "/var/tmp/F.cpp" } and { 1015L, "/var/tmp/G.cpp" } into to the map, the MapConflictVoter 
         * must make sure the directory "/var/tmp" can contains at most 3 file change events so that { 1006L, "/var/tmp/6.cpp" }, 
         * { 1007L, "/var/tmp/7.cpp" }, { 1008L, "/var/tmp/7.cpp" }, { 1009L, "/var/tmp/A.cpp" }, { 1010L, "/var/tmp/B.cpp" },
         * { 1011L, "/var/tmp/C.cpp" } and { 1012L, "/var/tmp/D.cpp" } are voted to be the conflict key-value pairs and they are 
         * removed automatically 
         */
        this.fileChangeEventMap.putAll(
                prepareMap(
                    1009L, "/var/tmp/A.cpp", 
                    1010L, "/var/tmp/B.cpp",
                    1011L, "/var/tmp/C.cpp",
                    1012L, "/var/tmp/D.cpp",
                    1013L, "/var/tmp/E.cpp",
                    1014L, "/var/tmp/F.cpp",
                    1015L, "/var/tmp/G.cpp"
                )
        );
        assertOrderedMap(
            this.fileChangeEventMap, 
            1013L, "/var/tmp/E.cpp", 
            1014L, "/var/tmp/F.cpp", 
            1015L, "/var/tmp/G.cpp"
        );
    }
    
    @Test
    public void testSpecialDirectoryContainsAtMost4Files() {
        
        this.fileChangeEventMap.put(1001L, "/usr/include/1.cpp");
        this.fileChangeEventMap.put(1002L, "/usr/include/2.cpp");
        this.fileChangeEventMap.put(1003L, "/usr/include/3.cpp");
        this.fileChangeEventMap.put(1004L, "/usr/include/4.cpp");
        assertOrderedMap(
            this.fileChangeEventMap, 
            1001L, "/usr/include/1.cpp", 
            1002L, "/usr/include/2.cpp", 
            1003L, "/usr/include/3.cpp",
            1004L, "/usr/include/4.cpp");
        
        /*
         * Add { 1005L, "/usr/include/5.cpp" } into the map, the MapConflictVoter must make sure the
         * special directory "/usr/include" can contains at most 4 file change events so that 
         * { 1001L : "/usr/include/1.cpp" } is voted to be the conflict key-value pair and 
         * it is removed automatically 
         */
        this.fileChangeEventMap.put(1005L, "/usr/include/5.cpp");
        assertOrderedMap(
            this.fileChangeEventMap, 
            1002L, "/usr/include/2.cpp", 
            1003L, "/usr/include/3.cpp", 
            1004L, "/usr/include/4.cpp", 
            1005L, "/usr/include/5.cpp"
        );
        
        /*
         * Add { 1006L, "/usr/include/6.cpp" } and { 1007L, "/usr/include/7.cpp"} into the map, the MapConflictVoter must make sure the
         * special directory "/usr/include" can contains at most 4 file change events so that { 1002L, "/usr/include/2.cpp" } and 
         * { 1003L, "/usr/include/3.cpp" } are voted to be the conflict key-value pairs and they are removed automatically 
         */
        this.fileChangeEventMap.putAll(prepareMap(1006L, "/usr/include/6.cpp", 1007L, "/usr/include/7.cpp"));
        assertOrderedMap(
            this.fileChangeEventMap, 
            1004L, "/usr/include/4.cpp", 
            1005L, "/usr/include/5.cpp", 
            1006L, "/usr/include/6.cpp", 
            1007L, "/usr/include/7.cpp"
        );
        
        /*
         * Add { 1008L, "/usr/include/8.cpp" }, { 1009L, "/usr/include/9.cpp" } into to the map, the MapConflictVoter must make sure the
         * special directory "/usr/include" can contains at most 4 file change events so that { 1004L, "/usr/include/4.cpp" } and 
         * { 1005L, "/usr/include/5.cpp" } are voted to be the conflict key-value pairs and they are removed automatically 
         */
        this.fileChangeEventMap.putAll(prepareMap(1008L, "/usr/include/8.cpp", 1009L, "/usr/include/8.cpp"));
        assertOrderedMap(
            this.fileChangeEventMap, 
            1006L, "/usr/include/6.cpp", 
            1007L, "/usr/include/7.cpp", 
            1008L, "/usr/include/8.cpp", 
            1009L, "/usr/include/8.cpp"
        );
        
        /*
         * Add { 1010L, "/usr/include/A.cpp" }, { 1011L, "/usr/include/B.cpp" }, { 1012L, "/usr/include/C.cpp" }, { 1013L, "/usr/include/D.cpp" }, 
         * { 1014L, "/usr/include/E.cpp"}, { 1015L, "/usr/include/F.cpp" } and { 1016L, "/usr/include/G.cpp" } into to the map, 
         * the MapConflictVoter must make sure the special directory "/usr/include" can contains at most 4 file change events so that 
         * { 1006L, "/usr/include/6.cpp" }, { 1007L, "/usr/include/7.cpp" }, { 1008L, "/usr/include/8.cpp" }, { 1009L, "/usr/include/8.cpp" }, 
         * { 1010L, "/usr/include/A.cpp" }, { 1011L, "/usr/include/B.cpp" } and { 1012L, "/usr/include/C.cpp" } are voted to be 
         * the conflict key-value pairs and they are 
         * removed automatically 
         */
        this.fileChangeEventMap.putAll(
                prepareMap(
                    1010L, "/usr/include/A.cpp", 
                    1011L, "/usr/include/B.cpp",
                    1012L, "/usr/include/C.cpp",
                    1013L, "/usr/include/D.cpp",
                    1014L, "/usr/include/E.cpp",
                    1015L, "/usr/include/F.cpp",
                    1016L, "/usr/include/G.cpp"
                )
        );
        assertOrderedMap(
            this.fileChangeEventMap, 
            1013L, "/usr/include/D.cpp", 
            1014L, "/usr/include/E.cpp", 
            1015L, "/usr/include/F.cpp", 
            1016L, "/usr/include/G.cpp"
        );
    }
    
    private static Map<Long, String> prepareMap(Object ... keysAndValues) {
        if (keysAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("The parameter count must be even number");
        }
        int size = keysAndValues.length / 2;
        Map<Long, String> map = new LinkedHashMap<>((size * 4 + 2) / 3);
        for (int i = 0; i < size; i++) {
            Long key = (Long)Arguments.mustBeInstanceOfValue("keysAndValues[2 * " + i + ']', keysAndValues[2 * i], Long.class);
            String value = (String)Arguments.mustBeInstanceOfValue("keysAndValues[2 * " + i + " + 1]", keysAndValues[2 * i + 1], String.class);
            map.put(key, value);
        }
        return map;
    }
    
    private static void assertOrderedMap(XOrderedMap<Long, String> map, Object ... keysAndValues) {
        Assert.assertEquals(keysAndValues.length, map.size() * 2);
        int index = 0;
        for (Entry<Long, String> entry : map.entrySet()) {
            Long key = (Long)Arguments.mustBeInstanceOfValue("keysAndValues[" + index + ']', keysAndValues[index++], Long.class);
            String value = (String)Arguments.mustBeInstanceOfValue("keysAndValues[" + index + ']', keysAndValues[index++], String.class);
            Assert.assertEquals(key, entry.getKey());
            Assert.assertEquals(value, entry.getValue());
        }
    }
}
