package org.babyfishdemo.om4jpa.instrument.l2s;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfishdemo.om4jpa.instrument.l2s.Company;
import org.babyfishdemo.om4jpa.instrument.l2s.Investor;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata companyOMM = JPAMetadatas.of(Company.class);
    
    private JPAObjectModelMetadata investorOMM = JPAMetadatas.of(Investor.class);
    
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
        for (Class<?> clazz : new Class[] { Company.class, Investor.class }) {
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
    public void testCompanyObjectModelMetadata() {
        
        Assert.assertEquals(
                Company.class.getName() + "${OM}", 
                this.companyOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.companyOMM.getStaticMethodName()
        );
        
        Assert.assertEquals(3, this.companyOMM.getDeclaredProperties().size());
        JPAScalarProperty idProperty = this.companyOMM.getDeclaredScalarProperty("id");
        JPAScalarProperty nameProperty = this.companyOMM.getDeclaredScalarProperty("name");
        JPAAssociationProperty investorsProperty = 
                this.companyOMM.getDeclaredAssociationProperty("investors");
        
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(this.companyOMM.getEntityIdProperty(), idProperty);
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        Assert.assertSame(String.class, nameProperty.getReturnClass());
        
        Assert.assertSame(this.investorOMM, investorsProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(this.investorOMM, investorsProperty.getReturnObjectModelMetadata());
        Assert.assertSame(
                this.investorOMM.getDeclaredAssociationProperty("companys"), 
                investorsProperty.getOppositeProperty()
        );
        Assert.assertFalse(investorsProperty.isInverse());
        Assert.assertTrue(investorsProperty.isCollection());
        Assert.assertSame(AssociatedEndpointType.LIST, investorsProperty.getAssociatedEndpointType());
        Assert.assertSame(List.class, investorsProperty.getStandardReturnClass());
        Assert.assertSame(Investor.class, investorsProperty.getElementClass());
        Assert.assertSame(this.investorOMM, investorsProperty.getReturnObjectModelMetadata());
    }
    
    @Test
    public void testInvestorObjectModelMetadata() {
        Assert.assertEquals(
                Investor.class.getName() + "${OM}", 
                this.investorOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.investorOMM.getStaticMethodName()
        );
        
        Assert.assertEquals(3, this.investorOMM.getDeclaredProperties().size());
        JPAScalarProperty idProperty = this.investorOMM.getDeclaredScalarProperty("id");
        JPAScalarProperty nameProperty = this.investorOMM.getDeclaredScalarProperty("name");
        JPAAssociationProperty companysProperty = 
                this.investorOMM.getDeclaredAssociationProperty("companys");
        
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(this.investorOMM.getEntityIdProperty(), idProperty);
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        Assert.assertSame(String.class, nameProperty.getReturnClass());
        
        Assert.assertSame(
                this.companyOMM, 
                companysProperty.getOppositeProperty().getDeclaringObjectModelMetadata()
        );
        Assert.assertSame(
                this.companyOMM, 
                companysProperty.getReturnObjectModelMetadata()
        );
        Assert.assertSame(
                this.companyOMM.getDeclaredAssociationProperty("investors"), 
                companysProperty.getOppositeProperty()
        );
        Assert.assertTrue(companysProperty.isInverse());
        Assert.assertTrue(companysProperty.isCollection());
        Assert.assertSame(AssociatedEndpointType.SET, companysProperty.getAssociatedEndpointType());
        Assert.assertSame(Set.class, companysProperty.getStandardReturnClass());
        Assert.assertSame(Company.class, companysProperty.getElementClass());
        Assert.assertSame(this.companyOMM, companysProperty.getReturnObjectModelMetadata());
    }
}
