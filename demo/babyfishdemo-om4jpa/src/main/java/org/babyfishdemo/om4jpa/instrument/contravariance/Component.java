package org.babyfishdemo.om4jpa.instrument.contravariance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
 * package org.babyfishdemo.om4jpa.instrument.contravariance;
 * 
 * import javax.persistence.Column;
 * import javax.persistence.DiscriminatorColumn;
 * import javax.persistence.DiscriminatorType;
 * import javax.persistence.Entity;
 * import javax.persistence.FetchType;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.JoinColumn;
 * import javax.persistence.ManyToOne;
 * import javax.persistence.SequenceGenerator;
 * import javax.persistence.Table;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.Association;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.IndexMapping;
 * import org.babyfish.reference.IndexedReference;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name = "COMPONENT")
 * @SequenceGenerator(
 *     name = "componentSequence", 
 *     sequenceName = "COMPONENT_ID_SEQ", 
 *     initialValue=1, 
 *     allocationSize=1
 * )
 * @DiscriminatorColumn(name = "TYPE", discriminatorType=DiscriminatorType.INTEGER)
 * public abstract class Component {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om} = ({OM}){OM_FACTORY}.create(this);
 * 
 *     @Id
 *     @Column(name = "COMPONENT_ID", nullable=false)
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "componentSequence")
 *     public Long getId() {
 *         return {om}.getId();
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         {om}.setId(paramLong); 
 *     }
 *     
 *     @ManyToOne(fetch = FetchType.LAZY)
 *     @JoinColumn(name = "PARENT_ID")
 *     public Container getParent() { 
 *         return {om}.getParentReference().get(); 
 *     }
 * 
 *     public void setParent(Container paramContainer) {
 *         {om}.getParentReference().set(paramContainer);
 *     }
 *     
 *     @Column(name = "INDEX_IN_CONTAINER")
 *     public int getIndex() {
 *         return {om}.getParentReference().getIndex();
 *     }
 * 
 *     public void setIndex(int paramInt) {
 *         {om}.getIndexReference().setIndex(paramInt);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Component paramComponent) {
 *         return paramComponent.{om};
 *     }
 * 
 *     public String toString() {
 *         return {om}.toString();
 *     }
 * 
 *     @ObjectModelDeclaration(provider = "jpa", mode=ObjectModelMode.REFERENCE)
 *     @AllowDisability
 *     private interface {OM} {
 *     
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @Association(opposite = "components")
 *         @IndexMapping("index")
 *         IndexedReference<Container> getParentReference();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "COMPONENT")
@SequenceGenerator(
        name = "componentSequence",
        sequenceName = "COMPONENT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.INTEGER)
public abstract class Component {
    
    @Id
    @Column(name = "COMPONENT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "componentSequence")
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_ID")
    private Container parent;
    
    @Column(name = "INDEX_IN_CONTAINER")
    private int index;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Container getParent() {
        return parent;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
