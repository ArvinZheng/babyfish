package org.babyfishdemo.om4jpa.disablity;

import java.math.BigDecimal;

import org.babyfish.persistence.model.JPAEntities;
import org.babyfishdemo.om4jpa.disablity.entities.Author;
import org.babyfishdemo.om4jpa.disablity.entities.Book;
import org.babyfishdemo.om4jpa.disablity.entities.Book_;
import org.babyfishdemo.om4jpa.disablity.entities.Book__;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class BookServiceTest {

    private BookService bookService = new BookService();
    
    private AuthorService authorService = new AuthorService();
    
    @Before
    public void prepareData() {
        Assert.assertEquals(
                1L, 
                this.authorService.createAuthor(new Author("Philip A. Bernstein"))
        );
        Assert.assertEquals(
                2L, 
                this.authorService.createAuthor(new Author("Eric Newcomer"))
        );
        Assert.assertEquals(
                3L, 
                this.authorService.createAuthor(new Author("Anand Rajaraman"))
        );
        Assert.assertEquals(
                1L,
                this.bookService.createBook(
                        new Book(
                                "978-7-302-24041-9",
                                "Principles of Transaction Processing, Second Edition",
                                new BigDecimal(48),
                                "Tsinghua University Press",
                                JPAEntities.createFakeEntities(Author.class, 1L, 2L)
                        )
                )
        );
    }
    
    @After
    public void uninstallData() {
        TransactionScope.recreateDatabase();
    }
    
    @Test
    public void testModifyPrimaryBookInfoSuccessed() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Enable property "isbn" implicitly
        updateTo.setIsbn("978-7-302-24042-9");
        
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, Third Edition");
        
        // Enable property "price" implicitly
        updateTo.setPrice(new BigDecimal(50));
        
        /*
         * The properties "id", "isbn", "name" and "price" are enabled,
         * other properties "publisher" and "authors" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifyPrimaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookAfterModified, 
                "978-7-302-24042-9", 
                "Principles of Transaction Processing, Third Edition", 
                new BigDecimal("50.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
    }
    
    @Test
    public void testModifyPrimaryBookInfoFailed() {
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        updateTo.setId(1L);
        
        // Enable property "publisher" implicitly
        updateTo.setPublisher("China Machine Press");
        
        try {
            /*
             * Invalid operation because BookService.modifyPrimaryBookInfo(Book)
             * ONLY allows you to update "isbn", "name" and "price"
             * so that you can not update the property "publisher"
             */
            this.bookService.modifyPrimaryBookInfo(updateTo);
            Assert.fail(IllegalArgumentException.class.getName() + " must be raised");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("publisher"));
        }
    }
    
    @Test
    public void testModifySecondaryBookInfoSuccessed() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Enable property "publisher" implicitly
        updateTo.setPublisher("China Machine Press");
    
        // Enable property "authors" implicitly
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 1L));
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 2L));
        updateTo.getAuthors().add(JPAEntities.createFakeEntity(Author.class, 3L));
        
        /*
         * The properties "id", "publisher" and "authors" are enabled,
         * other properties "isbn", "name" and "price" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifySecondaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookAfterModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "China Machine Press", 
                "Philip A. Bernstein",
                "Eric Newcomer",
                "Anand Rajaraman");
    }
    
    @Test
    public void testModifySecondaryBookInfoFailed() {
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        updateTo.setId(1L);
        
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, Third Edition");
        
        try {
            /*
             * Invalid operation because BookService.modifySecondaryBookInfo(Book)
             * ONLY allows you to update "publisher", and "authors"
             * so that you can not update the property "name"
             */
            this.bookService.modifySecondaryBookInfo(updateTo);
            Assert.fail(IllegalArgumentException.class.getName() + " must be raised");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("name"));
        }
    }
    
    @Test
    public void testModifyOnlyOneProperty() {
        Book bookBeforeModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        assertBook(
                bookBeforeModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, Second Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
        
        Book updateTo = new Book();
        
        /*
         * Disable all the properties except "id"
         */
        JPAEntities.disableAll(updateTo);
        
        updateTo.setId(1L);
        
        // Only modify one property "name"
        // Enable property "name" implicitly
        updateTo.setName("Principles of Transaction Processing, 2nd Edition");
        
        /*
         * All the properties except "id" and "name" are disabled
         */
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.id));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.isbn));
        Assert.assertTrue(JPAEntities.isEnabled(updateTo, Book_.name));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.price));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.publisher));
        Assert.assertTrue(JPAEntities.isDisabled(updateTo, Book_.authors));
        
        /*
         * BabyFish guarantee that the disabled properties will
         * NEVER be update by babyfish-jpa.
         */
        this.bookService.modifyPrimaryBookInfo(updateTo);
        
        Book bookAfterModified = this.bookService.getBookById(
                1L, 
                Book__.begin().authors().end() // Fetch the book.authors
        );
        /*
         * Only the name has been changed in database.
         */
        assertBook(
                bookAfterModified, 
                "978-7-302-24041-9", 
                "Principles of Transaction Processing, 2nd Edition", 
                new BigDecimal("48.00"), 
                "Tsinghua University Press", 
                "Philip A. Bernstein",
                "Eric Newcomer");
    }
    
    private static void assertBook(
            Book book, 
            String isbn, 
            String name, 
            BigDecimal price, 
            String publisher, 
            String ... authorNames) {
        Assert.assertEquals(isbn, book.getIsbn());
        Assert.assertEquals(name, book.getName());
        Assert.assertEquals(price, book.getPrice());
        Assert.assertEquals(publisher, book.getPublisher());
    }
}
