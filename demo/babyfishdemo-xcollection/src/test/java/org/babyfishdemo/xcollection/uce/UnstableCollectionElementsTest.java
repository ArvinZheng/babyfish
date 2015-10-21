package org.babyfishdemo.xcollection.uce;

import java.util.Set;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.ReferenceElementMatcher;
import org.babyfish.collection.TreeMap;
import org.babyfish.collection.TreeSet;
import org.babyfish.collection.XMap;
import org.babyfish.collection.XSet;
import org.babyfishdemo.xcollection.mce.FullName;
import org.junit.Assert;
import org.junit.Test;
 
/**
 * @author Tao Chen
 */
public class UnstableCollectionElementsTest {
 
    @Test
    public void test() {
    
        /*
         * Prepare data
         */
        FullName jamesGosling = new FullName("james", "gosling");
        FullName joshBloch = new FullName("josh", "bloch");
        FullName gavinKing = new FullName("gavin", "king");
        
        XSet<FullName> firstNameLinkedHashSet = new LinkedHashSet<>(FullName.FIRST_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> firstNameLinkedHashMap = new LinkedHashMap<>(FullName.FIRST_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> firstNameTreeSet = new TreeSet<>(FullName.FIRST_NAME_COMPARATOR);
        XMap<FullName, Void> firstNameTreeMap = new TreeMap<>(FullName.FIRST_NAME_COMPARATOR);
            
        XSet<FullName> lastNameLinkedHashSet = new LinkedHashSet<>(FullName.LAST_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> lastNameLinkedHashMap = new LinkedHashMap<>(FullName.LAST_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> lastNameTreeSet = new TreeSet<>(FullName.LAST_NAME_COMPARATOR);
        XMap<FullName, Void> lastNameTreeMap = new TreeMap<>(FullName.LAST_NAME_COMPARATOR);
            
        XSet<FullName> fullNameLinkedHashSet = new LinkedHashSet<>(FullName.FULL_NAME_EQUALITY_COMPARATOR);
        XMap<FullName, Void> fullNameLinkedHashMap = new LinkedHashMap<>(FullName.FULL_NAME_EQUALITY_COMPARATOR);
        XSet<FullName> fullNameTreeSet = new TreeSet<>(FullName.FULL_NAME_COMPARATOR);
        XMap<FullName, Void> fullNameTreeMap = new TreeMap<>(FullName.FULL_NAME_COMPARATOR);
            
        firstNameLinkedHashSet.add(jamesGosling);
        firstNameLinkedHashSet.add(joshBloch);
        firstNameLinkedHashSet.add(gavinKing);
        firstNameLinkedHashMap.put(jamesGosling, null);
        firstNameLinkedHashMap.put(joshBloch, null);
        firstNameLinkedHashMap.put(gavinKing, null);
        firstNameTreeSet.add(jamesGosling);
        firstNameTreeSet.add(joshBloch);
        firstNameTreeSet.add(gavinKing);
        firstNameTreeMap.put(jamesGosling, null);
        firstNameTreeMap.put(joshBloch, null);
        firstNameTreeMap.put(gavinKing, null);
        
        lastNameLinkedHashSet.add(jamesGosling);
        lastNameLinkedHashSet.add(joshBloch);
        lastNameLinkedHashSet.add(gavinKing);
        lastNameLinkedHashMap.put(jamesGosling, null);
        lastNameLinkedHashMap.put(joshBloch, null);
        lastNameLinkedHashMap.put(gavinKing, null);
        lastNameTreeSet.add(jamesGosling);
        lastNameTreeSet.add(joshBloch);
        lastNameTreeSet.add(gavinKing);
        lastNameTreeMap.put(jamesGosling, null);
        lastNameTreeMap.put(joshBloch, null);
        lastNameTreeMap.put(gavinKing, null);
        
        fullNameLinkedHashSet.add(jamesGosling);
        fullNameLinkedHashSet.add(joshBloch);
        fullNameLinkedHashSet.add(gavinKing);
        fullNameLinkedHashMap.put(jamesGosling, null);
        fullNameLinkedHashMap.put(joshBloch, null);
        fullNameLinkedHashMap.put(gavinKing, null);
        fullNameTreeSet.add(jamesGosling);
        fullNameTreeSet.add(joshBloch);
        fullNameTreeSet.add(gavinKing);
        fullNameTreeMap.put(jamesGosling, null);
        fullNameTreeMap.put(joshBloch, null);
        fullNameTreeMap.put(gavinKing, null);
        
        {
	        /*
	         * Assert initialized values
	         */
	        assertNames(firstNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
	        assertNames(firstNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
	        assertNames(firstNameTreeSet, "gavin king", "james gosling", "josh bloch");
	        assertNames(firstNameTreeMap.keySet(), "gavin king", "james gosling", "josh bloch");
	        
	        assertNames(lastNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
	        assertNames(lastNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
	        assertNames(lastNameTreeSet, "josh bloch", "james gosling", "gavin king");
	        assertNames(lastNameTreeMap.keySet(), "josh bloch", "james gosling", "gavin king");
	        
	        assertNames(fullNameLinkedHashSet, "james gosling", "josh bloch", "gavin king");
	        assertNames(fullNameLinkedHashMap.keySet(), "james gosling", "josh bloch", "gavin king");
	        assertNames(fullNameTreeSet, "gavin king", "james gosling", "josh bloch");
	        assertNames(fullNameTreeMap.keySet(), "gavin king", "james gosling", "josh bloch");
        }
        
        {
	        /*
	         * (1) Change the first name of jamesGosling to be "JAMES", the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * The order of treeSet/treeMap based on firstName or fullName will be changed
	         */
	        jamesGosling.setFirstName("JAMES");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(firstNameTreeSet, "JAMES gosling", "gavin king", "josh bloch"); // Order changed
	        assertNames(firstNameTreeMap.keySet(), "JAMES gosling", "gavin king", "josh bloch"); // Order changed
	        
	        assertNames(lastNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(lastNameTreeSet, "josh bloch", "JAMES gosling", "gavin king");
	        assertNames(lastNameTreeMap.keySet(), "josh bloch", "JAMES gosling", "gavin king");
	        
	        assertNames(fullNameLinkedHashSet, "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "josh bloch", "gavin king");
	        assertNames(fullNameTreeSet, "JAMES gosling", "gavin king", "josh bloch"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "JAMES gosling", "gavin king", "josh bloch"); // Order changed
        }
        
        {
	        /*
	         * (2) Change the first name of joshBloch to be "JOSH", the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         *
	         * The order of treeSet/treeMap based on firstName or fullName will be changed
	         */
	        joshBloch.setFirstName("JOSH");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(firstNameTreeSet, "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
	        assertNames(firstNameTreeMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
	        
	        assertNames(lastNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(lastNameTreeSet, "JOSH bloch", "JAMES gosling", "gavin king");
	        assertNames(lastNameTreeMap.keySet(), "JOSH bloch", "JAMES gosling", "gavin king");
	        
	        assertNames(fullNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king");
	        assertNames(fullNameTreeSet, "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "JAMES gosling", "JOSH bloch", "gavin king"); // Order changed
        }
        
        {
	        /*
	         * (3) Change the first name of gavinKing to be "GAVIN", the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         *
	         * The order of treeSet/treeMap based on firstName or fullName will be changed
	         */
	        gavinKing.setFirstName("GAVIN");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(firstNameTreeSet, "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
	        assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
	        
	        assertNames(lastNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(lastNameTreeSet, "JOSH bloch", "JAMES gosling", "GAVIN king");
	        assertNames(lastNameTreeMap.keySet(), "JOSH bloch", "JAMES gosling", "GAVIN king");
	        
	        assertNames(fullNameLinkedHashSet, "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES gosling", "JOSH bloch", "GAVIN king");
	        assertNames(fullNameTreeSet, "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES gosling", "JOSH bloch"); // Order changed
        }
        
        {
	        /*
	         * (4) Change the last name of jamesGosling to be "GOSLING", the 
	         * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * The order of treeSet/treeMap based on lastName will be changed
	         */
	        jamesGosling.setLastName("GOSLING");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(firstNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH bloch");
	        assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH bloch");
	        
	        assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(lastNameTreeSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king"); // Order changed
	        assertNames(lastNameTreeMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king"); // Order changed
	        
	        assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH bloch", "GAVIN king");
	        assertNames(fullNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH bloch");
	        assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH bloch");
        }
        
        {
	        /*
	         * (5) Change the last name of joshBloch to be "BLOCH", the 
	         * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * The order of treeSet/treeMap based on lastName will be changed
	         */
	        joshBloch.setLastName("BLOCH");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(firstNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
	        assertNames(firstNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
	        
	        assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(lastNameTreeSet, "JOSH BLOCH", "JAMES GOSLING", "GAVIN king"); // Order changed
	        assertNames(lastNameTreeMap.keySet(), "JOSH BLOCH", "JAMES GOSLING", "GAVIN king"); // Order changed
	        
	        assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN king");
	        assertNames(fullNameTreeSet, "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
	        assertNames(fullNameTreeMap.keySet(), "GAVIN king", "JAMES GOSLING", "JOSH BLOCH");
        }
        
        {
	        /*
	         * (6) Change the last name of gavinKing to be "KING", the 
	         * lastNameHashSet, lastNameHashSet, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         */
	        gavinKing.setLastName("KING");
	        
	        assertNames(firstNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(firstNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(firstNameTreeSet, "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
	        assertNames(firstNameTreeMap.keySet(), "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
	        
	        assertNames(lastNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(lastNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(lastNameTreeSet, "JOSH BLOCH", "JAMES GOSLING", "GAVIN KING");
	        assertNames(lastNameTreeMap.keySet(), "JOSH BLOCH", "JAMES GOSLING", "GAVIN KING");
	        
	        assertNames(fullNameLinkedHashSet, "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(fullNameLinkedHashMap.keySet(), "JAMES GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(fullNameTreeSet, "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
	        assertNames(fullNameTreeMap.keySet(), "GAVIN KING", "JAMES GOSLING", "JOSH BLOCH");
        }
        
        {
	        /*
	         * (7) Change the first name of jamesGosling to be null, the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * The order of treeSet/treeMap based on firstName or fullName will be changed
	         */
	        jamesGosling.setFirstName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(firstNameLinkedHashMap.keySet(), "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(firstNameTreeSet, "null GOSLING", "GAVIN KING", "JOSH BLOCH"); // Order changed
	        assertNames(firstNameTreeMap.keySet(), "null GOSLING", "GAVIN KING", "JOSH BLOCH"); // Order changed
	        
	        assertNames(lastNameLinkedHashSet, "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(lastNameLinkedHashMap.keySet(), "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(lastNameTreeSet, "JOSH BLOCH", "null GOSLING", "GAVIN KING");
	        assertNames(lastNameTreeMap.keySet(), "JOSH BLOCH", "null GOSLING", "GAVIN KING");
	        
	        assertNames(fullNameLinkedHashSet, "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(fullNameLinkedHashMap.keySet(), "null GOSLING", "JOSH BLOCH", "GAVIN KING");
	        assertNames(fullNameTreeSet, "null GOSLING", "GAVIN KING", "JOSH BLOCH"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "null GOSLING", "GAVIN KING", "JOSH BLOCH"); // Order changed
        }
        
        {
	        /*
	         * (8) Change the first name of joshBloch to be null, the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically.
	         * 
	         * (1) For set/map base on firstName,
	         * The first name of jamesGosling already is null, now the first name of joshBloch become to be null too,
	         * so jamesGlosing will be removed automatically because the default ReplacementRule is NEW_REFERENCE_WIN.
	         * (If ReplacementRule of set/map is OLD_REFERENCE_WIN, joshBloch will be removed automatically)
	         * 
	         * (2) The order of treeSet/treeMap based on firstName or fullName will be changed.
	         */
	        joshBloch.setFirstName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null BLOCH", "GAVIN KING"); // Element Removed
	        assertNames(firstNameLinkedHashMap.keySet(), "null BLOCH", "GAVIN KING"); // Element Removed
	        assertNames(firstNameTreeSet, "null BLOCH", "GAVIN KING"); // // Element Removed & Order changed
	        assertNames(firstNameTreeMap.keySet(), "null BLOCH", "GAVIN KING"); // Element Removed & Order changed
	        
	        assertNames(lastNameLinkedHashSet, "null GOSLING", "null BLOCH", "GAVIN KING");
	        assertNames(lastNameLinkedHashMap.keySet(), "null GOSLING", "null BLOCH", "GAVIN KING");
	        assertNames(lastNameTreeSet, "null BLOCH", "null GOSLING", "GAVIN KING");
	        assertNames(lastNameTreeMap.keySet(), "null BLOCH", "null GOSLING", "GAVIN KING");
	        
	        assertNames(fullNameLinkedHashSet, "null GOSLING", "null BLOCH", "GAVIN KING");
	        assertNames(fullNameLinkedHashMap.keySet(), "null GOSLING", "null BLOCH", "GAVIN KING");
	        assertNames(fullNameTreeSet, "null BLOCH", "null GOSLING", "GAVIN KING"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "null BLOCH", "null GOSLING", "GAVIN KING"); // Order changed
        }
        
        {
	        /*
	         * (9) Change the first name of gavinKing to be null, the 
	         * firstNameHashSet, firstNameHashSet, firstNameTreeSet, firstNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * For set/map base on firstName,
	         * The first name of joshBloch already is null, now the first name of gavinKing become to be null too,
	         * so joshBloch will be removed automatically because the default ReplacementRule is NEW_REFERENCE_WIN.
	         * (If ReplacementRule of set/map is OLD_REFERENCE_WIN, gavinKing will be removed automatically).
	         */
	        gavinKing.setFirstName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null KING"); // Element Removed
	        assertNames(firstNameLinkedHashMap.keySet(), "null KING"); // Element Removed
	        assertNames(firstNameTreeSet, "null KING"); // Element Removed
	        assertNames(firstNameTreeMap.keySet(), "null KING"); // Element Removed
	        
	        assertNames(lastNameLinkedHashSet, "null GOSLING", "null BLOCH", "null KING");
	        assertNames(lastNameLinkedHashMap.keySet(), "null GOSLING", "null BLOCH", "null KING");
	        assertNames(lastNameTreeSet, "null BLOCH", "null GOSLING", "null KING");
	        assertNames(lastNameTreeMap.keySet(), "null BLOCH", "null GOSLING", "null KING");
	        
	        assertNames(fullNameLinkedHashSet, "null GOSLING", "null BLOCH", "null KING");
	        assertNames(fullNameLinkedHashMap.keySet(), "null GOSLING", "null BLOCH", "null KING");
	        assertNames(fullNameTreeSet, "null BLOCH", "null GOSLING", "null KING");
	        assertNames(fullNameTreeMap.keySet(), "null BLOCH", "null GOSLING", "null KING"); 
        }
        
        {
	        /*
	         * (10) Change the last name of jamesGosling to be null, the 
	         * lastNameHashSet, lastNameHashMap, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * The order of treeSet/treeMap based on lastName or fullName will be changed.
	         */
	        jamesGosling.setLastName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null KING");
	        assertNames(firstNameLinkedHashMap.keySet(), "null KING");
	        assertNames(firstNameTreeSet, "null KING");
	        assertNames(firstNameTreeMap.keySet(), "null KING");
	        
	        assertNames(lastNameLinkedHashSet, "null null", "null BLOCH", "null KING");
	        assertNames(lastNameLinkedHashMap.keySet(), "null null", "null BLOCH", "null KING");
	        assertNames(lastNameTreeSet, "null null", "null BLOCH", "null KING"); // Order changed
	        assertNames(lastNameTreeMap.keySet(), "null null", "null BLOCH", "null KING"); // Order changed
	        
	        assertNames(fullNameLinkedHashSet, "null null", "null BLOCH", "null KING");
	        assertNames(fullNameLinkedHashMap.keySet(), "null null", "null BLOCH", "null KING");
	        assertNames(fullNameTreeSet, "null null", "null BLOCH", "null KING"); // Order changed
	        assertNames(fullNameTreeMap.keySet(), "null null", "null BLOCH", "null KING"); // Order changed
        }
        
        {
	        /*
	         * (11) Change the last name of joshBloch to be null, the 
	         * lastNameHashSet, lastNameHashMap, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * For set/map base on lastName or fullName,
	         * The last name of jamesGosling already is null, now the last name of joshBloch become to be null too,
	         * so jamesGosling will be removed automatically because the default ReplacementRule is NEW_REFERENCE_WIN.
	         * (If ReplacementRule of set/map is OLD_REFERENCE_WIN, joshBloch will be removed automatically)
	         */
	        joshBloch.setLastName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null KING");
	        assertNames(firstNameLinkedHashMap.keySet(), "null KING");
	        assertNames(firstNameTreeSet, "null KING");
	        assertNames(firstNameTreeMap.keySet(), "null KING");
	        
	        assertNames(lastNameLinkedHashSet, "null null", "null KING"); // Element Removed
	        assertNames(lastNameLinkedHashMap.keySet(), "null null", "null KING"); // Element Removed
	        assertNames(lastNameTreeSet, "null null", "null KING"); // Element Removed
	        assertNames(lastNameTreeMap.keySet(), "null null", "null KING"); // Element Removed
	        
	        assertNames(fullNameLinkedHashSet, "null null", "null KING"); // Element Removed
	        assertNames(fullNameLinkedHashMap.keySet(), "null null", "null KING"); // Element Removed
	        assertNames(fullNameTreeSet, "null null", "null KING"); // Element Removed
	        assertNames(fullNameTreeMap.keySet(), "null null", "null KING"); // Element Removed
	        
	        /*
	         * Assert the "null null" is joshBloch
	         */
	        Assert.assertTrue(lastNameLinkedHashSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameLinkedHashMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameTreeSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameTreeMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameLinkedHashSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameLinkedHashMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameTreeSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameTreeMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        /*
	         * Assert the "null null" is not jamesGlosing
	         */
	        Assert.assertFalse(lastNameLinkedHashSet.contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameLinkedHashMap.keySet().contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameTreeSet.contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameTreeMap.keySet().contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameLinkedHashSet.contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameLinkedHashMap.keySet().contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameTreeSet.contains(jamesGosling, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameTreeMap.keySet().contains(jamesGosling, ReferenceElementMatcher.getInstance()));
        }
        
        {
	        /*
	         * (12) Change the last name of gavinKing to be null, the 
	         * lastNameHashSet, lastNameHashMap, lastNameTreeSet, lastNameTreeMap,
	         * fullNameHashSet, fullNameHashSet, fullNameTreeSet and fullNameTreeMap 
	         * will be adjusted automatically. 
	         * 
	         * For set/map base on lastName or fullName,
	         * The last name of joshBloch already is null, now the last name of gavinKing become to be null too,
	         * so joshBloch will be removed automatically because the default ReplacementRule is NEW_REFERENCE_WIN.
	         * (If ReplacementRule of set/map is OLD_REFERENCE_WIN, gavinKing will be removed automatically)
	         */
	        gavinKing.setLastName(null);
	        
	        assertNames(firstNameLinkedHashSet, "null null");
	        assertNames(firstNameLinkedHashMap.keySet(), "null null");
	        assertNames(firstNameTreeSet, "null null");
	        assertNames(firstNameTreeMap.keySet(), "null null");
	        
	        assertNames(lastNameLinkedHashSet, "null null"); // Element Removed
	        assertNames(lastNameLinkedHashMap.keySet(), "null null"); // Element Removed
	        assertNames(lastNameTreeSet, "null null"); // Element Removed
	        assertNames(lastNameTreeMap.keySet(), "null null"); // Element Removed
	        
	        assertNames(fullNameLinkedHashSet, "null null"); // Element Removed
	        assertNames(fullNameLinkedHashMap.keySet(), "null null"); // Element Removed
	        assertNames(fullNameTreeSet, "null null"); // Element Removed
	        assertNames(fullNameTreeMap.keySet(), "null null"); // Element Removed
	        
	        /*
	         * Assert the "null null" is gavingKing
	         */
	        Assert.assertTrue(lastNameLinkedHashSet.contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameLinkedHashMap.keySet().contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameTreeSet.contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(lastNameTreeMap.keySet().contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameLinkedHashSet.contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameLinkedHashMap.keySet().contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameTreeSet.contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        Assert.assertTrue(fullNameTreeMap.keySet().contains(gavinKing, ReferenceElementMatcher.getInstance()));
	        /*
	         * Assert the "null null" is not joshBloch
	         */
	        Assert.assertFalse(lastNameLinkedHashSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameLinkedHashMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameTreeSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(lastNameTreeMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameLinkedHashSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameLinkedHashMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameTreeSet.contains(joshBloch, ReferenceElementMatcher.getInstance()));
	        Assert.assertFalse(fullNameTreeMap.keySet().contains(joshBloch, ReferenceElementMatcher.getInstance()));
        }
    }
    
    private static void assertNames(Set<FullName> fullNameSetWithParticularOrder, String ... names) {
    	Assert.assertEquals(names.length, fullNameSetWithParticularOrder.size());
        int index = 0;
        for (FullName fullName : fullNameSetWithParticularOrder) {
            Assert.assertEquals(names[index++], fullName.getFirstName() + " " + fullName.getLastName());
        }
    }
}
