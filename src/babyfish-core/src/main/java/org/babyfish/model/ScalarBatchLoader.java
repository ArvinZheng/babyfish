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
package org.babyfish.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.babyfish.collection.ArrayList;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.LinkedHashSet;
import org.babyfish.collection.MACollections;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.lang.Nulls;
import org.babyfish.model.metadata.ObjectModelMetadata;
import org.babyfish.model.spi.ObjectModelImplementor;

/**
 * @author Tao Chen
 */
public class ScalarBatchLoader {

    private Map<ObjectModelMetadata, Map<ObjectModel, Set<Integer>>> preparedMap =
            new LinkedHashMap<>(ReferenceEqualityComparator.getInstance());
    
    public void prepareLoad(ObjectModel objectModel, int ... scalarPropertyIds) {
        if (objectModel == null || Nulls.isNullOrEmpty(scalarPropertyIds)) {
            return;
        }
        ObjectModelMetadata metadata = objectModel.getObjectModelMetadata();
        Map<ObjectModel, Set<Integer>> subMap = this.preparedMap.get(metadata);
        if (subMap == null) {
            subMap = new LinkedHashMap<>(ReferenceEqualityComparator.getInstance());
            this.preparedMap.put(metadata, subMap);
        }
        Set<Integer> idSet = subMap.get(objectModel);
        if (idSet == null) {
            idSet = new LinkedHashSet<>((scalarPropertyIds.length * 4 + 2) / 3);
            subMap.put(objectModel, idSet);
        }
        for (int id : scalarPropertyIds) {
            idSet.add(id);
        }
    }
    
    public void flush() {
        if (this.preparedMap.isEmpty()) {
            return;
        }
        for (Map<ObjectModel, Set<Integer>> subMap : this.preparedMap.values()) {
            Map<int[], List<ObjectModel>> reversedSubMap = new LinkedHashMap<>(new IdArrayEqualityComparator());
            for (Entry<ObjectModel, Set<Integer>> e : subMap.entrySet()) {
                int[] idArray = MACollections.toIntArray(e.getValue());
                List<ObjectModel> list = reversedSubMap.get(idArray);
                if (list == null) {
                    list = new ArrayList<>();
                    reversedSubMap.put(idArray, list);
                }
                list.add(e.getKey());
            }
            for (Entry<int[], List<ObjectModel>> e : reversedSubMap.entrySet()) {
                ObjectModelImplementor objectModelImplementor = (ObjectModelImplementor)e.getValue().get(0);
                objectModelImplementor.batchLoad(e.getValue(), e.getKey());
            }
        }
        this.preparedMap = null;
    }
    
    private static class IdArrayEqualityComparator implements EqualityComparator<int[]> {

        @Override
        public int hashCode(int[] o) {
            return Arrays.hashCode(o);
        }

        @Override
        public boolean equals(int[] o1, int[] o2) {
            return Arrays.equals(o1, o2);
        }
    }
}
