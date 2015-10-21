package org.babyfishdemo.xcollection.lcv;

import java.util.List;
import java.util.regex.Pattern;
 
import org.babyfish.collection.ArrayList;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.XList;
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.ListReader;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class ListConflictVoterTest {
 
    private static final Pattern LINUX_ABS_FILE_PATH = Pattern.compile("^(/[^/]{1,255})+$");
    
    private static final String SPECIAL_DIR = "/usr/include";
 
    private XList<String> fileChangeEventList;
    
    @Before
    public void setUp() {
        this.fileChangeEventList = new ArrayList<>();
        
        /*
         * Add validators to the list.
         */
        this.fileChangeEventList.addValidator(Validators.<String>notNull());
        this.fileChangeEventList.addValidator(Validators.instanceOf(String.class));
        this.fileChangeEventList.addValidator(new Validator<String>() {
            @Override
            public void validate(String e) {
                if (!LINUX_ABS_FILE_PATH.matcher(e).matches()) {
                    throw new IllegalArgumentException("Illegal formant of new element.");
                }
            }
        });
        
        /*
         * Add conflict voter to the list.
         */
        this.fileChangeEventList.addConflictVoter(new ListConflictVoter<String>() {
            @Override
            public void vote(ListConflictVoterArgs<String> args) {
                
            	String newElement = args.newElement();
                String newDir = newElement.substring(0, newElement.lastIndexOf('/'));
                List<Integer> indexes = new ArrayList<>();
                
                /*
                 * Create an org.babyfish.collection.conflict.ListReader to iterate the elements.
                 * Be different with java.util.Iterator and java.util.ListIterator,
                 * (1) org.babyfish.collection.conflict.ListReader always skips the elements 
                 * that have been voted to be conflict by this conflict voter.
                 * (2) org.babyfish.collection.conflict.ListReader can iterate the elements
                 * that are going to be added(but haven't been added) into the current list
                 * and has been processed by this conflict voter.
                 * 
                 * In this example, this list is used to retain many linux-style file paths.
                 * (1) For each directory, its file change event count must <= 3. 
                 * (2) Specially, for directory "/usr/include", its file change event count must <= 4.
                 */
                ListReader<String> reader = args.reader();
                while (reader.read()) {
                    int index = reader.index();
                    String e = reader.element();
                    String dir = e.substring(0, e.lastIndexOf('/'));
                    if (dir.equals(newDir)) {
                        indexes.add(index);
                    }
                }
                int maxFileChangeEventCount = newDir.equals(SPECIAL_DIR) ? 4 : 3;
                int size = indexes.size();
                int conflictCount = size - (maxFileChangeEventCount - 1); // "-1" for new element to be added.
                if (conflictCount > 0) {
                    args.conflictFound(indexes.subList(0, conflictCount));
                }
            }
        });
    }
    
    @Test
    public void testDirectoryContainsAtMost3Files() {
        
        this.fileChangeEventList.add("/var/tmp/1.cpp");
        this.fileChangeEventList.add("/var/tmp/2.cpp");
        this.fileChangeEventList.add("/var/tmp/3.cpp");
        assertList(this.fileChangeEventList, "/var/tmp/1.cpp", "/var/tmp/2.cpp", "/var/tmp/3.cpp");
        
        /*
         * Add "/var/tmp/4.cpp" into the list, the ListConflictVoter must make sure the
         * directory "/var/tmp" can contain at most 3 file change events so that "/var/tmp/1.cpp" is
         * voted to be the conflict element and it is removed automatically 
         */
        this.fileChangeEventList.add("/var/tmp/4.cpp");
        assertList(this.fileChangeEventList, "/var/tmp/2.cpp", "/var/tmp/3.cpp", "/var/tmp/4.cpp");
        
        /*
         * Add "/var/tmp/5.cpp" and ""/var/tmp/6.cpp"" into the list, the ListConflictVoter must make sure the
         * directory "/var/tmp" can contain at most 3 file change events so that "/var/tmp/2.cpp" and "/var/tmp/3.cpp" are
         * voted to be the conflict elements and they are removed automatically 
         */
        this.fileChangeEventList.addAll(MACollections.wrap("/var/tmp/5.cpp", "/var/tmp/6.cpp"));
        assertList(this.fileChangeEventList, "/var/tmp/4.cpp", "/var/tmp/5.cpp", "/var/tmp/6.cpp");
        
        /*
         * Add "/var/tmp/7.cpp" into to the list twice, the ListConflictVoter must make sure the
         * directory "/var/tmp" can contain at most 3 file change events so that "/var/tmp/4.cpp" and "/var/tmp/5.cpp" are
         * voted to be the conflict elements and they are removed automatically 
         */
        this.fileChangeEventList.addAll(MACollections.wrap("/var/tmp/7.cpp", "/var/tmp/7.cpp"));
        assertList(this.fileChangeEventList, "/var/tmp/6.cpp", "/var/tmp/7.cpp", "/var/tmp/7.cpp");
        
        /*
         * Add "/var/tmp/A.cpp", "/var/tmp/B.cpp", "/var/tmp/C.cpp", "/var/tmp/D.cpp", "/var/tmp/E.cpp", "/var/tmp/F.cpp" and 
         * "/var/tmp/G.cpp" into to the list, the ListConflictVoter must make sure the
         * directory "/var/tmp" can contain at most 3 file change events so that "/var/tmp/6.cpp", "/var/tmp/7.cpp", 
         * "/var/tmp/A.cpp", "/var/tmp/B.cpp", "/var/tmp/C.cpp" and "/var/tmp/D.cpp"
         * voted to be the conflict elements and they are removed automatically 
         */
        this.fileChangeEventList.addAll(
                MACollections.wrap(
                    "/var/tmp/A.cpp", 
                    "/var/tmp/B.cpp",
                    "/var/tmp/C.cpp",
                    "/var/tmp/D.cpp",
                    "/var/tmp/E.cpp",
                    "/var/tmp/F.cpp",
                    "/var/tmp/G.cpp"
                )
        );
        assertList(this.fileChangeEventList, "/var/tmp/E.cpp", "/var/tmp/F.cpp", "/var/tmp/G.cpp");
    }
    
    @Test
    public void testSpecialDirectoryContainsAtMost4Files() {
        
        this.fileChangeEventList.add("/usr/include/1.h");
        this.fileChangeEventList.add("/usr/include/2.h");
        this.fileChangeEventList.add("/usr/include/3.h");
        this.fileChangeEventList.add("/usr/include/4.h");
        assertList(this.fileChangeEventList, "/usr/include/1.h", "/usr/include/2.h", "/usr/include/3.h", "/usr/include/4.h");
        
        /*
         * Add "/usr/include/5.h" into the list, the ListConflictVoter must make sure the
         * special directory "/usr/include" can contain at most 4 file change events so that "/usr/include/1.h" is
         * voted to be the conflict element and it is removed automatically 
         */
        this.fileChangeEventList.add("/usr/include/5.h");
        assertList(this.fileChangeEventList, "/usr/include/2.h", "/usr/include/3.h", "/usr/include/4.h", "/usr/include/5.h");
        
        /*
         * Add "/usr/include/6.h" and ""/usr/include/7.h"" into the list, the ListConflictVoter must make sure the
         * special directory "/usr/include" can contain at most 4 file change events so that "/usr/include/2.h" 
         * and "/usr/include/3.h" are voted to be the conflict elements and they are removed automatically 
         */
        this.fileChangeEventList.addAll(MACollections.wrap("/usr/include/6.h", "/usr/include/7.h"));
        assertList(this.fileChangeEventList, "/usr/include/4.h", "/usr/include/5.h", "/usr/include/6.h", "/usr/include/7.h");
        
        /*
         * Add "/usr/include/8.h" into to the list for three times, the ListConflictVoter must make sure the
         * special directory "/usr/include" can contain at most 4 file change events so that "/usr/include/4.h", 
         * "/usr/include/5.h" and "/usr/include/6.h" are voted to be the conflict elements and they are removed automatically 
         */
        this.fileChangeEventList.addAll(MACollections.wrap("/usr/include/8.h", "/usr/include/8.h", "/usr/include/8.h"));
        assertList(this.fileChangeEventList, "/usr/include/7.h", "/usr/include/8.h", "/usr/include/8.h", "/usr/include/8.h");
        
        /*
         * Add "/usr/include/A.h", "/usr/include/B.h", "/usr/include/C.h", "/usr/include/D.h", "/usr/include/E.h", 
         * "/usr/include/F.h" and "/usr/include/G.h" into to the list, the ListConflictVoter must make sure the
         * special directory "/usr/include" can contain at most 4 file change events so that 
         * "/usr/include/7.h", "/usr/include/8.h", "/usr/include/A.h", "/usr/include/B.h" and "/usr/include/C.h"
         * voted to be the conflict elements and they are removed automatically
         */
        this.fileChangeEventList.addAll(
                MACollections.wrap(
                    "/usr/include/A.h", 
                    "/usr/include/B.h",
                    "/usr/include/C.h",
                    "/usr/include/D.h",
                    "/usr/include/E.h",
                    "/usr/include/F.h",
                    "/usr/include/G.h"
                )
        );
        assertList(this.fileChangeEventList, "/usr/include/D.h", "/usr/include/E.h", "/usr/include/F.h", "/usr/include/G.h");
    }
    
    private static void assertList(List<String> list, String ... elements) {
        Assert.assertEquals(elements.length, list.size());
        int index = 0;
        for (String element : list) {
            Assert.assertEquals(elements[index++], element);
        }
    }
}
