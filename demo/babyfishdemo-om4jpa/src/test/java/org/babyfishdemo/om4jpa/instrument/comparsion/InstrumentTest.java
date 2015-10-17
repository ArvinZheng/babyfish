package org.babyfishdemo.om4jpa.instrument.comparsion;

import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.FrozenEqualityComparator;
import org.babyfish.collection.XSet;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.junit.Assert;
import org.junit.Test;

/*
 * This test class is similar with "org.babyfishdemo.om4jpa.instrument.s2r.InstrumentTest",
 * it's unnecessary to do the tests of that test class here again.
 * 
 * In this class, only the difference between this test class and that test class are shown.
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata departmentOMM = JPAMetadatas.of(Department.class);
    
    private JPAObjectModelMetadata employeeOMM = JPAMetadatas.of(Employee.class);
    
    @Test
    public void testEqualityComparator() {
        
        EqualityComparator<Employee> expectedEqualityComparator = 
                this.employeeOMM.getOwnerEqualityComparator("name");
        
        Assert.assertTrue(expectedEqualityComparator instanceof FrozenEqualityComparator<?>);
        
        /*
         * Metadata test
         */
        Assert.assertSame(
                expectedEqualityComparator, 
                
                this
                .departmentOMM
                .getAssociationProperty("employees")
                .getCollectionUnifiedComparator()
                .equalityComparator(true)
        );
        
        /*
         * Runtime test
         */
        Assert.assertSame(
                expectedEqualityComparator, 
                
                ((XSet<Employee>)new Department().getEmployees())
                .unifiedComparator()
                .equalityComparator(true)
        );
    }
}
