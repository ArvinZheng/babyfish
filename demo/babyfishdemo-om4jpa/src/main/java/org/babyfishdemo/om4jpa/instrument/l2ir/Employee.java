package org.babyfishdemo.om4jpa.instrument.l2ir;

import javax.persistence.Column;
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
 * package org.babyfishdemo.om4jpa.instrument.l2ir;
 * 
 * import javax.persistence.Column;
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
 * import org.babyfish.model.metadata.Scalar;
 * import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
 * import org.babyfish.persistence.instrument.JPAObjectModelInstrument;
 * import org.babyfish.persistence.model.metadata.EntityId;
 * import org.babyfish.persistence.model.metadata.IndexMapping;
 * import org.babyfish.reference.IndexedReference;
 * 
 * @JPAObjectModelInstrument
 * @Entity
 * @Table(name = "l2ir_EMPLOYEE")
 * @SequenceGenerator(
 *     name = "employeeSequence", 
 *     sequenceName = "l2ir_EMPLOYEE_ID_SEQ", 
 *     initialValue = 1, 
 *     allocationSize = 1
 * )
 * public class Employee {
 *     public static final boolean {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E} = true;
 *     
 *     private static final ObjectModelFactory {OM_FACTORY} = 
 *         ObjectModelFactoryFactory.factoryOf({OM}.class);
 *         
 *     private {OM} {om} = ({OM}){OM_FACTORY}.create(this);
 * 
 *     @Id
 *     @Column(name = "EMPLOYEE_ID")
 *     @GeneratedValue(strategy=GenerationType.SEQUENCE, generator = "employeeSequence")
 *     public Long getId() {
 *         return this.{om}.getId();
 *     }
 * 
 *     public void setId(Long paramLong) {
 *         this.{om}.setId(paramLong);
 *     }
 *     
 *     @Column(name = "NAME", nullable=false, length=50)
 *     public String getName() {
 *         return this.{om}.getName();
 *     }
 * 
 *     public void setName(String paramString) {
 *         this.{om}.setName(paramString);
 *     }
 *     
 *     @Column(name = "INDEX_IN_DEPARTMENT")
 *     public int getIndex() {
 *         return this.{om}.getDepartmentReference().getIndex();
 *     }
 * 
 *     public void setIndex(int paramInt) {
 *         this.{om}.getIndexReference().setIndex(paramInt); 
 *     }
 *     
 *     @ManyToOne(fetch=FetchType.LAZY)
 *     @JoinColumn(name = "DEPARTMENT_ID")
 *     public Department getDepartment() { 
 *         return this.{om}.getDepartmentReference().get(); 
 *     }
 * 
 *     public void setDepartment(Department paramDepartment) {
 *         this.{om}.getDepartmentReference().set(paramDepartment);
 *     }
 * 
 *     @StaticMethodToGetObjectModel
 *     static {OM} {om}(Employee paramEmployee) {
 *         return paramEmployee.{om};
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
 *         @Association(opposite = "employees")
 *         @IndexMapping("index")
 *         IndexedReference<Department> getDepartmentReference();
 *     }
 * }
 */
/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Entity
@Table(name = "l2ir_EMPLOYEE")
@SequenceGenerator(
    name = "employeeSequence",
    sequenceName = "l2ir_EMPLOYEE_ID_SEQ",
    initialValue = 1,
    allocationSize = 1
)
public class Employee {
    
    @Id
    @Column(name = "EMPLOYEE_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "employeeSequence")
    private Long id;
    
    @Column(name = "NAME", nullable = false, length = 50)
    private String name;
    
    @Column(name = "INDEX_IN_DEPARTMENT")
    private int index;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID")
    private Department department;

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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
}
