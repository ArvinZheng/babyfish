package org.babyfishdemo.om4jpa.disablity.entities;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
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
 * import java.math.BigDecimal;
 * import java.util.Collection;
 * import java.util.Set;
 * import javax.persistence.Column;
 * import javax.persistence.Entity;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.JoinTable;
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
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name = "BOOK")
 * @SequenceGenerator(
 *     name = "bookSequence", 
 *     sequenceName = "BOOK_ID_SEQ", 
 *     initialValue = 1, 
 *     allocationSize = 1
 * )
 * public class Book {
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om};
 * 
 *     public Book() {
 *         this.{om} = (({OM}){OM_FACTORY}.create(this));
 *     }
 * 
 *     public Book(
 *             String isbn, 
 *             String name, 
 *             BigDecimal price, 
 *             String pulisher, 
 *             Collection<Author> authors) {
 *         this.{om} = (({OM}){OM_FACTORY}.create(this));
 *         this.setIsbn(isbn);
 *         this.setName(name);
 *         this.setPrice(price);
 *         this.setPublisher(pulisher);
 *         for (Author author : authors) {
 *             this.getAuthors().add(author);
 *         }
 *     }
 *     
 *     @Id
 *     @Column(name = "BOOK_ID", nullable=false)
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "bookSequence")
 *     public Long getId() {
 *         return this.{om}.getId();
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         this.{om}.setId(paramLong);
 *     }
 *     
 *     @Column(name = "ISBN", length=20, nullable=false, unique=true)
 *     public String getIsbn() {
 *         return this.{om}.getIsbn();
 *     }
 * 
 *     public void setIsbn(String paramString) {
 *         this.{om}.setIsbn(paramString);
 *     }
 *     
 *     @Column(name = "NAME", length=100, nullable=false)
 *     public String getName() {
 *         return this.{om}.getName();
 *     }
 * 
 *     public void setName(String paramString) {
 *         this.{om}.setName(paramString);
 *     }
 *     
 *     @Column(name = "PRICE", nullable=false)
 *     public BigDecimal getPrice() {
 *         return this.{om}.getPrice();
 *     }
 * 
 *     public void setPrice(BigDecimal paramBigDecimal) {
 *         this.{om}.setPrice(paramBigDecimal);
 *     }
 *     
 *     @Column(name = "PUBLISHER", length=100)
 *     public String getPublisher() {
 *         return this.{om}.getPublisher();
 *     }
 * 
 *     public void setPublisher(String paramString) {
 *         this.{om}.setPublisher(paramString); 
 *     }
 *      
 *     @ManyToMany
 *     @JoinTable(name = "BOOK_AUTHOR_MAPPING", joinColumns={@javax.persistence.JoinColumn(name = "BOOK_ID")}, inverseJoinColumns={@javax.persistence.JoinColumn(name = "AUTHOR_ID")})
 *     public Set<Author> getAuthors() { 
 *         return this.{om}.getAuthors(); 
 *     }
 * 
 *     public void setAuthors(Set<Author> paramSet) {
 *         Set localSet = this.{om}.getAuthors(); 
 *         localSet.clear(); 
 *         if (paramSet != null) { 
 *             localSet.addAll(paramSet);
 *         }
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Book paramBook) {
 *         return paramBook.{om};
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
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @Scalar
 *         String getIsbn();
 *         void setIsbn(String paramString);
 * 
 *         @Scalar
 *         String getName();
 *         void setName(String paramString);
 * 
 *         @Scalar
 *         BigDecimal getPrice();
 *         void setPrice(BigDecimal paramBigDecimal);
 * 
 *         @Scalar
 *         String getPublisher();
 *         void setPublisher(String paramString);
 * 
 *         @Association(opposite = "books")
 *         Set<Author> getAuthors();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "BOOK")
@SequenceGenerator(
        name = "bookSequence",
        sequenceName = "BOOK_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Book {

    @Id
    @Column(name = "BOOK_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bookSequence")
    private Long id;
    
    @Column(name = "ISBN", length =20, nullable = false, unique = true)
    private String isbn;
    
    @Column(name = "NAME", length = 100, nullable = false)
    private String name;
    
    @Column(name = "PRICE", nullable = false)
    private BigDecimal price;
    
    @Column(name = "PUBLISHER", length = 100)
    private String publisher;
    
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "BOOK_AUTHOR_MAPPING",
            joinColumns = @JoinColumn(name = "BOOK_ID"),
            inverseJoinColumns = @JoinColumn(name = "AUTHOR_ID")
    )
    private Set<Author> authors;
    
    public Book() {
        
    }
    
    public Book(String isbn, String name, BigDecimal price, String pulisher, Collection<Author> authors) {
        this.isbn = isbn;
        this.name = name;
        this.price = price;
        this.publisher = pulisher;
        for (Author author : authors) {
            this.authors.add(author);
        }
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Set<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<Author> authors) {
        this.authors = authors;
    }
}
