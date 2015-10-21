package org.babyfishdemo.om4jpa.instrument.m2kr_2;

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
 * package org.babyfishdemo.om4jpa.instrument.m2kr_2;
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
 * public class Key {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *         
 *     private {OM} {om};
 * 
 *     private Key() {
 *         this.{om} = {OM_FACTORY}.create(this);
 *     }
 * 
 *     public Key(String primaryCode) {
 *         this.{om} = {OM_FACTORY}.create(this);
 *         this.setPrimaryCode(primaryCode);
 *     }
 *     public Key(String primaryCode, String secondaryCode) {
 *         this.{om} = {OM_FACTORY}.create(this);
 *         this.setPrimaryCode(primaryCode);
 *         this.setSecondaryCode(secondaryCode);
 *     }
 *     
 *     @Column(name = "PRIMARY_CODE", length=10, nullable=false)
 *     public String getPrimaryCode() {
 *         return this.{om}.getPrimaryCode();
 *     }
 * 
 *     public void setPrimaryCode(String paramString) {
 *         this.{om}.setPrimaryCode(paramString);
 *     }
 *     
 *     @Column(name = "PRIMARY_CODE", length=20)
 *     public String getSecondaryCode() {
 *         return this.{om}.getSecondaryCode();
 *     }
 * 
 *     public void setSecondaryCode(String paramString) {
 *         this.{om}.setSecondaryCode(paramString);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Key paramKey) {
 *         return paramKey.{om};
 *     }
 * 
 *     public int hashCode() {
 *         return {OM_FACTORY}.getObjectModelMetadata().getOwnerEqualityComparator().hashCode(this);
 *     }
 * 
 *     public boolean equals(Object paramObject) {
 *         return {OM_FACTORY}.getObjectModelMetadata().getOwnerEqualityComparator().equals(this, paramObject);
 *     }
 * 
 *     public String toString() {
 *         return this.{om}.toString();
 *     }
 * 
 *     // [provider = "jpa"] is very important!
 *     // That is why it is ObjectModel4JPA, not ObjectModel4Java.
 *     @ObjectModelDeclaration(
 *         provider = "jpa", 
 *         mode=ObjectModelMode.EMBEDDABLE, 
 *         declaredPropertiesOrder = "primaryCode, secondaryCode"
 *     )
 *     @AllowDisability
 *     private interface {OM} {
 *     
 *         @Scalar
 *         String getPrimaryCode();
 *         void setPrimaryCode(String paramString);
 * 
 *         @Scalar
 *         String getSecondaryCode();
 *         void setSecondaryCode(String paramString);
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Embeddable
public class Key {

    @Column(name = "PRIMARY_CODE", length = 10, nullable = false)
    private String primaryCode;
    
    @Column(name = "PRIMARY_CODE", length = 20)
    private String secondaryCode;
    
    @SuppressWarnings("unused") // Only for hibernate
    private Key() {
        
    }

    public Key(String primaryCode) {
        this.primaryCode = primaryCode;
    }

    public Key(String primaryCode, String secondaryCode) {
        this.primaryCode = primaryCode;
        this.secondaryCode = secondaryCode;
    }

    public String getPrimaryCode() {
        return primaryCode;
    }

    public void setPrimaryCode(String primaryCode) {
        this.primaryCode = primaryCode;
    }

    public String getSecondaryCode() {
        return secondaryCode;
    }

    public void setSecondaryCode(String secondaryCode) {
        this.secondaryCode = secondaryCode;
    }
}
