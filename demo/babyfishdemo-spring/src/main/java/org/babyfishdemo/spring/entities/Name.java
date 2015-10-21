package org.babyfishdemo.spring.entities;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
 *
 * package org.babyfishdemo.spring.entities;
 * 
 * import javax.persistence.Column;
 * import javax.persistence.Embeddable;
 * import org.babyfish.collection.EqualityComparator;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMetadata;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * 
 * @JPAObjectModelInstrument
 * @Embeddable
 * public class Name {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory<{OM}> {OM_FACTORY} = ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om};
 * 
 *     public Name() {
 *         {om} = {OM_FACTORY}.create(this);
 *     }
 *     
 *     public Name(String firstName, String lastName) { 
 *         {om} = {OM_FACTORY}.create(this);
 *         setFirstName(firstName);
 *         setLastName(lastName); 
 *     }
 * 
 *     @Column(name="FIRST_NAME", length=20, nullable=false)
 *     public String getFirstName() {
 *         return {om}.getFirstName();
 *     }
 * 
 *     public void setFirstName(String paramString) {
 *         {om}.setFirstName(paramString);
 *     }
 *     
 *     @Column(name="LAST_NAME", length=20)
 *     public String getLastName() {
 *         return {om}.getLastName();
 *     }
 * 
 *     public void setLastName(String paramString) {
 *         {om}.setLastName(paramString);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Name paramName) {
 *         return paramName.{om};
 *     }
 * 
 *     @Override
 *     public int hashCode() {
 *         return {OM_FACTORY}.getObjectModelMetadata().getOwnerEqualityComparator().hashCode(this);
 *     }
 * 
 *     @Override
 *     public boolean equals(Object paramObject) {
 *         return {OM_FACTORY}.getObjectModelMetadata().getOwnerEqualityComparator().equals(this, paramObject);
 *     }
 * 
 *     @Override
 *     public String toString() {
 *         return {om}.toString();
 *     }
 * 
 *     @ObjectModelDeclaration(
 *         provider="jpa", 
 *         mode=ObjectModelMode.EMBEDDABLE, 
 *         declaredPropertiesOrder="firstName, lastName"
 *     )
 *     @AllowDisability
 *     private static abstract interface {OM} {
 *     
 *         @Scalar
 *         String getFirstName();
 *         void setFirstName(String paramString);
 * 
 *         @Scalar
 *         String getLastName();
 *         void setLastName(String paramString);
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Embeddable
public class Name {

    @Column(name = "FIRST_NAME", length = 20, nullable = false)
    private String firstName;
    
    @Column(name = "LAST_NAME", length = 20)
    private String lastName; //lastName can be null, for protoss units
    
    public Name() {}
    
    public Name(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
