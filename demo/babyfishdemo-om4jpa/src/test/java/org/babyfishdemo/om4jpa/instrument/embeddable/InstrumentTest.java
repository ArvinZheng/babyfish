package org.babyfishdemo.om4jpa.instrument.embeddable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.babyfish.collection.HashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.model.metadata.ObjectModelMode;
import org.babyfish.persistence.model.metadata.JPAMetadatas;
import org.babyfish.persistence.model.metadata.JPAObjectModelMetadata;
import org.babyfish.persistence.model.metadata.JPAScalarProperty;
import org.babyfishdemo.om4jpa.instrument.embeddable.Name;
import org.babyfishdemo.om4jpa.instrument.embeddable.Person;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InstrumentTest {
    
    private JPAObjectModelMetadata personOMM = JPAMetadatas.of(Person.class);
    
    private JPAObjectModelMetadata nameOMM = JPAMetadatas.of(Name.class);

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
        for (Class<?> clazz : new Class[] { Person.class, Name.class }) {
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
    public void testPersonObjectModelMetadata() {
        Assert.assertEquals(2, this.personOMM.getDeclaredProperties().size());
        
        JPAScalarProperty idProperty = this.personOMM.getDeclaredScalarProperty("id");
        Assert.assertTrue(idProperty.isEntityId());
        Assert.assertSame(idProperty, this.personOMM.getDeclaredEntityIdProperty());
        Assert.assertSame(Long.class, idProperty.getReturnClass());
        
        JPAScalarProperty nameProperty = this.personOMM.getDeclaredScalarProperty("name");
        Assert.assertTrue(nameProperty.isEmbeded());
        Assert.assertSame(this.nameOMM, nameProperty.getReturnObjectModelMetadata());
    }
    
    @Test
    public void testNameObjectModelMetadata() {
        Assert.assertEquals(ObjectModelMode.EMBEDDABLE, this.nameOMM.getMode());
        
        Assert.assertEquals(2, this.nameOMM.getDeclaredProperties().size());
        
        JPAScalarProperty firstNameProperty = this.nameOMM.getDeclaredScalarProperty("firstName");
        Assert.assertSame(String.class, firstNameProperty.getReturnClass());
        
        JPAScalarProperty lastNameProperty = this.nameOMM.getDeclaredScalarProperty("lastName");
        Assert.assertSame(String.class, lastNameProperty.getReturnClass());
    }
}
