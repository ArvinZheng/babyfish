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
package org.babyfish.test.hibernate.model.hibernate;

import java.util.Set;

import junit.framework.Assert;

import org.babyfish.collection.MASet;
import org.babyfish.collection.UnifiedComparator;
import org.babyfish.hibernate.model.metadata.HibernateMetadatas;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Association;
import org.babyfish.model.metadata.ReferenceComparisonRule;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.persistence.model.metadata.EntityId;
import org.babyfish.persistence.model.metadata.Inverse;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class EqualityComparatorTest {
    
    @Test
    public void testDefaultEqualityComparator() {
        
        ObjectModelMetadata authorMetadata = HibernateMetadatas.of(Author.class);
        UnifiedComparator<?> booksUnifiedComparator = 
                authorMetadata.getAssociationProperty("books").getCollectionUnifiedComparator();
        Assert.assertEquals(
                Book.class.getName() + 
                "{defaultJPAFrozenEqualityComparator:92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}",
        booksUnifiedComparator.unwrap().getClass().getName());
    }
    
    @Test
    public void testCustomizeEqualityComparator() {
        
        ObjectModelMetadata bookMetadata = HibernateMetadatas.of(Book.class);
        UnifiedComparator<?> authorsUnifiedComparator = 
                bookMetadata.getAssociationProperty("authors").getCollectionUnifiedComparator();
        Assert.assertEquals(
                Author.class.getName() + 
                "{FrozenEqualityComparator=>" +
                bookMetadata.getProperty("name").getId() +
                ":92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}",
        authorsUnifiedComparator.unwrap().getClass().getName());
    }

    static class Book {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Book book) {
            return book.om;
        }
        
        public Long getId() {
            return this.om.getId();
        }
        
        protected void setId(Long id) {
            this.om.setId(id);
        }
        
        public String getName() {
            return this.om.getName();
        }
        
        public void setName(String name) {
            this.om.setName(name);
        }
        
        public Set<Author> getAuthors() {
            return this.om.getAuthors();
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        private interface OM {
            
            @EntityId
            Long getId();
            void setId(Long id);
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Association(opposite = "books")
            @Inverse
            MASet<Author> getAuthors();
        }
    }
    
    static class Author {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Author author) {
            return author.om;
        }
        
        public Long getId() {
            return this.om.getId();
        }
        
        protected void setId(Long id) {
            this.om.setId(id);
        }
        
        public String getName() {
            return this.om.getName();
        }
        
        public void setName(String name) {
            this.om.setName(name);
        }
        
        public Set<Book> getBooks() {
            return this.om.getBooks();
        }
        
        @ObjectModelDeclaration(provider = "jpa")
        @ReferenceComparisonRule("name")
        private interface OM {
            
            @EntityId
            Long getId();
            void setId(Long id);
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Association(opposite = "authors")
            MASet<Book> getBooks();
        }
    }
}
