/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.collection.spi.base.serializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import org.babyfish.collection.UnifiedComparator;
import org.babyfish.collection.conflict.MAMapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoter;
import org.babyfish.collection.conflict.MapConflictVoterArgs;
import org.babyfish.collection.conflict.spi.AbstractMapConflictVoterManager;
import org.babyfish.collection.spi.base.AbstractBaseEntriesImpl;
import org.babyfish.collection.spi.base.BaseEntries;
import org.babyfish.collection.spi.base.BaseEntry;
import org.babyfish.collection.spi.base.BaseEntryIterator;
import org.babyfish.collection.spi.base.DescendingBaseEntries;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public abstract class AbstractBaseEntriesSerializableTest extends AbstractSerializableTest {
    
    private static final Field ROOT_ENTRIES_OR_ROOT_DATA = 
            fieldOf(AbstractBaseEntriesImpl.class, "rootEntriesOrRootData");
    
    private static final Class<?> ROOT_DATA =
            classOf(AbstractBaseEntriesImpl.class.getName() + "$RootData");
    
    private static final Field KEY_COMPARATOR_OR_EQUALITY_COMPARATOR = 
            fieldOf(ROOT_DATA, "keyComparatorOrEqualityComparator");
    
    private static final Field VALUE_COMPARATOR_OR_EQUALITY_COMPARATOR = 
            fieldOf(ROOT_DATA, "valueComparatorOrEqualityComparator");
    
    private static final Field KEY_VALIDATOR = fieldOf(ROOT_DATA, "keyValidator");
    
    private static final Field VALUE_VALIDATOR = fieldOf(ROOT_DATA, "valueValidator");
    
    private static final Field VOTER_MANAGER_FIELD = fieldOf(ROOT_DATA, "voterManager");
    
    private static final Field ARR_FIELD =
            fieldOf(classOf(Validators.class.getName() + "$CombinedImpl"), "arr");
    
    private static final Field VOTERS_FIELD =
            fieldOf(AbstractMapConflictVoterManager.class, "voters");
    
    private static final Field VALIDATORS_NODE_VALIDATOR = 
    		fieldOf(classOf(Validators.class.getName() + "$Node"), "validator");

    protected abstract BaseEntries<String, String> createBaseEntries(boolean withUnifiedComparator);
    
    @Test
    public void testSerializeableWithUnifiedComparator() throws IOException, ClassNotFoundException {
        BaseEntries<String, String> baseEntries = this.createBaseEntries(true);
        Assert.assertNotNull(UnifiedComparator.emptyToNull(baseEntries.keyUnifiedComparator()));
        Assert.assertNotNull(UnifiedComparator.emptyToNull(baseEntries.valueUnifiedComparator()));
        BaseEntries<String, String> deserializedBaseEntries = serialzingClone(baseEntries);
        Assert.assertTrue(baseEntries != deserializedBaseEntries);
        Object keyCOrUc = 
                getField(
                        getField(baseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                        KEY_COMPARATOR_OR_EQUALITY_COMPARATOR);
        Object deserializedKeyCOrUc = 
                getField(
                        getField(deserializedBaseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                        KEY_COMPARATOR_OR_EQUALITY_COMPARATOR);
        Assert.assertTrue(keyCOrUc != deserializedKeyCOrUc);
        Assert.assertEquals(keyCOrUc, deserializedKeyCOrUc);
        Object valueCOrUc = 
                getField(
                        getField(baseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                        VALUE_COMPARATOR_OR_EQUALITY_COMPARATOR);
        Object deserializedValueCOrUc = 
                getField(
                        getField(deserializedBaseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                        VALUE_COMPARATOR_OR_EQUALITY_COMPARATOR);
        Assert.assertTrue(valueCOrUc != deserializedValueCOrUc);
        Assert.assertEquals(valueCOrUc, deserializedValueCOrUc);
    }
    
    @Test
    public void testSerializableWithCombinedValidator() throws IOException, ClassNotFoundException {
        BaseEntries<String, String> baseEntries = this.createBaseEntries(false);
        baseEntries.combineKeyValidator(new NotNullValidator());
        baseEntries.combineKeyValidator(new NotEmptyValidator());
        baseEntries.combineValueValidator(new NotEmptyValidator());
        baseEntries.combineValueValidator(new NotNullValidator());
        BaseEntries<String, String> deserializedBaseEntries = serialzingClone(baseEntries);
        Assert.assertTrue(baseEntries != deserializedBaseEntries);
        Validator<?>[] keyArr = nodesToValidators(
	            (Object[])getField(
	                        getField(
	                                getField(baseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
	                                KEY_VALIDATOR
	                        ), 
	                ARR_FIELD
                )
        );
        Validator<?>[] deserializedKeyArr = nodesToValidators(
                (Object[])getField(
                        getField(
                                getField(deserializedBaseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                                KEY_VALIDATOR
                        ), 
                ARR_FIELD
            )
        );
        Assert.assertTrue(keyArr != deserializedKeyArr);
        Assert.assertTrue(deserializedKeyArr.length > 2 ? deserializedKeyArr[2] == null : deserializedKeyArr.length == 2);
        Assert.assertSame(NotNullValidator.class, deserializedKeyArr[0].getClass());
        Assert.assertSame(NotEmptyValidator.class, deserializedKeyArr[1].getClass());
        Validator<?>[] valueArr = nodesToValidators(
                (Object[])getField(
                        getField(
                                getField(baseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                                VALUE_VALIDATOR
                        ), 
                ARR_FIELD
            )
        );
        Validator<?>[] deserializedValueArr = nodesToValidators(
                (Object[])getField(
                        getField(
                                getField(deserializedBaseEntries, ROOT_ENTRIES_OR_ROOT_DATA), 
                                VALUE_VALIDATOR
                        ), 
                ARR_FIELD
            )
        );
        Assert.assertTrue(valueArr != deserializedValueArr);
        Assert.assertTrue(deserializedValueArr.length > 2 ? deserializedValueArr[2] == null : deserializedValueArr.length == 2);
        Assert.assertSame(NotEmptyValidator.class, deserializedValueArr[0].getClass());
        Assert.assertSame(NotNullValidator.class, deserializedValueArr[1].getClass());
    }
    
    @Test
    public void testSerializableWithCombinedVoter() throws IOException, ClassNotFoundException {
        BaseEntries<String, String> baseEntries = this.createBaseEntries(false);
        baseEntries.combineConflictVoter(new MockBeginCharConflitVoter());
        baseEntries.combineConflictVoter(new MockEndCharConflitVoter());
        BaseEntries<String, String> deserializedBaseEntries = serialzingClone(baseEntries);
        Assert.assertTrue(baseEntries != deserializedBaseEntries);
        MapConflictVoter<String, String>[] arr =
                getField(
                        getField(
                                getField(
                                        baseEntries, 
                                        ROOT_ENTRIES_OR_ROOT_DATA
                                ),
                                VOTER_MANAGER_FIELD
                        ),
                        VOTERS_FIELD
                );
        MapConflictVoter<String, String>[] deserializedArr =
                getField(
                        getField(
                                getField(
                                        deserializedBaseEntries, 
                                        ROOT_ENTRIES_OR_ROOT_DATA
                                ),
                                VOTER_MANAGER_FIELD
                        ),
                        VOTERS_FIELD
                );
        Assert.assertTrue(arr != deserializedArr);
        assertNonNullLen(2, deserializedArr);
        Assert.assertSame(MockBeginCharConflitVoter.class, deserializedArr[0].getClass());
        Assert.assertSame(MockEndCharConflitVoter.class, deserializedArr[1].getClass());
    }
    
    @Test
    public void testSerializableWithEntries() throws IOException, ClassNotFoundException {
        for (int i = 0; i < 1000; i++) {
            this.testSerializableWithEntries(i);
        }
    }
    
    private void testSerializableWithEntries(int entryCount) throws IOException, ClassNotFoundException {
        BaseEntries<String, String> baseEntries = this.createBaseEntries(false);
        String[] arr = new String[entryCount << 1];
        for (int i = 0; i < entryCount; i++) {
            String v = String.format("%4d", i);
            baseEntries.put(
                    arr[i << 1] = "key[" + v + ']', 
                    arr[(i << 1) + 1] = "value[" + v + ']', 
                    null);
        }
        BaseEntries<String, String> deserializedBaseEntries = serialzingClone(baseEntries);
        Assert.assertTrue(baseEntries != deserializedBaseEntries);
        Assert.assertEquals(baseEntries.size(), deserializedBaseEntries.size());
        BaseEntryIterator<String, String> itr = deserializedBaseEntries.iterator();
        int index = 0;
        if (deserializedBaseEntries instanceof DescendingBaseEntries<?, ?>) {
            while (itr.hasNext()) {
                BaseEntry<String, String> be = itr.next();
                String key = arr[index++];
                String value = arr[index++];
                Assert.assertTrue(key != be.getKey());
                Assert.assertEquals(key, be.getKey());
                Assert.assertTrue(value != be.getValue());
                Assert.assertEquals(value, be.getValue());
            }
        } else {
            boolean[] found = new boolean[entryCount];
            while (itr.hasNext()) {
                BaseEntry<String, String> be = itr.next();
                for (int i = 0; i < entryCount; i++) {
                    if (arr[i << 1].equals(be.getKey()) && arr[(i << 1) + 1].equals(be.getValue())) {
                        Assert.assertTrue(arr[(i << 1) + 1] != be.getValue());
                        found[i] = true;
                    }
                }
            }
            for (int i = entryCount - 1; i >= 0; i--) {
                if (!found[i]) {
                    Assert.fail();
                }
            }
        }
        this.compare(baseEntries, deserializedBaseEntries);
    }
    
    protected void compare(BaseEntries<String, String> baseEntries, BaseEntries<String, String> deserializedBaseEntries) {
        
    }
    
    @SuppressWarnings("unchecked")
    protected static BaseEntries<String, String> serialzingClone(
            BaseEntries<String, String> baseEntries) 
            throws IOException, ClassNotFoundException {
        byte[] buffer;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(baseEntries);
            oout.flush();
            buffer = bout.toByteArray();
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
            ObjectInputStream oin = new ObjectInputStream(bin)) {
            return (BaseEntries<String, String>)oin.readObject();
        }
    }
    
    private static <T> void assertNonNullLen(int len, T[] arr) {
        if (len == 0) {
            Assert.assertNull(arr);
        } else {
            Assert.assertTrue(len <= arr.length);
            for (int i = arr.length - 1; i >= 0; i--) {
                if (i < len) {
                    Assert.assertNotNull(arr[i]);
                } else {
                    Assert.assertNull(arr[i]);
                }
            }
        }
    }
    
    public static Validator<?>[] nodesToValidators(Object[] nodes) {
    	Validator<?>[] arr = new Validator[nodes.length];
    	for (int i = arr.length - 1; i >= 0; i--) {
    		arr[i] = (Validator<?>)getField(nodes[i], VALIDATORS_NODE_VALIDATOR);
    	}
    	return arr;
    }

    private static class MockBeginCharConflitVoter extends MAMapConflictVoter<String, String> implements Serializable {

        private static final long serialVersionUID = 5403677745627012406L;

        @Override
        protected void detach(String key, String value) {
            
        }

        @Override
        protected void attach(String key, String value) {
            
        }

        @Override
        public void vote(MapConflictVoterArgs<String, String> args) {
            
        }
    }
    
    private static class MockEndCharConflitVoter extends MAMapConflictVoter<String, String> implements Serializable {

        private static final long serialVersionUID = 6729534885925098835L;


        @Override
        protected void detach(String key, String value) {
            // TODO Auto-generated method stub
            
        }


        @Override
        protected void attach(String key, String value) {
            // TODO Auto-generated method stub
            
        }


        @Override
        public void vote(MapConflictVoterArgs<String, String> args) {
            // TODO Auto-generated method stub
            
        }
    }
}
