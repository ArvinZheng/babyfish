package org.babyfishdemo.om4jpa.instrument.contravariance;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import org.babyfish.association.AssociatedEndpointType;
import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.persistence.model.metadata.JPAAssociationProperty;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfish.reference.IndexedReference;
import org.babyfishdemo.om4jpa.instrument.contravariance.Button;
import org.babyfishdemo.om4jpa.instrument.contravariance.Component;
import org.babyfishdemo.om4jpa.instrument.contravariance.Container;
import org.babyfishdemo.om4jpa.instrument.contravariance.TabControl;
import org.babyfishdemo.om4jpa.instrument.contravariance.TabPage;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata componentOMM = JPAMetadatas.of(Component.class);
    
    private JPAObjectModelMetadata containerOMM = JPAMetadatas.of(Container.class);
    
    private JPAObjectModelMetadata buttonOMM = JPAMetadatas.of(Button.class);
    
    private JPAObjectModelMetadata tabControlOMM = JPAMetadatas.of(TabControl.class);
    
    private JPAObjectModelMetadata tabPageOMM = JPAMetadatas.of(TabPage.class);
    
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
        for (Class<?> clazz : new Class[] { 
                Component.class, 
                Container.class, 
                Button.class, 
                TabControl.class, 
                TabPage.class }) {
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
    public void testComponentObjectModelMetadata() {
        Assert.assertEquals(
                Component.class.getName() + "${OM}", 
                this.componentOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.componentOMM.getStaticMethodName()
        );
        
        /*
         * The name of JPA property is "parent", 
         * but the name of ObjectModel property is "parentReference"
         */
        Assert.assertEquals(
                "parentReference", 
                this
                .componentOMM
                .getDeclaredMappingSources()
                .get("parent")
                .getName()
        );
        Assert.assertEquals(
                "parent", 
                this
                .componentOMM
                .getDeclaredAssociationProperty("parentReference")
                .getOwnerProperty()
                .getName()
        );
        
        Assert.assertEquals(2, this.componentOMM.getDeclaredProperties().size());
        JPAScalarProperty idProperty = this.componentOMM.getDeclaredScalarProperty("id");
        JPAAssociationProperty parentReferenceProperty = 
                this.componentOMM.getDeclaredAssociationProperty("parentReference");
        
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(this.componentOMM.getEntityIdProperty(), idProperty);
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        Assert.assertSame(this.containerOMM, parentReferenceProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(
                this.containerOMM.getDeclaredAssociationProperty("components"), 
                parentReferenceProperty.getOppositeProperty()
        );
        Assert.assertFalse(parentReferenceProperty.isInverse());
        Assert.assertTrue(parentReferenceProperty.isReference());
        Assert.assertSame(AssociatedEndpointType.INDEXED_REFERENCE, parentReferenceProperty.getAssociatedEndpointType());
        Assert.assertSame(IndexedReference.class, parentReferenceProperty.getStandardReturnClass());
        Assert.assertSame(Container.class, parentReferenceProperty.getElementClass());
        Assert.assertSame(this.containerOMM, parentReferenceProperty.getReturnObjectModelMetadata());
    }
    
    @Test
    public void testContainerObjectModelMetadata() {
        Assert.assertEquals(
                Container.class.getName() + "${OM}", 
                this.containerOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.containerOMM.getStaticMethodName()
        );
        
        /*
         * Inheritance
         */
        Assert.assertSame(this.componentOMM, this.containerOMM.getSuperMetadata());
        
        Assert.assertEquals(1, this.containerOMM.getDeclaredProperties().size());
        JPAAssociationProperty componentsProperty = this.containerOMM.getDeclaredAssociationProperty("components");
        
        Assert.assertSame(this.componentOMM, componentsProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(
                this.componentOMM.getDeclaredAssociationProperty("parentReference"), 
                componentsProperty.getOppositeProperty()
        );
        Assert.assertTrue(componentsProperty.isInverse());
        Assert.assertTrue(componentsProperty.isCollection());
        Assert.assertSame(AssociatedEndpointType.LIST, componentsProperty.getAssociatedEndpointType());
        Assert.assertSame(List.class, componentsProperty.getStandardReturnClass());
        Assert.assertSame(Component.class, componentsProperty.getElementClass());
        Assert.assertSame(this.componentOMM, componentsProperty.getReturnObjectModelMetadata());
    }
    
    @Test
    public void testButtonObjectModelMetadata() {
        Assert.assertEquals(
                Button.class.getName() + "${OM}", 
                this.buttonOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.buttonOMM.getStaticMethodName()
        );
        
        /*
         * Inheritance
         */
        Assert.assertSame(this.componentOMM, this.buttonOMM.getSuperMetadata());
        
        Assert.assertEquals(1, this.buttonOMM.getDeclaredProperties().size());
        
        JPAScalarProperty textProperty = this.buttonOMM.getDeclaredScalarProperty("text");
        
        Assert.assertSame(String.class, textProperty.getReturnClass());
    }
    
    @Test
    public void testTabControlObjectModelMetadata() {
        Assert.assertEquals(
                TabControl.class.getName() + "${OM}", 
                this.tabControlOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.tabControlOMM.getStaticMethodName()
        );
        
        /*
         * Inheritance
         */
        Assert.assertSame(this.containerOMM, this.tabControlOMM.getSuperMetadata());
        
        Assert.assertEquals(1, this.tabControlOMM.getDeclaredProperties().size());
        JPAAssociationProperty tabPagesProperty = this.tabControlOMM.getDeclaredAssociationProperty("tabPages");
        
        Assert.assertSame(this.tabPageOMM, tabPagesProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(
                this.tabPageOMM.getDeclaredAssociationProperty("tabControlReference"), 
                tabPagesProperty.getOppositeProperty()
        );
        Assert.assertTrue(tabPagesProperty.isInverse());
        Assert.assertTrue(tabPagesProperty.isCollection());
        Assert.assertSame(AssociatedEndpointType.LIST, tabPagesProperty.getAssociatedEndpointType());
        Assert.assertSame(List.class, tabPagesProperty.getStandardReturnClass());
        Assert.assertSame(TabPage.class, tabPagesProperty.getElementClass());
        Assert.assertSame(this.tabPageOMM, tabPagesProperty.getReturnObjectModelMetadata());
        
        /*
         * Covariance & Contravariance
         */
        Assert.assertSame(
                this.containerOMM.getDeclaredAssociationProperty("components"), 
                this.tabControlOMM.getDeclaredAssociationProperty("tabPages").getCovarianceProperty()
        );
    }
    
    @Test
    public void testTabPageObjectModelMetadata() {
        Assert.assertEquals(
                TabPage.class.getName() + "${OM}", 
                this.tabPageOMM.getObjectModelClass().getName()
        );
        Assert.assertEquals(
                "{om}",
                this.tabPageOMM.getStaticMethodName()
        );
        
        /*
         * Inheritance
         */
        Assert.assertSame(this.containerOMM, this.tabPageOMM.getSuperMetadata());
        
        /*
         * The name of JPA property is "parent", 
         * but the name of ObjectModel property is "parentReference"
         */
        Assert.assertEquals(
                "tabControlReference", 
                this
                .tabPageOMM
                .getDeclaredMappingSources()
                .get("tabControl")
                .getName()
        );
        Assert.assertEquals(
                "tabControl", 
                this
                .tabPageOMM
                .getDeclaredAssociationProperty("tabControlReference")
                .getOwnerProperty()
                .getName()
        );
        
        Assert.assertEquals(1, this.tabPageOMM.getDeclaredProperties().size());
        JPAAssociationProperty tabControlReferenceProperty = 
                this.tabPageOMM.getDeclaredAssociationProperty("tabControlReference");
        
        Assert.assertSame(this.tabControlOMM, tabControlReferenceProperty.getOppositeProperty().getDeclaringObjectModelMetadata());
        Assert.assertSame(
                this.tabControlOMM.getDeclaredAssociationProperty("tabPages"), 
                tabControlReferenceProperty.getOppositeProperty()
        );
        Assert.assertFalse(tabControlReferenceProperty.isInverse());
        Assert.assertTrue(tabControlReferenceProperty.isReference());
        Assert.assertSame(AssociatedEndpointType.INDEXED_REFERENCE, tabControlReferenceProperty.getAssociatedEndpointType());
        Assert.assertSame(IndexedReference.class, tabControlReferenceProperty.getStandardReturnClass());
        Assert.assertSame(TabControl.class, tabControlReferenceProperty.getElementClass());
        Assert.assertSame(this.tabControlOMM, tabControlReferenceProperty.getReturnObjectModelMetadata());
        
        /*
         * Covariance & Contravariance
         */
        Assert.assertSame(
                this.componentOMM.getDeclaredAssociationProperty("parentReference"), 
                this.tabPageOMM.getDeclaredAssociationProperty("tabControlReference").getCovarianceProperty()
        );
    }
}
