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
package org.babyfish.persistence.tool.instrument.metadata;

import java.util.List;
import java.util.Map;

import javax.persistence.AccessType;
import javax.persistence.FetchType;

/**
 * @author Tao Chen
 */
public interface MetadataProperty {

    MetadataClass getDeclaringClass();
    
    String getName();
    
    AccessType getAccessType();
    
    String getGetterName();
    
    String getSetterName(boolean canReturnNull);
    
    String getTypeDescriptor();
    
    String getKeyTypeDescriptor();
    
    List<MetadataAnnotation> getVisibleAnnotations();
    
    List<MetadataAnnotation> getInvisibleAnnotations();
    
    String getColumnName();
    
    Map<MetadataProperty, String> getEmbeddedColumnNames();
    
    boolean isOverriding();
    
    boolean isBasic();
    
    boolean isLob(); // Not be exclude with "isBasic()", when "isLob()" is true, "isBasic()" should be true too.
    
    boolean isId();
    
    boolean isVersion();
    
    boolean isEmbedded();
    
    boolean isAssociation();
    
    boolean isReference();
    
    boolean isOneToOne();
    
    boolean isManyToOne();
    
    boolean isAny();
    
    boolean isCollection();
    
    boolean isOneToMany();
    
    boolean isManyToMany();
    
    FetchType getFetchType();
    
    MetadataClass getRelatedClass();
    
    MetadataJoin getJoin();
    
    Class<?> getStandardAssocationType();
    
    String getReferenceComparisonRule();

    MetadataProperty getOppositeProperty();
    
    MetadataProperty getOppositeIndexProperty();
    
    MetadataProperty getOppositeKeyProperty();
    
    MetadataProperty getIndexProperty();

    MetadataProperty getKeyProperty();

    MetadataProperty getReferenceProperty();
    
    MetadataProperty getCovarianceProperty();
    
    boolean isInverse();
}
