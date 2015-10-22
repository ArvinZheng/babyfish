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
package org.babyfish.hibernate.tool.entities;

import java.io.Serializable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen
 */
@JPAObjectModelInstrument
@Embeddable
public class EntityId implements Serializable {

    private static final long serialVersionUID = 7077496778530906470L;

    @Column(name = "PRIMARY_ID")
    private String primaryId;
    
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "prefix", column = @Column(name = "ENITY_ID_PREFIX")),
        @AttributeOverride(name = "main", column = @Column(name = "ENITY_ID_MAIN")),
        @AttributeOverride(name = "postfix", column = @Column(name = "ENITY_ID_POSTFIX"))
    })
    private SecondaryEntityId secondaryId;

    public String getPrimaryId() {
        return primaryId;
    }

    public void setPrimaryId(String primaryId) {
        this.primaryId = primaryId;
    }

    public SecondaryEntityId getSecondaryId() {
        return secondaryId;
    }

    public void setSecondaryId(SecondaryEntityId secondaryId) {
        this.secondaryId = secondaryId;
    }
}
