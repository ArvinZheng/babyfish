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
package org.babyfish.test.model.metadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.XNavigableSet;
import org.babyfish.collection.XOrderedSet;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class OwnerComparatorOrEqualityComparatorTest {
    
    //TODO
    @Ignore
    @Test
    public void testHashCode() {
        @SuppressWarnings("unchecked")
        EqualityComparator<Book> equalityComparator =
            (EqualityComparator<Book>)
            Metadatas
            .of(Book.class)
            .getAssociationProperty("analogousBooks")
            .getCollectionUnifiedComparator()
            .equalityComparator(true);
        Assert.assertNotNull(equalityComparator);
        
        Assert.assertEquals(
                (("0001".hashCode() * 31) + "969-9-678-54545-7".hashCode()) * 31 + "Thinking in C++".hashCode(), 
                equalityComparator.hashCode(new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertEquals(
                (("0001".hashCode() * 31) + 0) * 31 + "Thinking in C++".hashCode(),  
                equalityComparator.hashCode(new Book(3, null, "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertEquals(
                (("0001".hashCode() * 31) + "969-9-678-54545-7".hashCode()) * 31 + 0, 
                equalityComparator.hashCode(new Book(3, "0001", null, "969-9-678-54545-7")));
        Assert.assertEquals(
                (("0001".hashCode() * 31) + "969-9-678-54545-7".hashCode()) * 31 + "Thinking in C++".hashCode(), 
                equalityComparator.hashCode(new Book(3, "0001", "Thinking in C++", null)));
        Assert.assertEquals(
                0, 
                equalityComparator.hashCode(new Book(3, null, null, null)));
    }
    
    @Test
    public void testEquals() {
        @SuppressWarnings("unchecked")
        EqualityComparator<Book> equalityComparator =
                (EqualityComparator<Book>)
                Metadatas
                .of(Book.class)
                .getAssociationProperty("analogousBooks")
                .getCollectionUnifiedComparator()
                .equalityComparator(true);
        Assert.assertNotNull(equalityComparator);
        
        Assert.assertTrue(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(
                                3, 
                                new String("0001"), 
                                new String("Thinking in C++"), 
                                new String("969-9-678-54545-7"))));
        
        Assert.assertTrue(
                equalityComparator.equals(
                        new Book(3, null, null, null),
                        new Book(3, null, null, null)));
        
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0002", "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in Java", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-8")));
        
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, null, "Thinking in C++", "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", null, "969-9-678-54545-7")));
        Assert.assertFalse(
                equalityComparator.equals(
                        new Book(3, "0001", "Thinking in C++", "969-9-678-54545-7"),
                        new Book(3, "0001", "Thinking in C++", null)));
    }
    
    @Test
    public void testComparator() {
        @SuppressWarnings("unchecked")
        Comparator<Book> comparator =
        (Comparator<Book>)
        Metadatas
        .of(Book.class)
        .getAssociationProperty("sameSeriesBooks")
        .getCollectionUnifiedComparator()
        .comparator(true);
        Assert.assertNotNull(comparator);
        
        Assert.assertEquals(
                0, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("A"))));
        
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "B", "B"),
                        new Book(
                                3, 
                                new String("B"), 
                                new String("A"), 
                                new String("A"))));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", "B"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("B"), 
                                new String("A"))));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("B"))));
        
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "B", "A", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("B"), 
                                new String("B"))));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "B", "A"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("B"))));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", "B"),
                        new Book(
                                3, 
                                new String("A"), 
                                new String("A"), 
                                new String("A"))));
        
        Assert.assertEquals(
                0, 
                comparator.compare(
                        new Book(3, null, null, null),
                        new Book(3, null, null, null)));
        
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, null, "A", "A"),
                        new Book(3, "A", null, null)));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", null, "A"),
                        new Book(3, "A", "A", null)));
        Assert.assertEquals(
                -1, 
                comparator.compare(
                        new Book(3, "A", "A", null),
                        new Book(3, "A", "A", "A")));
        
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", null, null),
                        new Book(3, null, "A", "A")));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", null),
                        new Book(3, "A", null, "A")));
        Assert.assertEquals(
                +1, 
                comparator.compare(
                        new Book(3, "A", "A", "A"),
                        new Book(3, "A", "A", null)));
    }
    
    @Test
    public void testIO() throws ClassNotFoundException, IOException {
        byte[] buf;
        ObjectModelMetadata objectModelMetadata = Metadatas.of(Book.class);
        int codeId = objectModelMetadata.getScalarProperty("code").getId();
        int nameId = objectModelMetadata.getScalarProperty("name").getId();
        Object[] arr = new Object[] {
                objectModelMetadata.getOwnerEqualityComparator(),
                objectModelMetadata.getOwnerComparator("code"),
                objectModelMetadata.getOwnerEqualityComparator("code"),
                objectModelMetadata.getOwnerComparator(codeId),
                objectModelMetadata.getOwnerEqualityComparator(codeId),
                objectModelMetadata.getOwnerComparator("code", "name"),
                objectModelMetadata.getOwnerEqualityComparator("code", "name"),
                objectModelMetadata.getOwnerComparator(codeId, nameId),
                objectModelMetadata.getOwnerEqualityComparator(codeId, nameId),
        };
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ObjectOutputStream oout = new ObjectOutputStream(bout)) {
            oout.writeObject(arr);
            oout.flush();
            buf = bout.toByteArray();
        }
        Object[] deserializedArr;
        try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                ObjectInputStream oin = new ObjectInputStream(bin)) {
            deserializedArr = (Object[])oin.readObject();
        }
        Assert.assertTrue(arr != deserializedArr);
        Assert.assertEquals(arr.length, deserializedArr.length);
        for (int i = arr.length - 1; i >= 0; i--) {
            Assert.assertSame(arr[i], deserializedArr[i]);
        }
    }
    
    static class Book {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        public Book() {
            
        }
        
        public Book(long id, String code, String name, String isbn) {
            this.setId(id);
            this.setCode(code);
            this.setName(name);
            this.setIsbn(isbn);
        }
        
        public long getId() {
            return this.om.getId();
        }
        
        protected void setId(long id) {
            this.om.setId(id);
        }
        
        public String getCode() {
            return this.om.getCode();
        }
        
        public void setCode(String code) {
            this.om.setCode(code);
        }
        
        public String getName() {
            return this.om.getName();
        }
        
        public void setName(String name) {
            this.om.setName(name);
        }
        
        public String getIsbn() {
            return this.om.getIsbn();
        }
        
        public void setIsbn(String isbn) {
            this.om.setIsbn(isbn);
        }
        
        public Set<Book> getAnalogousBooks() {
            return om.getAnalogousBooks();
        }
        
        public XNavigableSet<Book> getSameSeriesBooks() {
            return om.getSameSeriesBooks();
        }

        @StaticMethodToGetObjectModel
        static OM om(Book book) {
            return book.om;
        }
        
        @ObjectModelDeclaration
        @ReferenceComparisonRule("code, name, isbn")
        private interface OM {
            
            @Scalar
            long getId();
            void setId(long id);
            
            @Scalar
            String getCode();
            void setCode(String code);
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Scalar
            String getIsbn();
            void setIsbn(String isbn);
            
            @Association(opposite = "analogousBooks")
            XOrderedSet<Book> getAnalogousBooks();
            
            @Association(opposite = "sameSeriesBooks")
            XNavigableSet<Book> getSameSeriesBooks();
        }
        
    }
        
}
