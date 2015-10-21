package org.babyfishdemo.om4jpa.instrument.s2r;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.reference.Reference;
import org.babyfishdemo.om4jpa.instrument.s2r.Department;
import org.babyfishdemo.om4jpa.instrument.s2r.Employee;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata departmentOMM = JPAMetadatas.of(Department.class);
    
    private JPAObjectModelMetadata employeeOMM = JPAMetadatas.of(Employee.class);
    
    @Test
    public void testUserDefinedFieldsHaveBeenRemoved() {
        /*
         * All the user defined fields will be removed,
         * [
         *    (1) In the original source code, user defined fields must be private
         *      so that it can ONLY be used by the current class directly(not by reflection).
         *    (2) If there are some field accessing/assignments in the class,
         *      They will be replace to the invocations of getter/setter method.
         * ]
         */
        Set<String> expectedFields = new HashSet<>(
                MACollections.wrap(
                        "static {INSTRUMENTED_92B8C17E_BF4E_4135_B596_5A76E0FEBF4E}",
                        "static {OM_FACTORY}", 
                        "{om}"
                )
        );
        for (Class<?> clazz : new Class[] { Department.class, Employee.class }) {
            Set<String> fields = new HashSet<>();
            for (Field field : clazz.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    fields.add("static " + field.getName());
                } else {
                    fields.add(field.getName());
                }
            }
            Assert.assertEquals(expectedFields, fields);
        }
    }
    
    @Test
    public void testDepartmentObjectModelMetadata() {
        
        Assert.assertEquals(
                Department.class.getName() + "${OM}", 
                this.departmentOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.departmentOMM.getStaticMethodName()
        );
        
        Assert.assertEquals(3, this.departmentOMM.getDeclaredProperties().size());
        JPAScalarProperty idProperty = this.departmentOMM.getDeclaredScalarProperty("id");
        JPAScalarProperty nameProperty = this.departmentOMM.getDeclaredScalarProperty("name");
        JPAAssociationProperty employeesProperty = 
                this.departmentOMM.getDeclaredAssociationProperty("employees");
        
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(this.departmentOMM.getEntityIdProperty(), idProperty);
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        Assert.assertSame(String.class, nameProperty.getReturnClass());
        
        Assert.assertSame(this.employeeOMM, employeesProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(this.employeeOMM, employeesProperty.getReturnObjectModelMetadata());
        Assert.assertSame(
                this.employeeOMM.getDeclaredAssociationProperty("departmentReference"), 
                employeesProperty.getOppositeProperty()
        );
        Assert.assertTrue(employeesProperty.isInverse());
        Assert.assertTrue(employeesProperty.isCollection());
        Assert.assertSame(AssociatedEndpointType.SET, employeesProperty.getAssociatedEndpointType());
        Assert.assertSame(Set.class, employeesProperty.getStandardReturnClass());
        Assert.assertSame(Employee.class, employeesProperty.getElementClass());
        Assert.assertSame(this.employeeOMM, employeesProperty.getReturnObjectModelMetadata());
    }
    
    @Test
    public void testEmployeeObjectModelMetadata() {
        Assert.assertEquals(
                Employee.class.getName() + "${OM}", 
                this.employeeOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.employeeOMM.getStaticMethodName()
        );
        /*
         * The name of JPA property is "department", 
         * but the name of ObjectModel property is "departmentReference"
         */
        Assert.assertEquals(
                "departmentReference", 
                this
                .employeeOMM
                .getDeclaredMappingSources()
                .get("department")
                .getName()
        );
        Assert.assertEquals(
                "department", 
                this
                .employeeOMM
                .getDeclaredAssociationProperty("departmentReference")
                .getOwnerProperty()
                .getName()
        );
        
        Assert.assertEquals(3, this.employeeOMM.getDeclaredProperties().size());
        JPAScalarProperty idProperty = this.employeeOMM.getDeclaredScalarProperty("id");
        JPAScalarProperty nameProperty = this.employeeOMM.getDeclaredScalarProperty("name");
        JPAAssociationProperty departmentReferenceProperty = 
                this.employeeOMM.getDeclaredAssociationProperty("departmentReference");
        
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(this.employeeOMM.getEntityIdProperty(), idProperty);
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        Assert.assertSame(String.class, nameProperty.getReturnClass());
        
        Assert.assertSame(
                this.departmentOMM, 
                departmentReferenceProperty.getOppositeProperty().getDeclaringObjectModelMetadata()
        );
        Assert.assertSame(
                this.departmentOMM, 
                departmentReferenceProperty.getReturnObjectModelMetadata()
        );
        Assert.assertSame(
                this.departmentOMM.getDeclaredAssociationProperty("employees"), 
                departmentReferenceProperty.getOppositeProperty()
        );
        Assert.assertFalse(departmentReferenceProperty.isInverse());
        Assert.assertTrue(departmentReferenceProperty.isReference());
        Assert.assertSame(AssociatedEndpointType.REFERENCE, departmentReferenceProperty.getAssociatedEndpointType());
        Assert.assertSame(Reference.class, departmentReferenceProperty.getStandardReturnClass());
        Assert.assertSame(Department.class, departmentReferenceProperty.getElementClass());
        Assert.assertSame(this.departmentOMM, departmentReferenceProperty.getReturnObjectModelMetadata());
    }
}
