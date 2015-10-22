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
package org.babyfish.persistence.tool.instrument;

import org.babyfish.persistence.tool.instrument.metadata.MetadataClass;

/**
 * @author Tao Chen
 */
public class InstrumentedResult {
    
    private MetadataClass metadataClass;

    private byte[] ownerBytecode;
    
    private byte[] objectModelBytecode;

    public InstrumentedResult(
            MetadataClass metadataClass,
            byte[] ownerBytecode, 
            byte[] objectModelBytecode) {
        this.metadataClass = metadataClass;
        this.ownerBytecode = ownerBytecode;
        this.objectModelBytecode = objectModelBytecode;
    }

    public MetadataClass getMetadataClass() {
        return metadataClass;
    }

    public byte[] getOwnerBytecode() {
        return this.ownerBytecode;
    }

    public byte[] getObjectModelBytecode() {
        return this.objectModelBytecode;
    }
}
