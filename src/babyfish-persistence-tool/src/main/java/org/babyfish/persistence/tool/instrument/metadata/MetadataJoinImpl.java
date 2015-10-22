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
import java.util.Map;

import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.MACollections;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;

/**
 * @author Tao Chen
 */
class MetadataJoinImpl implements MetadataJoin {
    
    private String tableName;
    
    private Map<String, Column> columns;
    
    private Map<String, Column> inverseColumns;
    
    private MetadataJoinImpl reversedJoin;
    
    private transient int hash;
    
    private transient String toString;
    
    public MetadataJoinImpl(Collection<Column> columns) {
        Arguments.mustNotBeEmpty("columns", Arguments.mustNotBeNull("columns", columns));
        Map<String, Column> map = new LinkedHashMap<>((columns.size() * 4 + 2) / 3);
        for (Column column : columns) {
            map.put(column.getName(), column);
        }
        this.columns = MACollections.unmodifiable(map);
    }
    
    public MetadataJoinImpl(String tableName, Collection<Column> columns, Collection<Column> inverseColumns) {
        
        this.tableName = Arguments.mustNotBeEmpty("tableName", Arguments.mustNotBeNull("tableName", tableName));
        
        Arguments.mustNotBeEmpty("columns", Arguments.mustNotBeNull("columns", columns));
        Map<String, Column> map = new LinkedHashMap<>((columns.size() * 4 + 2) / 3);
        for (Column column : columns) {
            map.put(column.getName(), column);
        }
        this.columns = MACollections.unmodifiable(map);
        
        Arguments.mustNotBeEmpty("inverseColumns", Arguments.mustNotBeNull("inverseColumns", inverseColumns));
        Map<String, Column> inverseMap = new LinkedHashMap<>((inverseColumns.size() * 4 + 2) / 3);
        for (Column inverseColumn : inverseColumns) {
            inverseMap.put(inverseColumn.getName(), inverseColumn);
        }
        this.inverseColumns = MACollections.unmodifiable(inverseMap);
    }
    
    private MetadataJoinImpl(String tableName, Map<String, Column> columns, Map<String, Column> inverseColumns) {
        this.tableName = tableName;
        this.columns = columns;
        this.inverseColumns = inverseColumns;
    }

    @Override
    public String getTableName() {
        return this.tableName;
    }

    @Override
    public Map<String, Column> getColumns() {
        return this.columns;
    }

    @Override
    public Map<String, Column> getInverseColumns() {
        return this.inverseColumns;
    }

    @Override
    public MetadataJoin reversedJoin() {
        MetadataJoinImpl reversedJoin = this.reversedJoin;
        if (reversedJoin == null) {
            if (this.tableName == null) {
                reversedJoin = this;
            } else {
                reversedJoin = new MetadataJoinImpl(
                        this.tableName, 
                        this.inverseColumns, 
                        this.columns
                );
            }
            this.reversedJoin = reversedJoin;
        }
        return reversedJoin;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            hash = Nulls.hashCode(this.tableName);
            hash = hash * 31 + this.columns.hashCode();
            hash = hash * 31 + Nulls.hashCode(this.inverseColumns);
            if (hash == 0) {
                hash = -1;
            }
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetadataJoin)) {
            return false;
        }
        MetadataJoin other = (MetadataJoin)obj;
        return Nulls.equals(this.tableName, other.getTableName()) &&
                this.columns.equals(other.getColumns()) &&
                Nulls.equals(this.inverseColumns, other.getInverseColumns());
    }

    @Override
    public String toString() {
        String toString = this.toString;
        if (toString == null) {
            StringBuilder builder = new StringBuilder();
            if (this.tableName != null) {
                builder
                .append("@JoinTable(\n")
                .append("\tname = \"")
                .append(this.tableName)
                .append("\",\n")
                .append("\tjoinColumns = ");
                appendJoinColumns(builder, this.columns.values(), 1);
                builder.append(",\n\tinverseJoinColumns = ");
                appendJoinColumns(builder, this.inverseColumns.values(), 1);
                builder.append("\n)");
            } else if (this.columns.size() > 1) {
                builder.append("@JoinColumns(");
                appendJoinColumns(builder, this.columns.values(), 0);
                builder.append(")");
            } else {
                Column column = this.columns.values().iterator().next();
                builder.append(column.toString());
            }
            this.toString = toString = builder.toString();
        }
        return toString;
    }
    
    private static void appendJoinColumns(
            StringBuilder builder,
            Collection<Column> columns,
            int tabCount) {
        if (columns.size() == 1) {
            Column column = columns.iterator().next();
            builder.append(column.toString());
        }
        else {
            builder.append("{\n");
            appendTabs(builder, tabCount + 1);
            boolean addNewLine = false;
            for (Column column : columns) {
                if (addNewLine) {
                    builder.append(",\n");
                    appendTabs(builder, tabCount + 1);
                } else {
                    addNewLine = true;
                }
                builder.append(column.toString());
            }
            builder.append("\n");
            appendTabs(builder, tabCount);
            builder.append("}");
        }
    }
    
    private static void appendTabs(StringBuilder builder, int tabCount) {
        for (int i = tabCount - 1; i >= 0; i--) {
            builder.append('\t');
        }
    }

    static class ColumnImpl implements Column {
        
        private String name;
        
        private String referencedColumnName;
        
        private boolean insertable;
        
        private boolean updatable;
        
        private transient int hash;
        
        private transient String toString;
        
        public ColumnImpl(
                String name, 
                String referencedColumnName,
                Boolean insertable, 
                Boolean updatable) {
            this.name = Arguments.mustNotBeNull("name", name);
            this.referencedColumnName = Arguments.mustNotBeNull("referencedColumnName", referencedColumnName);
            this.insertable = insertable == null ? true : insertable;
            this.updatable = updatable == null ? true : updatable;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getReferencedColumnName() {
            return this.referencedColumnName;
        }

        @Override
        public boolean isInsertable() {
            return this.insertable;
        }

        @Override
        public boolean isUpdatable() {
            return this.updatable;
        }

        @Override
        public int hashCode() {
            int hash = this.hash;
            if (hash == 0) {
                hash = this.name.hashCode() ^ this.referencedColumnName.hashCode();
                if (hash == 0) {
                    hash = -1;
                }
                this.hash = hash;
            }
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Column)) {
                return false;
            }
            Column other = (Column)obj;
            return this.name.equals(other.getName()) && 
                    this.referencedColumnName.equals(other.getReferencedColumnName());
        }

        @Override
        public String toString() {
            String toString = this.toString;
            if (toString == null) {
                StringBuilder builder = new StringBuilder();
                builder
                .append("@JoinColumn(name = \"")
                .append(this.name)
                .append("\", referencedColumnName = \"")
                .append(this.referencedColumnName)
                .append("\"");
                if (!this.insertable) {
                    builder.append(", insertable = false");
                }
                if (!this.updatable) {
                    builder.append(", updatable = false");
                }
                builder.append(")");
                this.toString = toString = builder.toString();
            }
            return toString;
        }
    }
}
