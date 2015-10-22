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
package org.babyfish.test.model.nonready;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.Assert;

import org.babyfish.model.NoObjectModelLoaderException;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.ObjectModelFactoryFactory;
import org.babyfish.model.ScalarBatchLoader;
import org.babyfish.model.metadata.Deferrable;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelDeclaration;
import org.babyfish.model.metadata.Scalar;
import org.babyfish.model.metadata.StaticMethodToGetObjectModel;
import org.babyfish.model.spi.ObjectModelImplementor;
import org.babyfish.model.spi.ObjectModelLoaderDirtinessAware;
import org.babyfish.model.spi.ObjectModelScalarLoader;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class DeferralTest {
    
    private static final int ADDRESS = Metadatas.of(Person.class).getDeclaredProperty("address").getId();
    
    private static final int IMAGE = Metadatas.of(Person.class).getDeclaredProperty("image").getId();

    @Test(expected = NoObjectModelLoaderException.class)
    public void testUnload() {
        Person person = new Person();
        person.unloadAddress();
        Assert.assertTrue(person.isAddressUnloaded());
        person.getAddress();
    }
    
    @Test
    public void testImplicitlyLoad() {
        Person person = new Person();
        person.unloadAddress();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        Assert.assertTrue(person.isAddressUnloaded());
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
    }
    
    @Test
    public void testExplicitlyLoad() {
        Person person = new Person();
        person.unloadAddress();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        Assert.assertTrue(person.isAddressUnloaded());
        person.loadAddress();
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
    }
    
    @Test(expected = NoObjectModelLoaderException.class)
    public void testUnloadByOMAPI() {
        Person person = new Person();
        person.unloadAddress();
        Assert.assertTrue(Person.om(person).isUnloaded(ADDRESS));
        Person.om(person).getScalar(ADDRESS);
    }
    
    @Test
    public void testImplicitlyLoadByOMAPI() {
        Person person = new Person();
        person.unloadAddress();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        Assert.assertTrue(Person.om(person).isUnloaded(ADDRESS));
        Assert.assertEquals("addressLoadedByLoader", Person.om(person).getScalar(ADDRESS));
        Assert.assertFalse(Person.om(person).isUnloaded(ADDRESS));
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
    }
    
    @Test
    public void testExplicitlyLoadByOMAPI() {
        Person person = new Person();
        person.unloadAddress();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        Assert.assertTrue(Person.om(person).isUnloaded(ADDRESS));
        Person.om(person).load(ADDRESS);
        Assert.assertFalse(Person.om(person).isUnloaded(ADDRESS));
        Assert.assertEquals("addressLoadedByLoader", Person.om(person).getScalar(ADDRESS));
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
    }
    
    @Test
    public void testLoadAddressFirst() {
        Person person = new Person();
        person.unloadAddress();
        person.unloadImage();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertTrue(person.isAddressUnloaded());
        Assert.assertTrue(person.isImageUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
        
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertFalse(person.isImageUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, person.getImage()));
    }
    
    @Test
    public void testLoadImageFirst() {
        Person person = new Person();
        person.unloadAddress();
        person.unloadImage();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        Assert.assertTrue(person.isAddressUnloaded());
        Assert.assertTrue(person.isImageUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 2, 3 }, person.getImage()));
        
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertFalse(person.isImageUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        Assert.assertEquals("addressLoadedByLoader", person.getAddress());
    }
    
    @Test
    public void testChangeDirty() {
        Person person = new Person();
        person.unloadAddress();
        person.unloadImage();
        ((ObjectModelImplementor)Person.om(person)).setScalarLoader(new LoaderImpl());
        
        Assert.assertTrue(person.isAddressUnloaded());
        Assert.assertTrue(person.isImageUnloaded());
        Assert.assertEquals(0, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        person.setAddress("Unknown");
        Assert.assertEquals("Unknown", person.getAddress());
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertTrue(person.isImageUnloaded());
        Assert.assertEquals(1, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
        
        person.setImage(new byte[] {1, 4, 9 });
        Assert.assertEquals("Unknown", person.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] { 1, 4, 9 }, person.getImage()));
        Assert.assertFalse(person.isAddressUnloaded());
        Assert.assertFalse(person.isImageUnloaded());
        Assert.assertEquals(2, ((LoaderImpl)((ObjectModelImplementor)Person.om(person)).getScalarLoader()).getDirtyCount());
    }
    
    @Test
    public void testBatchLoad() {
        Person person1 = new Person();
        Person person2 = new Person();
        LoaderImpl loader = new LoaderImpl();
        ((ObjectModelImplementor)Person.om(person1)).setScalarLoader(loader);
        ((ObjectModelImplementor)Person.om(person2)).setScalarLoader(loader);
        person1.unloadAddress();
        person1.unloadImage();
        person2.unloadAddress();
        person2.unloadImage();
        
        Assert.assertTrue(person1.isAddressUnloaded());
        Assert.assertTrue(person1.isImageUnloaded());
        Assert.assertTrue(person2.isAddressUnloaded());
        Assert.assertTrue(person2.isImageUnloaded());
        
        ScalarBatchLoader sbl = new ScalarBatchLoader();
        sbl.prepareLoad(Person.om(person1), ADDRESS, IMAGE);
        sbl.prepareLoad(Person.om(person2), ADDRESS, IMAGE);
        
        Assert.assertTrue(person1.isAddressUnloaded());
        Assert.assertTrue(person1.isImageUnloaded());
        Assert.assertTrue(person2.isAddressUnloaded());
        Assert.assertTrue(person2.isImageUnloaded());
        
        sbl.flush();
        
        Assert.assertFalse(person1.isAddressUnloaded());
        Assert.assertFalse(person1.isImageUnloaded());
        Assert.assertFalse(person2.isAddressUnloaded());
        Assert.assertFalse(person2.isImageUnloaded());
        Assert.assertEquals("addressLoadedByLoader[0]", person1.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] {1, 2, 3 }, person1.getImage()));
        Assert.assertEquals("addressLoadedByLoader[1]", person2.getAddress());
        Assert.assertTrue(Arrays.equals(new byte[] {2, 3, 4 }, person2.getImage()));
    }
    
    static class Person {
        
        private static final ObjectModelFactory<OM> OM_FACTORY =
                ObjectModelFactoryFactory.factoryOf(OM.class);
        
        private OM om = OM_FACTORY.create(this);
        
        @StaticMethodToGetObjectModel
        static OM om(Person person) {
            return person.om;
        }
        
        public String getName() {
            return om.getName();
        }
    
        public void setName(String name) {
            om.setName(name);
        }
    
        public String getAddress() {
            return om.getAddress();
        }
    
        public void setAddress(String address) {
            om.setAddress(address);
        }
        
        public byte[] getImage() {
            return om.getImage();
        }

        public void setImage(byte[] image) {
            om.setImage(image);
        }

        public boolean isAddressUnloaded() {
            return om.isUnloaded(ADDRESS);
        }
        
        public void unloadAddress() {
            om.unload(ADDRESS);
        }
        
        public void loadAddress() {
            om.load(ADDRESS);
        }
        
        public boolean isImageUnloaded() {
            return om.isUnloaded(IMAGE);
        }
        
        public void unloadImage() {
            om.unload(IMAGE);
        }
        
        public void loadImage() {
            om.load(IMAGE);
        }
        
        @ObjectModelDeclaration
        private interface OM extends ObjectModel {
            
            @Scalar
            String getName();
            void setName(String name);
            
            @Deferrable
            String getAddress();
            void setAddress(String address);
            
            @Deferrable
            byte[] getImage();
            void setImage(byte[] image);
        }
    }

    static class LoaderImpl implements ObjectModelScalarLoader, ObjectModelLoaderDirtinessAware {
        
        private int dirtyCount;
        
        @Override
        public void loadScalars(Collection<ObjectModel> objectModels, int[] scalarPropertyIds) {
            boolean batch = objectModels.size() > 1;
            int index = 0;
            for (ObjectModel objectModel : objectModels) {
                for (int scalarPropertyId : scalarPropertyIds) {
                    if (scalarPropertyId == ADDRESS) {
                        objectModel.setScalar(
                                scalarPropertyId, 
                                "addressLoadedByLoader" + (batch ? "[" + index + ']' : "")
                        );
                    } else if (scalarPropertyId == IMAGE) {
                        objectModel.setScalar(
                                scalarPropertyId, 
                                new byte[] { (byte)(index + 1), (byte)(index + 2), (byte)(index + 3) }
                        );
                    } else {
                        throw new IllegalArgumentException();
                    }
                }
                index++;
            }
        }

        @Override
        public void dirty() {
            this.dirtyCount++;
        }
        
        public int getDirtyCount() {
            return dirtyCount;
        }
    }
}
