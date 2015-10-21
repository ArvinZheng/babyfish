package org.babyfishdemo.om4jpa.disablity.entities;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/*
 * This class is marked by the annotation 
 * @org.babyfish.persistence.instrument.JPAObjectModelInstrument
 * and the ant task "org.babyfish.hibernate.tool.InstrumentTask" is
 * declared in pom.xml, so the byte-code of this class will be 
 * instrumented.
 * 
 * You can use some java decompile tools such as jd-gui.exe to
 * decompile the jar file under the target directory if you 
 * have doubt or you want to research it.
 * 
 * The disadvantage of the byte-code instrument mechanism is, every 
 * time you change the source code of the entity classes, you must 
 * use maven to recompile them because the default compilation 
 * behavior of eclipse will NOT do this byte-code instrument.
 * 
 * Finally, after instrument, this actual code of this class should be
 * (
 *      In java language, java identifiers can not contains invalid characters 
 *      such as "{" and "}", but they can be accepted by JVM, so I generate 
 *      the identifiers with "{" and "}" so that they can not be conflicted by 
 *      the user defined identifiers absolutely.
 * ):
 * 
 * package org.babyfishdemo.om4jpa.disablity.entities;
 * 
 * import java.util.Collection;
 * import java.util.Set;
 * import javax.persistence.Column;
 * import javax.persistence.Entity;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.ManyToMany;
 * import javax.persistence.SequenceGenerator;
 * import javax.persistence.Table;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.Association;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.Inverse;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name = "AUTHOR")
 * @SequenceGenerator(
 *     name = "authorSequence", 
 *     sequenceName = "AUTHOR_ID_SEQ", 
 *     initialValue = 1, 
 *     allocationSize = 1
 * )
 * public class Author {
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *         
 *     private {OM} {om};
 * 
 *     public Author() {
 *         this.{om} = (({OM}){OM_FACTORY}.create(this));
 *     }
 *     
 *     public Author(String name) {
 *         this.{om} = (({OM}){OM_FACTORY}.create(this));
 *         this.setName(name); 
 *     }
 *     
 *     @Id
 *     @Column(name = "AUTHOR_ID")
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "authorSequence")
 *     public Long getId() { 
 *         return this.{om}.getId(); 
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         this.{om}.setId(paramLong);
 *     }
 *     
 *     @Column(name = "NAME", length=50, nullable=false)
 *     public String getName() {
 *         return this.{om}.getName();
 *     }
 * 
 *     public void setName(String paramString) {
 *         this.{om}.setName(paramString);
 *     }
 *     
 *     @ManyToMany(mappedBy = "authors")
 *     public Set<Book> getBooks() {
 *         return this.{om}.getBooks();
 *     }
 * 
 *     public void setBooks(Set<Book> paramSet) {
 *         Set localSet = this.{om}.getBooks(); 
 *         localSet.clear(); 
 *         if (paramSet != null) { 
 *             localSet.addAll(paramSet);
 *         }
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Author paramAuthor) {
 *         return paramAuthor.{om};
 *     }
 * 
 *     public String toString() {
 *         return this.{om}.toString();
 *     }
 * 
 *     // [provider = "jpa"] is very important!
 *     // That is why it is ObjectModel4JPA, not ObjectModel4Java.
 *     @ObjectModelDeclaration(provider = "jpa", mode=ObjectModelMode.REFERENCE)
 *     @AllowDisability
 *     private interface {OM} {
 *     
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @Scalar
 *         String getName();
 *         void setName(String paramString);
 * 
 *         @Association(opposite = "authors")
 *         @Inverse
 *         Set<Book> getBooks();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "AUTHOR")
@SequenceGenerator(
        name = "authorSequence",
        sequenceName = "AUTHOR_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Author {

    @Id
    @Column(name = "AUTHOR_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "authorSequence")
    private Long id;
    
    @Column(name = "NAME", length = 50, nullable = false)
    private String name;
    
    @ManyToMany(mappedBy = "authors")
    private Set<Book> books;
    
    public Author() {
    }
    
    public Author(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }
}
