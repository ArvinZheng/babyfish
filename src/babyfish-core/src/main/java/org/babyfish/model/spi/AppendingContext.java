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
package org.babyfish.model.spi;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.babyfish.association.AssociatedEndpoint;
import org.babyfish.collection.EqualityComparator;
import org.babyfish.collection.HashMap;
import org.babyfish.collection.LinkedHashMap;
import org.babyfish.collection.ReferenceEqualityComparator;
import org.babyfish.collection.XOrderedMap;
import org.babyfish.lang.Arguments;
import org.babyfish.lang.Nulls;
import org.babyfish.model.ObjectModel;
import org.babyfish.model.ObjectModelFactory;
import org.babyfish.model.metadata.Metadatas;
import org.babyfish.reference.IndexedReference;
import org.babyfish.reference.KeyedReference;
import org.babyfish.reference.Reference;
import org.babyfish.state.DisablityManageable;
import org.babyfish.state.LazinessManageable;

/**
 * @author Tao Chen
 */
public class AppendingContext {
    
    private Appendable appendable;
    
    private static final String INDENT_TEXT = "    ";
    
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");
    
    private static final char[] HEX_SYMBOLS = new char[] { 
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' 
    };
    
    private static final int MAX_TEXT_LENGTH = 256;

    private XOrderedMap<Object, AppendingNode> appendingMap = 
            new LinkedHashMap<>(
                    ReferenceEqualityComparator.getInstance(),
                    (EqualityComparator<AppendingNode>)null);
            
    private Map<Class<?>, ObjectModelFactory<ObjectModelAppender>> noLockObjectModelFactoryMap = new HashMap<>();
            
    private boolean multipleLine;
    
    private int indent;
    
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private boolean needIndent;
    
    private int referenceIdSequence;
    
    public AppendingContext(Appendable appendable, boolean multipleLine) {
        this.appendable = Arguments.mustNotBeNull("appendable", appendable);
        this.multipleLine = multipleLine;
    }
    
    public static String toString(ObjectModel objectModel) {
        StringBuilder builder = new StringBuilder();
        try {
            ((ObjectModelAppender)objectModel).appendTo(new AppendingContext(builder, true));
        } catch (IOException ex) {
            // Do nothing! 
            // IOException never happens here!(I hate the checked exception of Java very much!)
        }
        return builder.toString();
    }
    
    public boolean enter(ObjectModel objectModel) throws IOException {
        return this.push(Arguments.mustNotBeNull("objectModel", objectModel), false);
    }
    
    public void exit(ObjectModel objectModel) throws IOException {
        if (this.appendingMap.lastKey() != objectModel) {
            throw new IllegalStateException();
        }
        this.pop();
    }
    
    public void appendReferenceId() throws IOException {
        this.appendable.append(Integer.toString(this.appendingMap.lastEntry().getValue().referenceId));
    }
    
    public void appendPropertyName(String propertyName) throws IOException {
        this.appendKey(
                Arguments.mustNotBeEmpty(
                        "propertyName",
                        Arguments.mustNotBeNull("propertyName", propertyName)
                )
        );
    }

    public void appendValue(boolean value) throws IOException {
        this.appendable.append(Boolean.toString(value));
    }
    
    public void appendValue(char value) throws IOException {
        this.appendable.append('"').append(value).append('"');
    }
    
    public void appendValue(byte value) throws IOException {
        this.appendable.append(Byte.toString(value));
    }
    
    public void appendValue(short value) throws IOException {
        this.appendable.append(Short.toString(value));
    }
    
    public void appendValue(int value) throws IOException {
        this.appendable.append(Integer.toString(value));
    }
    
    public void appendValue(long value) throws IOException {
        this.appendable.append(Long.toString(value));
    }
    
    public void appendValue(float value) throws IOException {
        this.appendable.append(Float.toString(value));
    }
    
    public void appendValue(double value) throws IOException {
        this.appendable.append(Double.toString(value));
    }
    
    public void appendValue(Boolean value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Character value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Byte value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Short value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Integer value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Long value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Float value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(Double value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(BigInteger value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(BigDecimal value) throws IOException {
        this.appendable.append(value != null ? value.toString() : "null");
    }
    
    public void appendValue(String value) throws IOException {
        Appendable appendable = this.appendable;
        if (value == null) {
            appendable.append("null");
        }
        else {
            appendable.append('"');
            int len = Math.min(value.length(), MAX_TEXT_LENGTH);
            for (int i = 0; i < len; i++) {
                this.append(value.charAt(i));
            }
            if (value.length() > MAX_TEXT_LENGTH) {
                appendable.append("...");
            }
            appendable.append('"');
        }
    }
    
    public void appendValue(char[] value) throws IOException {
        Appendable appendable = this.appendable;
        if (value == null) {
            appendable.append("null");
        } else {
            appendable.append('"');
            int len = Math.min(value.length, MAX_TEXT_LENGTH);
            for (int i = 0; i < len; i++) {
                this.append(value[i]);
            }
            if (value.length > MAX_TEXT_LENGTH) {
                appendable.append("...");
            }
            appendable.append('"');
        }
    }
    
    public void appendValue(byte[] value) throws IOException {
        Appendable appendable = this.appendable;
        if (value == null) {
            appendable.append("null");
        } else {
            appendable.append('0').append('x');
            int len = Math.min(value.length, MAX_TEXT_LENGTH);
            for (int i = 0; i < len; i++) {
                byte b = value[i];
                appendable
                .append(HEX_SYMBOLS[(b >>> 4) & 0x0F])
                .append(HEX_SYMBOLS[b & 0x0F]);
            }
            if (value.length > MAX_TEXT_LENGTH) {
                appendable.append("...");
            }
        }
    }
    
    public void appendValue(Date value) throws IOException {
        this.append(value);
    }
    
    public void appendValue(Time value) throws IOException {
        this.append(value);
    }
    
    public void appendValue(java.sql.Date value) throws IOException {
        this.append(value);
    }
    
    public void appendValue(Timestamp value) throws IOException {
        this.append(value);
    }
    
    public void appendValue(Object value) throws IOException {
        if (value == null) {
            this.appendable.append("null");
        } else {
            this.appendValue(value.toString());
        }
    }
    
    public void appendValue(Enum<?> value) throws IOException {
        if (value == null) {
            this.appendable.append("null");
        } else {
            this
            .appendable
            .append('"')
            .append(value.name())
            .append('"');
        }
    }
    
    public void appendValue(Collection<?> c) throws IOException {
        if (c == null) {
            //May be null, because association field uses lazy creating
            this.appendable.append("[]");
        }
        Arguments.mustBeInstanceOfAllOfValue("c", c, AssociatedEndpointDescriptor.class, AssociatedEndpoint.class);
        if (((DisablityManageable)c).isDisabled()) {
            this.appendable.append("$disabledCollection()");
            return;
        }
        if (!((LazinessManageable)c).isLoaded()) {
            this.appendable.append("$unloadedCollection()");
            return;
        }
        if (!this.push(c, true)) {
            return;
        }
        try {
            Appendable appendable = this.appendable;
            for (Object e : c) {
                this.appendKey(null);
                if (e == null) {
                    appendable.append("null");
                } else {
                    this.dynamicObjectModelAppender(e).appendTo(this);
                }
            }
        } finally {
            this.pop();
        }
    }
    
    public void appendValue(Map<?, ?> m) throws IOException {
        if (m == null) {
            //May be null, because association field uses lazy creating
            this.appendable.append("[]");
        }
        Arguments.mustBeInstanceOfAllOfValue("m", m, AssociatedEndpointDescriptor.class, AssociatedEndpoint.class);
        if (((DisablityManageable)m).isDisabled()) {
            this.appendable.append("$disabledMap()");
            return;
        }
        if (!((LazinessManageable)m).isLoaded()) {
            this.appendable.append("$unloadedMap()");
            return;
        }
        if (!this.push(m, true)) {
            return;
        }
        try {
            ObjectModelFactory<?> keyObjectModelFactory = ((AssociatedEndpointDescriptor)m).getKeyObjectModelFactory();
            Appendable appendable = this.appendable;
            for (Entry<?, ?> entry : m.entrySet()) {
                this.appendKey(null);
                Object key = entry.getKey();
                Object value = entry.getValue();
                this.push(entry, false);
                try {
                    this.appendKey("key");
                    if (keyObjectModelFactory == null) {
                        appendable.append(Nulls.toString(key));
                    } else {
                        this.dynamicObjectModelAppender(key).appendTo(this);
                    }
                    this.appendKey("value");
                    if (value == null) {
                        appendable.append("null");
                    } else {
                        this.dynamicObjectModelAppender(value).appendTo(this);
                    }
                } finally {
                    this.pop();
                }
            }
        } finally {
            this.pop();
        }
    }
    
    public void appendValue(Reference<?> r) throws IOException {
        if (r == null) {
            //May be null, because association field uses lazy creating
            this.appendable.append("null");
        }
        Arguments.mustBeInstanceOfAllOfValue("r", r, AssociatedEndpointDescriptor.class, AssociatedEndpoint.class);
        if (((DisablityManageable)r).isDisabled()) {
            this.appendable.append("$disabledReference()");
            return;
        }
        if (!((LazinessManageable)r).isLoaded()) {
            this.appendable.append("$unloadedReference()");
            return;
        }
        if (!this.push(r, false)) {
            return;
        }
        try {
            ObjectModelFactory<?> keyObjectModelFactory = ((AssociatedEndpointDescriptor)r).getKeyObjectModelFactory();
            Appendable appendable = this.appendable;
            Object value = r.get();
            if (r instanceof KeyedReference<?, ?>) {
                Object key = ((KeyedReference<?, ?>)r).getKey();
                this.appendKey("key");
                if (keyObjectModelFactory == null) {
                    appendable.append(Nulls.toString(key));
                } else {
                    this.dynamicObjectModelAppender(key).appendTo(this);
                }
            } else if (r instanceof IndexedReference<?>) {
                int index = ((IndexedReference<?>)r).getIndex();
                this.appendKey("index");
                appendable.append(Integer.toString(index));
            }
            this.appendKey("value");
            if (value == null) {
                appendable.append("null");
            } else {
                this.dynamicObjectModelAppender(value).appendTo(this);
            }
        } finally {
            this.pop();
        }
    }
    
    public void appendNullValue() throws IOException {
        this.appendable.append("null");
    }
    
    public void appendDisabledValue() throws IOException {
        this.appendable.append("@disabled()");
    }
    
    public void appendUnloadedValue() throws IOException {
        this.appendable.append("@unloaded()");
    }
    
    private boolean push(Object object, boolean collection) throws IOException {
        Appendable appendable = this.appendable;
        AppendingNode appendingNode = this.appendingMap.get(object);
        if (appendingNode != null) {
            appendable
            .append("@backReference(")
            .append(Integer.toString(appendingNode.referenceId))
            .append(")");
            return false;
        }
        this.appendingMap.put(object, new AppendingNode(++this.referenceIdSequence, collection));
        this.doIndent();
        appendable.append(collection ? '[' : '{');
        if (this.multipleLine) {
            this.indent++;
        }
        return true;
    }
    
    private void pop() throws IOException {
        Appendable appendable = this.appendable;
        AppendingNode appendingNode = this.appendingMap.pollLastEntry().getValue();
        if (this.multipleLine) {
            if (appendingNode.hasContent) {
                this.newLine();
            }
            this.indent--;
            this.doIndent();
        }
        appendable.append(appendingNode.collection ? ']' : '}');
    }
    
    private void appendKey(String key) throws IOException {
        Appendable appendable = this.appendable;
        AppendingNode appendingNode = this.appendingMap.lastEntry().getValue();
        
        if (appendingNode.hasContent) {
            appendable.append(',');
        } else {
            appendable.append(' ');
        }
        if (this.multipleLine) {
            this.newLine();
            this.doIndent();
        } else {
            appendable.append(' ');
        }
        if (!Nulls.isNullOrEmpty(key)) {
            appendable.append(key).append(':').append(' ');
        }
        appendingNode.hasContent = true;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private ObjectModelAppender dynamicObjectModelAppender(Object obj) {
        Class<?> clazz = obj.getClass();
        ObjectModelFactory<ObjectModelAppender> factory = (ObjectModelFactory)this.noLockObjectModelFactoryMap.get(clazz);
        if (factory == null) {
            factory = (ObjectModelFactory)Metadatas.of(clazz).getFactory();
            this.noLockObjectModelFactoryMap.put(clazz, factory);
        }
        return factory.get(obj);
    }
    
    private void newLine() throws IOException {
        this.appendable.append(LINE_SEPARATOR);
        this.needIndent = true;
    }
    
    private void doIndent() throws IOException {
        if (this.needIndent) {
            this.needIndent = false;
            Appendable appendable = this.appendable;
            for (int i = this.indent - 1; i >= 0; i--) {
                appendable.append(INDENT_TEXT);
            }
        }
    }
    
    private void append(char c) throws IOException {
        if (c == '\t') {
            this.appendable.append('\\').append('t');
            return;
        }
        if (c == '\r') {
            this.appendable.append('\\').append('r');
            return;
        }
        if (c == '\n') {
            this.appendable.append('\\').append('n');
            return;
        }
        if (c == '"') {
            this.appendable.append('\\').append('"');
            return;
        }
        this.appendable.append(c);
    }
    
    private void append(Date date) throws IOException {
        if (date == null) {
            this.appendable.append("null");
        } else {
            this
            .appendable
            .append('"')
            .append(this.dateFormat.format(date))
            .append('"');
        }
    }
    
    private static class AppendingNode {
        
        int referenceId;
        
        boolean collection;
        
        boolean hasContent;
        
        public AppendingNode(int no, boolean collection) {
            this.referenceId = no;
            this.collection = collection;
        }
    }
}
