package org.babyfishdemo.spring.entities;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;

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
 * import java.util.Collection;
 * import java.util.Set;
 * import javax.persistence.Basic;
 * import javax.persistence.Column;
 * import javax.persistence.Entity;
 * import javax.persistence.FetchType;
 * import javax.persistence.GeneratedValue;
 * import javax.persistence.GenerationType;
 * import javax.persistence.Id;
 * import javax.persistence.Lob;
 * import javax.persistence.OneToMany;
 * import javax.persistence.SequenceGenerator;
 * import javax.persistence.Table;
 * import javax.persistence.Version;
 * import org.babyfish.hibernate.model.loader.HibernateObjectModelScalarLoader;
 * import org.babyfish.model.ObjectModelFactory;
 * import org.babyfish.model.ObjectModelFactoryFactory;
 * import org.babyfish.model.metadata.AllowDisability;
 * import org.babyfish.model.metadata.Association;
 * import org.babyfish.model.metadata.Deferrable;
 * import org.babyfish.model.metadata.ObjectModelDeclaration;
 * import org.babyfish.model.metadata.ObjectModelMode;
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.model.spi.ObjectModelImplementor;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.Inverse;
 * import org.babyfish.persistence.model.metadata.OptimisticLock;
 * import org.hibernate.bytecode.internal.javassist.FieldHandled;
 * import org.hibernate.bytecode.internal.javassist.FieldHandler;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name="DEPARTMENT")
 * @SequenceGenerator(name="departmentSequence", sequenceName="DEPARTMENT_ID_SEQ", initialValue=1, allocationSize=1)
 * public class Department implements FieldHandled {
 * 
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 * 
 *     private static final ObjectModelFactory<{OM}> {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *     
 *     private {OM} {om} = {OM_FACTORY}.create(this);
 * 
 *     @Id
 *     @Column(name="DEPARTMENT_ID", nullable=false)
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="departmentSequence")
 *     public Long getId() {
 *         return {om}.getId();
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         {om}.setId(paramLong); 
 *     }
 *      
 *     @Version
 *     @Column(name="VERSION", nullable=false)
 *     public int getVersion() { 
 *         return {om}.getVersion(); 
 *     }
 * 
 *     public void setVersion(int paramInt) {
 *         {om}.setVersion(paramInt);
 *     }
 *     
 *     @Column(name="NAME", length=50, nullable=false, unique=true)
 *     public String getName() {
 *         return {om}.getName();
 *     }
 * 
 *     public void setName(String paramString) {
 *         {om}.setName(paramString); 
 *     }
 *      
 *     @Lob
 *     @Basic(fetch=FetchType.LAZY)
 *     @Column(name="IMAGE", nullable=false)
 *     public byte[] getImage() { 
 *         return {om}.getImage(); 
 *     }
 * 
 *     public void setImage(byte[] paramArrayOfByte) {
 *         {om}.setImage(paramArrayOfByte);
 *     }
 *     @OneToMany(mappedBy="department")
 *     public Set<Employee> getEmployees() {
 *         return {om}.getEmployees();
 *     }
 * 
 *     public void setEmployees(Set<Employee> paramSet) {
 *         Set localSet = {om}.getEmployees(); 
 *         localSet.clear(); 
 *         if (paramSet != null) {
 *             localSet.addAll(paramSet);
 *         }
 *     }
 * 
 *     @Override
 *     public FieldHandler getFieldHandler() {
 *         return (FieldHandler)((ObjectModelImplementor){om}).getScalarLoader();
 *     }
 * 
 *     @Override
 *     public void setFieldHandler(FieldHandler paramFieldHandler) {
 *         ObjectModelImplementor localObjectModelImplementor = (ObjectModelImplementor){om};
 *         localObjectModelImplementor.setScalarLoader(
 *             new HibernateObjectModelScalarLoader(localObjectModelImplementor, paramFieldHandler)
 *         );
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Department paramDepartment) {
 *         return paramDepartment.{om};
 *     }
 * 
 *     public String toString() {
 *         return {om}.toString();
 *     }
 * 
 *     @ObjectModelDeclaration(provider="jpa", mode=ObjectModelMode.REFERENCE)
 *     @AllowDisability
 *     private static abstract interface {OM} {
 *     
 *         @EntityId
 *         Long getId();
 *         void setId(Long paramLong);
 * 
 *         @OptimisticLock
 *         int getVersion();
 *         void setVersion(int paramInt);
 * 
 *         @Scalar
 *         String getName();
 *         void setName(String paramString);
 * 
 *         @Deferrable
 *         byte[] getImage();
 *         void setImage(byte[] paramArrayOfByte);
 * 
 *         @Association(opposite="departmentReference")
 *         @Inverse
 *         Set<Employee> getEmployees();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "DEPARTMENT")
@SequenceGenerator(
        name = "departmentSequence",
        sequenceName = "DEPARTMENT_ID_SEQ",
        initialValue = 1,
        allocationSize = 1
)
public class Department {

    @Id
    @Column(name = "DEPARTMENT_ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "departmentSequence")
    private Long id;
    
    @Version
    @Column(name = "VERSION", nullable = false)
    private int version;
    
    @Column(name = "NAME", length = 50, nullable = false, unique = true)
    private String name;
    
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "IMAGE", nullable = false)
    private byte[] image;
    
    @OneToMany(mappedBy = "department")
    private Set<Employee> employees;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }
}
