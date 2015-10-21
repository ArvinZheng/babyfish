package org.babyfishdemo.om4jpa.disablity;

import org.babyfish.lang.Arguments;
import org.babyfishdemo.om4jpa.disablity.entities.Author;

/*
 * The AuthorService does NOT contain business logic,
 * so the functionality of DAO is embedded here directly
 * and need not to create AuthorRepository
 * 
 * In real project, please create the AuthorRepository
 * even if AuthorService has NO business logic.
 */
/**
 * @author Tao Chen
 */
public class AuthorService {

    public long createAuthor(Author author) {
        Arguments.mustBeNull(
                "author.id", 
                Arguments.mustNotBeNull("author", author).getId()
        );
        try (TransactionScope ts = new TransactionScope()) {
            // In real project, please don't do this
            // You should create AuthorRepository and
            // NEVER invoke any JPA API here directly.
            return ts.complete(TransactionScope.getEntityManager().merge(author).getId());
        }
    }
}
