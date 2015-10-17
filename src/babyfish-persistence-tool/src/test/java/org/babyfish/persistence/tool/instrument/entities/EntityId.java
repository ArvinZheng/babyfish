/*
 * BabyFish, Object Model Framework for Java and JPA.
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
package org.babyfish.persistence.tool.instrument.entities;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import org.babyfish.persistence.instrument.JPAObjectModelInstrument;

/**
 * @author Tao Chen(&#38472;&#28059;)
 */
@JPAObjectModelInstrument
@Embeddable
public class EntityId {

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
