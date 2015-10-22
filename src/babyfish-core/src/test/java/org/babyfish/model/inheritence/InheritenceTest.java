/*
 * BabyFish, Object Model Framework for Java and JPA.
 * https://github.com/babyfish-ct/babyfish
 *
 * Copyright (c) 2008-2015, Tao Chen
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * Please visit "http://opensource.org/licenses/LGPL-3.0" to know more.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 */
package org.babyfish.model.inheritence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import junit.framework.Assert;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.lang.Func;
import org.babyfish.lang.UncheckedException;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class InheritenceTest {
    
    @Test
    public void testMetadata() {
        ObjectModelMetadata tabControlObjectModelMetadata = Metadatas.of(TabControl.class);
        ObjectModelMetadata tabPageObjectModelMetadata = Metadatas.of(TabPage.class);
        Assert.assertNotNull(
                tabControlObjectModelMetadata
                .getDeclaredAssociationProperty("pages")
                .getCovarianceProperty());
        Assert.assertNotNull(
                tabPageObjectModelMetadata
                .getDeclaredAssociationProperty("parentReference")
                .getCovarianceProperty());
    }
    
    @Test
    public void testInheritedProperies() {
        TabControl tabControl = new TabControl();
        ObjectModel om = 
                (ObjectModel)
                ObjectModelFactoryFactory
                .factoryOf(nestedClass(TabControl.class, "OM"))
                .get(tabControl);
        int widthId = om.getObjectModelMetadata().getScalarProperty("width").getId();
        int heightId = om.getObjectModelMetadata().getScalarProperty("height").getId();
        int controlsId = om.getObjectModelMetadata().getAssociationProperty("controls").getId();
        int parentId = om.getObjectModelMetadata().getAssociationProperty("parentReference").getId();
        int pagesId = om.getObjectModelMetadata().getAssociationProperty("pages").getId();
        Assert.assertTrue(controlsId != pagesId);
        om.setScalar(widthId, 400);
        om.setScalar(heightId, 300);
        Assert.assertEquals(400, om.getScalar(widthId));
        Assert.assertEquals(300, om.getScalar(heightId));
        om.getAssociation(parentId);
        Assert.assertSame(om.getAssociation(controlsId), om.getAssociation(pagesId));
    }

    @Test
    public void testModifyTabControl() {
        TabControl tabControl = new TabControl();
        Assert.assertSame(tabControl.getControls(), tabControl.getPages());
        tabControl.setWidth(400);
        tabControl.setHeight(300);
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        tabControl.getPages().add(tabPage1);
        tabControl.getPages().add(tabPage2);
        assertCollection(tabControl.getPages(), tabPage1, tabPage2);
        assertCollection(tabControl.getControls(), tabPage1, tabPage2);
        Assert.assertSame(tabControl, tabPage1.getParent());
        Assert.assertSame(tabControl, tabPage2.getParent());
    }
    
    @Test
    public void testModifyTabControlByCovarianceProperty() {
        TabControl tabControl = new TabControl();
        Assert.assertSame(tabControl.getControls(), tabControl.getPages());
        tabControl.setWidth(400);
        tabControl.setHeight(300);
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        tabControl.getControls().add(tabPage1);
        tabControl.getControls().add(tabPage2);
        assertCollection(tabControl.getPages(), tabPage1, tabPage2);
        assertCollection(tabControl.getControls(), tabPage1, tabPage2);
        Assert.assertSame(tabControl, tabPage1.getParent());
        Assert.assertSame(tabControl, tabPage2.getParent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testBadModificationWithInvalidObjectOnCovarianceProperty() {
        TabControl tabControl = new TabControl();
        tabControl.getControls().add(new Control());
    }
    
    @Test
    public void testModifyTabPage() {
        TabControl tabControl = new TabControl();
        Assert.assertSame(tabControl.getControls(), tabControl.getPages());
        tabControl.setWidth(400);
        tabControl.setHeight(300);
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        tabPage1.setParent(tabControl);
        tabPage2.setParent(tabControl);
        assertCollection(tabControl.getPages(), tabPage1, tabPage2);
        assertCollection(tabControl.getControls(), tabPage1, tabPage2);
        Assert.assertSame(tabControl, tabPage1.getParent());
        Assert.assertSame(tabControl, tabPage2.getParent());
    }
    
    @Test
    public void testSerializeTabControl() {
        this.testSerialize(new Func<TabControl, TabControl>() {
            @Override
            public TabControl run(TabControl x) {
                return serializingClone(x);
            }
        });
    }
    
    @Test
    public void testSerializeTabPage() {
        this.testSerialize(new Func<TabControl, TabControl>() {
            @Override
            public TabControl run(TabControl x) {
                return serializingClone(x.getPages().get(0)).getParent();
            }
        });
    }
    
    @Test
    public void testSerializeTabPages() {
        this.testSerialize(new Func<TabControl, TabControl>() {
            @Override
            public TabControl run(TabControl x) {
                return (TabControl)((AssociatedEndpoint<?, ?>)serializingClone(x.getPages())).getOwner();
            }
        });
    }
    
    private void testSerialize(Func<TabControl, TabControl> serializingCloneFunc) {
        TabControl tabControl = new TabControl();
        Assert.assertSame(tabControl.getControls(), tabControl.getPages());
        tabControl.setWidth(400);
        tabControl.setHeight(300);
        TabPage tabPage1 = new TabPage();
        TabPage tabPage2 = new TabPage();
        tabPage1.setText("tab1");
        tabPage2.setText("tab2");
        tabControl.getPages().add(tabPage1);
        tabControl.getPages().add(tabPage2);
        
        TabControl deserializedTabControl = serializingCloneFunc.run(tabControl);
        Assert.assertTrue(tabControl != deserializedTabControl);
        Assert.assertEquals(400, deserializedTabControl.getWidth());
        Assert.assertEquals(300, deserializedTabControl.getHeight());
        Assert.assertEquals(2, deserializedTabControl.getPages().size());
        TabPage deserializedTabPage1 = deserializedTabControl.getPages().get(0);
        TabPage deserializedTabPage2 = deserializedTabControl.getPages().get(1);
        Assert.assertTrue(tabPage1 != deserializedTabPage1);
        Assert.assertTrue(tabPage2 != deserializedTabPage2);
        Assert.assertEquals("tab1", deserializedTabPage1.getText());
        Assert.assertEquals("tab2", deserializedTabPage2.getText());
        TabPage tabPage3 = new TabPage();
        tabPage3.setText("tab3");
        deserializedTabControl.getPages().add(tabPage3);
        assertCollection(
                deserializedTabControl.getPages(), 
                deserializedTabPage1, 
                deserializedTabPage2, 
                tabPage3);
        Assert.assertSame(deserializedTabControl, tabPage3.getParent());
        TabPage tabPage4 = new TabPage();
        tabPage3.setText("tab4");
        tabPage4.setParent(deserializedTabControl);
        assertCollection(
                deserializedTabControl.getPages(), 
                deserializedTabPage1, 
                deserializedTabPage2, 
                tabPage3,
                tabPage4);
        Assert.assertSame(deserializedTabControl, tabPage4.getParent());
        deserializedTabControl.getPages().add(deserializedTabPage2);
        assertCollection(
                deserializedTabControl.getPages(), 
                deserializedTabPage1, 
                tabPage3,
                tabPage4,
                deserializedTabPage2);
        deserializedTabControl.getPages().add(deserializedTabPage1);
        assertCollection(
                deserializedTabControl.getPages(), 
                tabPage3,
                tabPage4,
                deserializedTabPage2,
                deserializedTabPage1);
    }
    
    @SuppressWarnings("unchecked")
    private static <T> T serializingClone(T obj) {
        try {
            byte[] buf;
            try (ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    ObjectOutputStream oout = new ObjectOutputStream(bout)) {
                oout.writeObject(obj);
                oout.flush();
                buf = bout.toByteArray();
            }
            try (ByteArrayInputStream bin = new ByteArrayInputStream(buf);
                    ObjectInputStream oin = new ObjectInputStream(bin)) {
                return (T)oin.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw UncheckedException.rethrow(ex);
        }
    }
    
    private static void assertCollection(Collection<?> c, Object ... elements) {
        Assert.assertEquals(elements.length, c.size());
        int index = 0;
        for (Object o : c) {
            Assert.assertSame(elements[index++], o);
        }
    }
    
    private static Class<?> nestedClass(Class<?> clazz, String name) {
        for (Class<?> nestedClass : clazz.getDeclaredClasses()) {
            if (nestedClass.getSimpleName().equals(name)) {
                return nestedClass;
            }
        }
        return null;
    }
}
