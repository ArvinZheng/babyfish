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

import java.util.Collection;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.persistence.tool.instrument.metadata.MetadataJoin;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class MetadataJoinTest {
    
    private static final String JOIN_COLUMN_TEXT =
            "@JoinColumn(name = \"foreignKey\", referencedColumnName = \"primaryKey\")";
    
    private static final String JOIN_COLUMNS_TEXT =
            "@JoinColumns({\n" +
            "\t@JoinColumn(name = \"foreignKey1\", referencedColumnName = \"primaryKey1\"),\n" +
            "\t@JoinColumn(name = \"foreignKey2\", referencedColumnName = \"primaryKey2\")\n" +
            "})";
    
    private static final String SIMPLE_JOIN_TABLE_TEXT =
            "@JoinTable(\n" +
            "\tname = \"simpleMiddleTable\",\n" +
            "\tjoinColumns = @JoinColumn(name = \"thisForeignKey\", referencedColumnName = \"thisPrimaryKey\"),\n" +
            "\tinverseJoinColumns = @JoinColumn(name = \"otherForeignKey\", referencedColumnName = \"otherPrimaryKey\")\n" +
            ")";
    
    private static final String COMPLEX_JOIN_TABLE_TEXT =
            "@JoinTable(\n" +
            "\tname = \"complexMiddleTable\",\n" +
            "\tjoinColumns = {\n" +
            "\t\t@JoinColumn(name = \"thisForeignKey1\", referencedColumnName = \"thisPrimaryKey1\"),\n" +
            "\t\t@JoinColumn(name = \"thisForeignKey2\", referencedColumnName = \"thisPrimaryKey2\")\n" +
            "\t},\n" +
            "\tinverseJoinColumns = {\n" +
            "\t\t@JoinColumn(name = \"otherForeignKeyA\", referencedColumnName = \"otherPrimaryKeyA\"),\n" +
            "\t\t@JoinColumn(name = \"otherForeignKeyB\", referencedColumnName = \"otherPrimaryKeyB\"),\n" +
            "\t\t@JoinColumn(name = \"otherForeignKeyC\", referencedColumnName = \"otherPrimaryKeyC\")\n" +
            "\t}\n" +
            ")"; 

    @Test
    public void testJoinColumn() {
        Collection<MetadataJoin.Column> columns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("foreignKey", "primaryKey", null, null)
        );
        Assert.assertEquals(JOIN_COLUMN_TEXT, new MetadataJoinImpl(columns).toString());
    }
    
    @Test
    public void testJoinColumns() {
        Collection<MetadataJoin.Column> columns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("foreignKey1", "primaryKey1", null, null),
                new MetadataJoinImpl.ColumnImpl("foreignKey2", "primaryKey2", null, null)
        );
        Assert.assertEquals(JOIN_COLUMNS_TEXT, new MetadataJoinImpl(columns).toString());
    }
    
    @Test
    public void testSimpleJoinTable() {
        Collection<MetadataJoin.Column> columns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("thisForeignKey", "thisPrimaryKey", null, null)
        );
        Collection<MetadataJoin.Column> inverseColumns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("otherForeignKey", "otherPrimaryKey", null, null)
        );
        Assert.assertEquals(
                SIMPLE_JOIN_TABLE_TEXT, 
                new MetadataJoinImpl("simpleMiddleTable", columns, inverseColumns).toString()
        );
    }
    
    @Test
    public void testComplexJoinTable() {
        Collection<MetadataJoin.Column> columns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("thisForeignKey1", "thisPrimaryKey1", null, null),
                new MetadataJoinImpl.ColumnImpl("thisForeignKey2", "thisPrimaryKey2", null, null)
        );
        Collection<MetadataJoin.Column> inverseColumns = MACollections.<MetadataJoin.Column>wrap(
                new MetadataJoinImpl.ColumnImpl("otherForeignKeyA", "otherPrimaryKeyA", null, null),
                new MetadataJoinImpl.ColumnImpl("otherForeignKeyB", "otherPrimaryKeyB", null, null),
                new MetadataJoinImpl.ColumnImpl("otherForeignKeyC", "otherPrimaryKeyC", null, null)
        );
        Assert.assertEquals(
                COMPLEX_JOIN_TABLE_TEXT, 
                new MetadataJoinImpl("complexMiddleTable", columns, inverseColumns).toString()
        );
    }
}
