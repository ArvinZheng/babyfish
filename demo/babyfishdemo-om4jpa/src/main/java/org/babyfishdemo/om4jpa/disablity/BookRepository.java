package org.babyfishdemo.om4jpa.disablity;

import org.babyfishdemo.om4jpa.disablity.entities.Book;
import org.babyfishdemo.om4jpa.disablity.entities.Book__;

/**
 * @author Tao Chen
 */
public class BookRepository {
    
    Book getBookById(
            long id, 
            /*
             * We will learn this parameter "queryPaths" in the demo project 
             * babyfishdemo-querypath, don't worry, we use it very simply in
             * this demo so you need NOT to learn it temporarily.
             */
            Book__ ... queryPaths) {
        return TransactionScope.getEntityManager().find(Book.class, id, queryPaths);
    }

    Book mergeBook(Book book) {
        return TransactionScope.getEntityManager().merge(book);
    }
}
