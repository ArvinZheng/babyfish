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
import org.babyfish.collection.conflict.ListConflictVoter;
import org.babyfish.collection.conflict.ListConflictVoterArgs;
import org.babyfish.collection.conflict.spi.AbstractListConflictVoterManager;
import org.babyfish.collection.spi.base.AbstractBaseElementsImpl;
import org.babyfish.collection.spi.base.BaseElements;
import org.babyfish.collection.spi.base.BaseListIterator;
import org.babyfish.validator.Validator;
import org.babyfish.validator.Validators;
import org.junit.Assert;
import org.junit.Test;
/**
 * @author Tao Chen
 */
public abstract class AbstractBaseElementsSerializableTest extends AbstractSerializableTest {
    
    private static final Field VALIDATOR_FIELD =
            fieldOf(AbstractBaseElementsImpl.class, "validator");
    
    private static final Field VOTER_MANAGER_FIELD = 
            fieldOf(AbstractBaseElementsImpl.class, "voterManager");
    
    private static final Field ARR_FIELD =
            fieldOf(classOf(Validators.class.getName() + "$CombinedImpl"), "arr");
    
    private static final Field VOTERS_FIELD =
            fieldOf(AbstractListConflictVoterManager.class, "voters");
    
    private static final Field VALIDATORS_NODE_VALIDATOR =
    		fieldOf(classOf(Validators.class.getName() + "$Node"), "validator");

    protected abstract BaseElements<String> createBaseElements(
            UnifiedComparator<String> unifiedComparator);
    
    @Test
    public void testSerializationWithEqualityComparator() throws IOException, ClassNotFoundException {
        BaseElements<String> baseElements = this.createBaseElements(
                UnifiedComparator.of(
                        new InsenstiveEqualityComparatorImpl()));
        BaseElements<String> deserializedBaseElements = serialzingClone(baseElements);
        Assert.assertTrue(baseElements != deserializedBaseElements);
        UnifiedComparator<? super String> unifiedComparator = baseElements.unifiedComparator();
        UnifiedComparator<? super String> deserializableUnifiedComparator = deserializedBaseElements.unifiedComparator();
        Assert.assertTrue(unifiedComparator != deserializableUnifiedComparator);
        Assert.assertTrue(unifiedComparator.equals(deserializableUnifiedComparator));
    }
    
    @Test
    public void testSerializationWithComparator() throws IOException, ClassNotFoundException {
        BaseElements<String> baseElements = this.createBaseElements(
                UnifiedComparator.of(
                        new InsenstiveComparatorImpl()));
        BaseElements<String> deserializedBaseElements = serialzingClone(baseElements);
        Assert.assertTrue(baseElements != deserializedBaseElements);
        UnifiedComparator<? super String> unifiedComparator = baseElements.unifiedComparator();
        UnifiedComparator<? super String> deserializableUnifiedComparator = deserializedBaseElements.unifiedComparator();
        Assert.assertTrue(unifiedComparator != deserializableUnifiedComparator);
        Assert.assertTrue(unifiedComparator.equals(deserializableUnifiedComparator));
    }
    
    @Test
    public void testSerializableWithCombinedValidator() throws IOException, ClassNotFoundException {
        BaseElements<String> baseElements = this.createBaseElements(null);
        baseElements.combineValidator(new NotNullValidator());
        baseElements.combineValidator(new NotEmptyValidator());
        BaseElements<String> deserializedBaseElements = serialzingClone(baseElements);
        Assert.assertTrue(baseElements != deserializedBaseElements);
        Validator<String> validator = getField(baseElements, VALIDATOR_FIELD);
        Validator<String> deserializedValidator = getField(deserializedBaseElements, VALIDATOR_FIELD);
        Assert.assertTrue(validator != deserializedValidator);
        Validator<String>[] arr = nodesToValidators((Object[])getField(validator, ARR_FIELD));
        Validator<String>[] deserializedArr = nodesToValidators((Object[])getField(deserializedValidator, ARR_FIELD));
        Assert.assertTrue(arr != deserializedArr);
        Assert.assertTrue(deserializedArr.length > 2 ? deserializedArr[2] == null : deserializedArr.length == 2);
        Assert.assertSame(NotNullValidator.class, deserializedArr[0].getClass());
        Assert.assertSame(NotEmptyValidator.class, deserializedArr[1].getClass());
    }
    
    @Test
    public void testSerializableWithCombinedVoter() throws IOException, ClassNotFoundException {
        BaseElements<String> baseElements = this.createBaseElements(null);
        baseElements.combineConflictVoter(new MockBeginCharConflitVoter());
        baseElements.combineConflictVoter(new MockEndCharConflitVoter());
        BaseElements<String> deserializedBaseElements = serialzingClone(baseElements);
        Assert.assertTrue(baseElements != deserializedBaseElements);
        AbstractListConflictVoterManager<String> voter = getField(baseElements, VOTER_MANAGER_FIELD);
        AbstractListConflictVoterManager<String> deserializedVoter = getField(deserializedBaseElements, VOTER_MANAGER_FIELD);
        Assert.assertTrue(voter != deserializedVoter);
        ListConflictVoter<String>[] arr = getField(voter, VOTERS_FIELD);
        ListConflictVoter<String>[] deserializedArr = getField(deserializedVoter, VOTERS_FIELD);
        Assert.assertTrue(arr != deserializedArr);
        Assert.assertEquals(2, deserializedArr.length);
        Assert.assertSame(MockBeginCharConflitVoter.class, deserializedArr[0].getClass());
        Assert.assertSame(MockEndCharConflitVoter.class, deserializedArr[1].getClass());
    }
    
    @Test
    public void testSerializableWithElements() throws IOException, ClassNotFoundException {
        for (int i = 0; i < 1000; i++) {
            this.testSerializableWithElements(i);
        }
    }
    
    private void testSerializableWithElements(int elementCount) throws IOException, ClassNotFoundException {
        String[] elements = new String[elementCount];
        BaseElements<String> baseElements = this.createBaseElements(null);
        for (int i = 0; i < elementCount; i++) {
            baseElements.add(
                    0, 
                    0, 
                    i, 
                    elements[i] = "element-[" + i + ']', 
                    null,
                    null);
        }
        BaseElements<String> deserializedBaseElements = serialzingClone(baseElements);
        Assert.assertTrue(baseElements != deserializedBaseElements);
        Assert.assertEquals(baseElements.allSize(), deserializedBaseElements.allSize());
        String[] deserialziedElements = new String[elementCount];
        String[] deserialziedDescendingElements = new String[elementCount];
        BaseListIterator<String> itr = deserializedBaseElements.listIterator(0, 0, 0, null);
        int index = 0;
        while (itr.hasNext()) {
            deserialziedElements[index++] = itr.next();
        }
        itr = deserializedBaseElements.listIterator(0, 0, elementCount, null);
        index = 0;
        while (itr.hasPrevious()) {
            deserialziedDescendingElements[index++] = itr.previous();
        }
        for (int i = elementCount - 1; i >= 0; i--) {
            Assert.assertNotSame(elements[i], deserialziedElements[i]);
            Assert.assertTrue(elements[i] != deserialziedElements[i]);
            Assert.assertEquals(elements[i], deserialziedElements[i]);
            Assert.assertTrue(elements[i] != deserialziedDescendingElements[elementCount - 1 - i]);
            Assert.assertEquals(elements[i], deserialziedDescendingElements[elementCount - 1 - i]);
        }
    }
    
    @SuppressWarnings("unchecked")
	public static <T> Validator<T>[] nodesToValidators(Object[] nodes) {
    	Validator<T>[] arr = new Validator[nodes.length];
    	for (int i = arr.length - 1; i >= 0; i--) {
    		arr[i] = (Validator<T>)getField(nodes[i], VALIDATORS_NODE_VALIDATOR);
    	}
    	return arr;
    }
    
    @SuppressWarnings("unchecked")
    protected static BaseElements<String> serialzingClone(BaseElements<String> baseElements) throws IOException, ClassNotFoundException {
        byte[] buffer;
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(baseElements);
            oout.flush();
            buffer = bout.toByteArray();
        }
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buffer);
            ObjectInputStream oin = new ObjectInputStream(bin)) {
            return (BaseElements<String>)oin.readObject();
        }
    }
    
    private static class MockBeginCharConflitVoter implements ListConflictVoter<String>, Serializable {

        private static final long serialVersionUID = 7418094514173183562L;

        @Override
        public void vote(ListConflictVoterArgs<String> args) {
            
        }

    }
    
    private static class MockEndCharConflitVoter implements ListConflictVoter<String>, Serializable {

        private static final long serialVersionUID = -4199753672991464724L;
        
        @Override
        public void vote(ListConflictVoterArgs<String> args) {
            
        }
    }
    
}
