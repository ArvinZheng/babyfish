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
package org.babyfish.model.contravariance;

import junit.framework.Assert;

import org.babyfish.model.metadata.AssociationProperty;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MetadataTest {

    @Test
    public void test1() {
        ObjectModelMetadata tabControlOMM = Metadatas.of(TabControl.class);
        ObjectModelMetadata tabPageOMM = Metadatas.of(TabPage.class);
        AssociationProperty tabPages = tabControlOMM.getDeclaredAssociationProperty("tabPages");
        AssociationProperty parentReference = tabPageOMM.getDeclaredAssociationProperty("parentReference");
        Assert.assertSame(parentReference, tabPages.getOppositeProperty());
        Assert.assertSame(tabPages, parentReference.getOppositeProperty());
    }
    
    @Test
    public void test2() {
        ObjectModelMetadata closeableTabControlOMM = Metadatas.of(CloseableTabControl.class);
        ObjectModelMetadata closeableTabPageOMM = Metadatas.of(CloseableTabPage.class);
        AssociationProperty closeableTabPages = closeableTabControlOMM.getDeclaredAssociationProperty("closeableTabPages");
        AssociationProperty parentReference = closeableTabPageOMM.getDeclaredAssociationProperty("parentReference");
        Assert.assertSame(parentReference, closeableTabPages.getOppositeProperty());
        Assert.assertSame(closeableTabPages, parentReference.getOppositeProperty());
    }
}
