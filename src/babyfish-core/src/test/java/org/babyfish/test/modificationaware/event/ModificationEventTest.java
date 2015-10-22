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
package org.babyfish.test.modificationaware.event;

import java.math.BigDecimal;

import junit.framework.Assert;

import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.viewinfo.CollectionViewInfos;
import org.babyfish.modificationaware.event.BubbledProperty;
import org.babyfish.modificationaware.event.BubbledPropertyConverter;
import org.babyfish.modificationaware.event.BubbledSharedProperty;
import org.babyfish.modificationaware.event.BubbledSharedPropertyConverter;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.EventDeclaration;
import org.babyfish.modificationaware.event.EventFactory;
import org.babyfish.modificationaware.event.EventProperty;
import org.babyfish.modificationaware.event.EventType;
import org.babyfish.modificationaware.event.Modification;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.babyfish.modificationaware.event.ModificationType;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.babyfish.view.ViewInfo;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class ModificationEventTest {
    
    private static final Modification MODIFICATION = CollectionModifications.<Object>clear();
    
    private static final ViewInfo VIEW = CollectionViewInfos.iterator();

    @Test
    public void testCreateDetachEvent() {
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>createDetachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'A', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "Old-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        Assert.assertEquals(EventType.PROTOSOMATIC, e.getEventType());
        Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertSame(MODIFICATION, e.getModification());
        Assert.assertNull(e.getCause());
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('A', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('\0', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(3, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(0, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(4L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(0L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertEquals("Old-String", e.getString(PropertyVersion.DETACH));
        Assert.assertNull(e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertNull(e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testCreateAttachEvent() {
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>createAttachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'A', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        Assert.assertEquals(EventType.PROTOSOMATIC, e.getEventType());
        Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertSame(MODIFICATION, e.getModification());
        Assert.assertNull(e.getCause());
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('\0', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('A', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(0, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(3, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(0L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(4L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertNull(e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertNull(e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testCreateReplaceEvent() {
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>createReplaceEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                false,
                'A', 
                'X',
                'a',
                (byte)1,
                (byte)-1,
                (byte)2,
                (short)2,
                (short)-2,
                (short)3,
                3,
                -3,
                4,
                4L,
                -4L,
                5L,
                5F,
                -5F,
                6L,
                6D,
                -6D,
                7D, 
                "Old-String",
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"),
                new BigDecimal("-1234567.8"),
                new BigDecimal("2345678.9"));
        Assert.assertEquals(EventType.PROTOSOMATIC, e.getEventType());
        Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertSame(MODIFICATION, e.getModification());
        Assert.assertNull(e.getCause());
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('A', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('X', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)-1, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)-2, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(3, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(-3, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(4L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(-4L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(-5F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(-6D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertEquals("Old-String", e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("-1234567.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testBubbleetachEventWithoutConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createDetachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'A', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "Old-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('A', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('\0', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(3, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(0, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(4L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(0L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertEquals("Old-String", e.getString(PropertyVersion.DETACH));
        Assert.assertNull(e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertNull(e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testBubbleAttachEventWithoutConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createAttachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'A', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('\0', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('A', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(0, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(3, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(0L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(4L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertNull(e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertNull(e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testBubbleReplaceEventWithoutConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createReplaceEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                false,
                'A', 
                'X',
                'a',
                (byte)1,
                (byte)-1,
                (byte)2,
                (short)2,
                (short)-2,
                (short)3,
                3,
                -3,
                4,
                4L,
                -4L,
                5L,
                5F,
                -5F,
                6L,
                6D,
                -6D,
                7D, 
                "Old-String",
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"),
                new BigDecimal("-1234567.8"),
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('A', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('X', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)-1, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)-2, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(3, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(-3, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(4L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(-4L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(-5F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(-6D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertEquals("Old-String", e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("-1234567.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBubbleetachEventWithConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createDetachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'A', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "Old-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                new BubbledPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledProperty<Boolean> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(!causeEvent.isBoolean(PropertyVersion.DETACH));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(!causeEvent.isBoolean(PropertyVersion.ATTACH));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledSharedProperty<Boolean> bubbledSharedProperty) {
                        bubbledSharedProperty.setValue(!bubbledSharedProperty.getValue());
                    }
                },
                new BubbledPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledProperty<Character> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (char)(causeEvent.getChar(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (char)(causeEvent.getChar(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledSharedProperty<Character> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((char)(causeEvent.getSharedChar() + 1));
                    }
                },
                new BubbledPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledProperty<Byte> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (byte)(causeEvent.getByte(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (byte)(causeEvent.getByte(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledSharedProperty<Byte> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((byte)(causeEvent.getSharedByte() + 1));
                    }
                },
                new BubbledPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledProperty<Short> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (short)(causeEvent.getShort(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (short)(causeEvent.getShort(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledSharedProperty<Short> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((short)(causeEvent.getSharedShort() + 1));
                    }
                },
                new BubbledPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledProperty<Integer> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getInt(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getInt(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledSharedProperty<Integer> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedInt() + 1);
                    }
                },
                new BubbledPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledProperty<Long> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getLong(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getLong(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledSharedProperty<Long> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedLong() + 1);
                    }
                },
                new BubbledPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledProperty<Float> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getFloat(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getFloat(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledSharedProperty<Float> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedFloat() + 1);
                    }
                },
                new BubbledPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledProperty<Double> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getDouble(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getDouble(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledSharedProperty<Double> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedDouble() + 1);
                    }
                },
                new BubbledPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledProperty<String> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getString(PropertyVersion.DETACH) + ":bubbled");
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getString(PropertyVersion.ATTACH) + ":bubbled");
                        }
                    }
                },
                new BubbledSharedPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledSharedProperty<String> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedString() + ":bubbled");
                    }
                },
                new BubbledPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledProperty<BigDecimal> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getGeneric(PropertyVersion.DETACH).add(new BigDecimal("+1")));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getGeneric(PropertyVersion.ATTACH).add(new BigDecimal("-1")));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledSharedProperty<BigDecimal> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedGeneric().add(new BigDecimal("+1")));
                    }
                });
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.DETACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(true, e.isSharedBoolean());
        Assert.assertEquals('B', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('\0', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('b', e.getSharedChar());
        Assert.assertEquals((byte)2, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)3, e.getSharedByte());
        Assert.assertEquals((short)3, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)4, e.getSharedShort());
        Assert.assertEquals(4, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(0, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(5, e.getSharedInt());
        Assert.assertEquals(5L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(0L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(6L, e.getSharedLong());
        Assert.assertEquals(6F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(7F, e.getSharedFloat());
        Assert.assertEquals(7D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(8D, e.getSharedDouble());
        Assert.assertEquals("Old-String:bubbled", e.getString(PropertyVersion.DETACH));
        Assert.assertNull(e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String:bubbled", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234568.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertNull(e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345679.9"), e.getSharedGeneric());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBubbleAttachEventWithConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createAttachEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                'X', 
                'a',
                (byte)1,
                (byte)2,
                (short)2,
                (short)3,
                3,
                4,
                4L,
                5L,
                5F,
                6L,
                6D,
                7D, 
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"), 
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                new BubbledPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledProperty<Boolean> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(!causeEvent.isBoolean(PropertyVersion.DETACH));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(!causeEvent.isBoolean(PropertyVersion.ATTACH));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledSharedProperty<Boolean> bubbledSharedProperty) {
                        bubbledSharedProperty.setValue(!bubbledSharedProperty.getValue());
                    }
                },
                new BubbledPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledProperty<Character> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (char)(causeEvent.getChar(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (char)(causeEvent.getChar(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledSharedProperty<Character> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((char)(causeEvent.getSharedChar() + 1));
                    }
                },
                new BubbledPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledProperty<Byte> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (byte)(causeEvent.getByte(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (byte)(causeEvent.getByte(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledSharedProperty<Byte> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((byte)(causeEvent.getSharedByte() + 1));
                    }
                },
                new BubbledPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledProperty<Short> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (short)(causeEvent.getShort(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (short)(causeEvent.getShort(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledSharedProperty<Short> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((short)(causeEvent.getSharedShort() + 1));
                    }
                },
                new BubbledPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledProperty<Integer> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getInt(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getInt(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledSharedProperty<Integer> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedInt() + 1);
                    }
                },
                new BubbledPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledProperty<Long> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getLong(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getLong(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledSharedProperty<Long> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedLong() + 1);
                    }
                },
                new BubbledPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledProperty<Float> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getFloat(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getFloat(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledSharedProperty<Float> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedFloat() + 1);
                    }
                },
                new BubbledPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledProperty<Double> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getDouble(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getDouble(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledSharedProperty<Double> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedDouble() + 1);
                    }
                },
                new BubbledPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledProperty<String> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getString(PropertyVersion.DETACH) + ":bubbled");
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getString(PropertyVersion.ATTACH) + ":bubbled");
                        }
                    }
                },
                new BubbledSharedPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledSharedProperty<String> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedString() + ":bubbled");
                    }
                },
                new BubbledPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledProperty<BigDecimal> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getGeneric(PropertyVersion.DETACH).add(new BigDecimal("+1")));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getGeneric(PropertyVersion.ATTACH).add(new BigDecimal("-1")));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledSharedProperty<BigDecimal> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedGeneric().add(new BigDecimal("+1")));
                    }
                });
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.ATTACH, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(true, e.isSharedBoolean());
        Assert.assertEquals('\0', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('W', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('b', e.getSharedChar());
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)0, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)3, e.getSharedByte());
        Assert.assertEquals((short)0, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)1, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)4, e.getSharedShort());
        Assert.assertEquals(0, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(2, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(5, e.getSharedInt());
        Assert.assertEquals(0L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(3L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(6L, e.getSharedLong());
        Assert.assertEquals(0F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(4F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(7F, e.getSharedFloat());
        Assert.assertEquals(0D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(5D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(8D, e.getSharedDouble());
        Assert.assertNull(e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String:bubbled", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String:bubbled", e.getSharedString());
        Assert.assertNull(e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("1234566.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345679.9"), e.getSharedGeneric());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testBubbleReplaceEventWithConverter() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createReplaceEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                false,
                'A', 
                'X',
                'a',
                (byte)1,
                (byte)-1,
                (byte)2,
                (short)2,
                (short)-2,
                (short)3,
                3,
                -3,
                4,
                4L,
                -4L,
                5L,
                5F,
                -5F,
                6L,
                6D,
                -6D,
                7D, 
                "Old-String",
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"),
                new BigDecimal("-1234567.8"),
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = TestedEvent.<BigDecimal>bubbleEvent(
                this, 
                new Cause(VIEW, originalEvent), 
                new BubbledPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledProperty<Boolean> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(!causeEvent.isBoolean(PropertyVersion.DETACH));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(!causeEvent.isBoolean(PropertyVersion.ATTACH));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Boolean>() {
                    @Override
                    public void convert(BubbledSharedProperty<Boolean> bubbledSharedProperty) {
                        bubbledSharedProperty.setValue(!bubbledSharedProperty.getValue());
                    }
                },
                new BubbledPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledProperty<Character> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (char)(causeEvent.getChar(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (char)(causeEvent.getChar(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Character>() {
                    @Override
                    public void convert(BubbledSharedProperty<Character> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((char)(causeEvent.getSharedChar() + 1));
                    }
                },
                new BubbledPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledProperty<Byte> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (byte)(causeEvent.getByte(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (byte)(causeEvent.getByte(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Byte>() {
                    @Override
                    public void convert(BubbledSharedProperty<Byte> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((byte)(causeEvent.getSharedByte() + 1));
                    }
                },
                new BubbledPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledProperty<Short> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    (short)(causeEvent.getShort(PropertyVersion.DETACH) + 1));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    (short)(causeEvent.getShort(PropertyVersion.ATTACH) - 1));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Short>() {
                    @Override
                    public void convert(BubbledSharedProperty<Short> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue((short)(causeEvent.getSharedShort() + 1));
                    }
                },
                new BubbledPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledProperty<Integer> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getInt(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getInt(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Integer>() {
                    @Override
                    public void convert(BubbledSharedProperty<Integer> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedInt() + 1);
                    }
                },
                new BubbledPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledProperty<Long> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getLong(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getLong(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Long>() {
                    @Override
                    public void convert(BubbledSharedProperty<Long> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedLong() + 1);
                    }
                },
                new BubbledPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledProperty<Float> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getFloat(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getFloat(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Float>() {
                    @Override
                    public void convert(BubbledSharedProperty<Float> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedFloat() + 1);
                    }
                },
                new BubbledPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledProperty<Double> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getDouble(PropertyVersion.DETACH) + 1);
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getDouble(PropertyVersion.ATTACH) - 1);
                        }
                    }
                },
                new BubbledSharedPropertyConverter<Double>() {
                    @Override
                    public void convert(BubbledSharedProperty<Double> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedDouble() + 1);
                    }
                },
                new BubbledPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledProperty<String> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getString(PropertyVersion.DETACH) + ":bubbled");
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getString(PropertyVersion.ATTACH) + ":bubbled");
                        }
                    }
                },
                new BubbledSharedPropertyConverter<String>() {
                    @Override
                    public void convert(BubbledSharedProperty<String> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedString() + ":bubbled");
                    }
                },
                new BubbledPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledProperty<BigDecimal> bubbledProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledProperty.getCause().getEvent();
                        if (bubbledProperty.isValueToDetachEnabled()) {
                            bubbledProperty.setValueToDetach(
                                    causeEvent.getGeneric(PropertyVersion.DETACH).add(new BigDecimal("+1")));
                        }
                        if (bubbledProperty.isValueToAttachEnabled()) {
                            bubbledProperty.setValueToAttach(
                                    causeEvent.getGeneric(PropertyVersion.ATTACH).add(new BigDecimal("-1")));
                        }
                    }
                },
                new BubbledSharedPropertyConverter<BigDecimal>() {
                    @Override
                    public void convert(BubbledSharedProperty<BigDecimal> bubbledSharedProperty) {
                        TestedEvent<BigDecimal> causeEvent = 
                            (TestedEvent<BigDecimal>)bubbledSharedProperty.getCause().getEvent();
                        bubbledSharedProperty.setValue(causeEvent.getSharedGeneric().add(new BigDecimal("+1")));
                    }
                });
        Assert.assertEquals(EventType.BUBBLED, e.getEventType());
        Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertNull(e.getModification());
        Assert.assertSame(originalEvent, e.getCause().getEvent());
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(true, e.isSharedBoolean());
        Assert.assertEquals('B', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('W', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('b', e.getSharedChar());
        Assert.assertEquals((byte)2, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)-2, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)3, e.getSharedByte());
        Assert.assertEquals((short)3, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)-3, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)4, e.getSharedShort());
        Assert.assertEquals(4, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(-4, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(5, e.getSharedInt());
        Assert.assertEquals(5L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(-5L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(6L, e.getSharedLong());
        Assert.assertEquals(6F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(-6F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(7F, e.getSharedFloat());
        Assert.assertEquals(7D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(-7D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(8D, e.getSharedDouble());
        Assert.assertEquals("Old-String:bubbled", e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String:bubbled", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String:bubbled", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234568.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("-1234568.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345679.9"), e.getSharedGeneric());
    }
    
    @Test
    public void testDispatch() {
        TestedEvent<BigDecimal> originalEvent = TestedEvent.<BigDecimal>createReplaceEvent(
                this, 
                MODIFICATION, 
                true, 
                false,
                false,
                'A', 
                'X',
                'a',
                (byte)1,
                (byte)-1,
                (byte)2,
                (short)2,
                (short)-2,
                (short)3,
                3,
                -3,
                4,
                4L,
                -4L,
                5L,
                5F,
                -5F,
                6L,
                6D,
                -6D,
                7D, 
                "Old-String",
                "New-String",
                "Shared-String",
                new BigDecimal("1234567.8"),
                new BigDecimal("-1234567.8"),
                new BigDecimal("2345678.9"));
        TestedEvent<BigDecimal> e = originalEvent.dispatch(this);
        Assert.assertEquals(EventType.PROTOSOMATIC, e.getEventType());
        Assert.assertEquals(ModificationType.REPLACE, e.getModificationType());
        Assert.assertSame(this, e.getSource());
        Assert.assertSame(MODIFICATION, e.getModification());
        Assert.assertNull(e.getCause());
        Assert.assertEquals(true, e.isBoolean(PropertyVersion.DETACH));
        Assert.assertEquals(false, e.isBoolean(PropertyVersion.ATTACH));
        Assert.assertEquals(false, e.isSharedBoolean());
        Assert.assertEquals('A', e.getChar(PropertyVersion.DETACH));
        Assert.assertEquals('X', e.getChar(PropertyVersion.ATTACH));
        Assert.assertEquals('a', e.getSharedChar());
        Assert.assertEquals((byte)1, e.getByte(PropertyVersion.DETACH));
        Assert.assertEquals((byte)-1, e.getByte(PropertyVersion.ATTACH));
        Assert.assertEquals((byte)2, e.getSharedByte());
        Assert.assertEquals((short)2, e.getShort(PropertyVersion.DETACH));
        Assert.assertEquals((short)-2, e.getShort(PropertyVersion.ATTACH));
        Assert.assertEquals((short)3, e.getSharedShort());
        Assert.assertEquals(3, e.getInt(PropertyVersion.DETACH));
        Assert.assertEquals(-3, e.getInt(PropertyVersion.ATTACH));
        Assert.assertEquals(4, e.getSharedInt());
        Assert.assertEquals(4L, e.getLong(PropertyVersion.DETACH));
        Assert.assertEquals(-4L, e.getLong(PropertyVersion.ATTACH));
        Assert.assertEquals(5L, e.getSharedLong());
        Assert.assertEquals(5F, e.getFloat(PropertyVersion.DETACH));
        Assert.assertEquals(-5F, e.getFloat(PropertyVersion.ATTACH));
        Assert.assertEquals(6F, e.getSharedFloat());
        Assert.assertEquals(6D, e.getDouble(PropertyVersion.DETACH));
        Assert.assertEquals(-6D, e.getDouble(PropertyVersion.ATTACH));
        Assert.assertEquals(7D, e.getSharedDouble());
        Assert.assertEquals("Old-String", e.getString(PropertyVersion.DETACH));
        Assert.assertEquals("New-String", e.getString(PropertyVersion.ATTACH));
        Assert.assertEquals("Shared-String", e.getSharedString());
        Assert.assertEquals(new BigDecimal("1234567.8"), e.getGeneric(PropertyVersion.DETACH));
        Assert.assertEquals(new BigDecimal("-1234567.8"), e.getGeneric(PropertyVersion.ATTACH));
        Assert.assertEquals(new BigDecimal("2345678.9"), e.getSharedGeneric());
    }
    
    @EventDeclaration(properties = {
            @EventProperty(name = "boolean"),
            @EventProperty(name = "sharedBoolean", shared = true),
            @EventProperty(name = "char"),
            @EventProperty(name = "sharedChar", shared = true),
            @EventProperty(name = "byte"),
            @EventProperty(name = "sharedByte", shared = true),
            @EventProperty(name = "short"),
            @EventProperty(name = "sharedShort", shared = true),
            @EventProperty(name = "int"),
            @EventProperty(name = "sharedInt", shared = true),
            @EventProperty(name = "long"),
            @EventProperty(name = "sharedLong", shared = true),
            @EventProperty(name = "float"),
            @EventProperty(name = "sharedFloat", shared = true),
            @EventProperty(name = "double"),
            @EventProperty(name = "sharedDouble", shared = true),
            @EventProperty(name = "string"),
            @EventProperty(name = "sharedString", shared = true),
            @EventProperty(name = "generic"),
            @EventProperty(name = "sharedGeneric", shared = true)
    })
    private static abstract class TestedEvent<T> extends ModificationEvent {
        
        private static final long serialVersionUID = -4189707503444994152L;
        
        private final static Factory<?> FACTORY = getFactory(Factory.class);

        protected TestedEvent(Object source) {
            super (source);
        }
        
        protected TestedEvent(Object source, Cause cause) {
            super (source, cause);
        }
        
        protected TestedEvent(Object source, TestedEvent<T> dispatchedSourceEvent) {
            super (source, dispatchedSourceEvent);
        }
        
        public abstract TestedEvent<T> dispatch(Object source);
        
        public abstract boolean isBoolean(PropertyVersion version);
        
        public abstract boolean isSharedBoolean();
        
        public abstract char getChar(PropertyVersion version);
        
        public abstract char getSharedChar();
        
        public abstract byte getByte(PropertyVersion version);
        
        public abstract byte getSharedByte();
        
        public abstract short getShort(PropertyVersion version);
        
        public abstract short getSharedShort();
        
        public abstract int getInt(PropertyVersion version);
        
        public abstract int getSharedInt();
        
        public abstract long getLong(PropertyVersion version);
        
        public abstract long getSharedLong();
        
        public abstract float getFloat(PropertyVersion version);
        
        public abstract float getSharedFloat();
        
        public abstract double getDouble(PropertyVersion version);
        
        public abstract double getSharedDouble();
        
        public abstract String getString(PropertyVersion version);
        
        public abstract String getSharedString();
        
        public abstract T getGeneric(PropertyVersion version);
        
        public abstract T getSharedGeneric();
        
        @SuppressWarnings("unchecked")
        public static <T> TestedEvent<T> createDetachEvent(
                    Object source,
                    Modification modification, 
                    boolean $boolean,
                    boolean sharedBoolean,
                    char $char,
                    char sharedChar,
                    byte $byte,
                    byte sharedByte,
                    short $short,
                    short sharedShort,
                    int $int,
                    int sharedInt,
                    long $long,
                    long sharedLong,
                    float $float,
                    float sharedFloat,
                    double $double,
                    double sharedDouble,
                    String string,
                    String sharedString,
                    T generic, 
                    T sharedGeneric) {
            return ((Factory<T>)FACTORY).createDetachEvent(
                    source, 
                    modification, 
                    $boolean,
                    sharedBoolean,
                    $char,
                    sharedChar,
                    $byte,
                    sharedByte,
                    $short,
                    sharedShort,
                    $int,
                    sharedInt,
                    $long,
                    sharedLong,
                    $float,
                    sharedFloat,
                    $double,
                    sharedDouble,
                    string,
                    sharedString,
                    generic, 
                    sharedGeneric);
        }
        
        @SuppressWarnings("unchecked")
        public static <T> TestedEvent<T> createAttachEvent(
                    Object source,
                    Modification modification, 
                    boolean $boolean,
                    boolean sharedBoolean,
                    char $char,
                    char sharedChar,
                    byte $byte,
                    byte sharedByte,
                    short $short,
                    short sharedShort,
                    int $int,
                    int sharedInt,
                    long $long,
                    long sharedLong,
                    float $float,
                    float sharedFloat,
                    double $double,
                    double sharedDouble,
                    String string,
                    String sharedString,
                    T generic, 
                    T sharedGeneric) {
            return ((Factory<T>)FACTORY).createAttachEvent(
                    source, 
                    modification, 
                    $boolean,
                    sharedBoolean,
                    $char,
                    sharedChar,
                    $byte,
                    sharedByte,
                    $short,
                    sharedShort,
                    $int,
                    sharedInt,
                    $long,
                    sharedLong,
                    $float,
                    sharedFloat,
                    $double,
                    sharedDouble,
                    string,
                    sharedString,
                    generic, 
                    sharedGeneric);
        }
        
        @SuppressWarnings("unchecked")
        public static <T> TestedEvent<T> createReplaceEvent(
                Object source,
                Modification modification,
                boolean willBeDetachedBoolean,
                boolean willBeAttachedBoolean,
                boolean sharedBoolean,
                char willBeDetachedChar,
                char willBeAttachedChar,
                char sharedChar,
                byte willBeDetachedByte,
                byte willBeAttachedByte,
                byte sharedByte,
                short willBeDetachedShort,
                short willBeAttachedShort,
                short sharedShort,
                int willBeDetachedInt,
                int willBeAttachedInt,
                int sharedInt,
                long willBeDetachedLong,
                long willBeAttachedLong,
                long sharedLong,
                float willBeDetachedFloat,
                float willBeAttachedFloat,
                float sharedFloat,
                double willBeDetachedDouble,
                double willBeAttachedDouble,
                double sharedDouble,
                String willBeDetachedString,
                String willBeAttachedString,
                String sharedString,
                T willBeDetachedGeneric,
                T willBeAttachedGeneric,
                T sharedGeneric) {
            return ((Factory<T>)FACTORY).createReplaceEvent(
                    source, 
                    modification, 
                    willBeDetachedBoolean, 
                    willBeAttachedBoolean, 
                    sharedBoolean, 
                    willBeDetachedChar, 
                    willBeAttachedChar, 
                    sharedChar, 
                    willBeDetachedByte, 
                    willBeAttachedByte, 
                    sharedByte, 
                    willBeDetachedShort, 
                    willBeAttachedShort, 
                    sharedShort, 
                    willBeDetachedInt, 
                    willBeAttachedInt, 
                    sharedInt, 
                    willBeDetachedLong, 
                    willBeAttachedLong, 
                    sharedLong, 
                    willBeDetachedFloat, 
                    willBeAttachedFloat, 
                    sharedFloat, 
                    willBeDetachedDouble, 
                    willBeAttachedDouble, 
                    sharedDouble, 
                    willBeDetachedString, 
                    willBeAttachedString, 
                    sharedString, 
                    willBeDetachedGeneric, 
                    willBeAttachedGeneric, 
                    sharedGeneric);
        }
        
        @SuppressWarnings("unchecked")
        public static <T> TestedEvent<T> bubbleEvent(
                    Object source,
                    Cause cause,
                    BubbledPropertyConverter<Boolean> booleanConverter,
                    BubbledSharedPropertyConverter<Boolean> sharedBooleanConverter,
                    BubbledPropertyConverter<Character> charConverter,
                    BubbledSharedPropertyConverter<Character> sharedCharConverter,
                    BubbledPropertyConverter<Byte> byteConverter,
                    BubbledSharedPropertyConverter<Byte> sharedByteConverter,
                    BubbledPropertyConverter<Short> shortConverter,
                    BubbledSharedPropertyConverter<Short> sharedShortConverter,
                    BubbledPropertyConverter<Integer> intConverter,
                    BubbledSharedPropertyConverter<Integer> sharedIntConverter,
                    BubbledPropertyConverter<Long> longConverter,
                    BubbledSharedPropertyConverter<Long> sharedLongConverter,
                    BubbledPropertyConverter<Float> floatConverter,
                    BubbledSharedPropertyConverter<Float> sharedFloatConverter,
                    BubbledPropertyConverter<Double> doubleConverter,
                    BubbledSharedPropertyConverter<Double> sharedDoubleConverter,
                    BubbledPropertyConverter<String> stringConverter,
                    BubbledSharedPropertyConverter<String> sharedStringConverter,
                    BubbledPropertyConverter<T> genericConverter,
                    BubbledSharedPropertyConverter<T> sharedGenericConverter) {
            return ((Factory<T>)FACTORY).bubbleEvent(
                    source, 
                    cause, 
                    booleanConverter, 
                    sharedBooleanConverter, 
                    charConverter, 
                    sharedCharConverter, 
                    byteConverter, 
                    sharedByteConverter, 
                    shortConverter, 
                    sharedShortConverter, 
                    intConverter, 
                    sharedIntConverter, 
                    longConverter, 
                    sharedLongConverter, 
                    floatConverter, 
                    sharedFloatConverter, 
                    doubleConverter, 
                    sharedDoubleConverter, 
                    stringConverter, 
                    sharedStringConverter, 
                    genericConverter, 
                    sharedGenericConverter);
        }
        
        @EventFactory
        private interface Factory<T> {
        
            TestedEvent<T> createDetachEvent(
                    Object source,
                    Modification modification, 
                    boolean $boolean,
                    boolean sharedBoolean,
                    char $char,
                    char sharedChar,
                    byte $byte,
                    byte sharedByte,
                    short $short,
                    short sharedShort,
                    int $int,
                    int sharedInt,
                    long $long,
                    long sharedLong,
                    float $float,
                    float sharedFloat,
                    double $double,
                    double sharedDouble,
                    String string,
                    String sharedString,
                    T generic, 
                    T sharedGeneric);
            
            TestedEvent<T> createAttachEvent(
                    Object source,
                    Modification modification, 
                    boolean $boolean,
                    boolean sharedBoolean,
                    char $char,
                    char sharedChar,
                    byte $byte,
                    byte sharedByte,
                    short $short,
                    short sharedShort,
                    int $int,
                    int sharedInt,
                    long $long,
                    long sharedLong,
                    float $float,
                    float sharedFloat,
                    double $double,
                    double sharedDouble,
                    String string,
                    String sharedString,
                    T generic, 
                    T sharedGeneric);
            
            TestedEvent<T> createReplaceEvent(
                    Object source,
                    Modification modification,
                    boolean willBeDetachedBoolean,
                    boolean willBeAttachedBoolean,
                    boolean sharedBoolean,
                    char willBeDetachedChar,
                    char willBeAttachedChar,
                    char sharedChar,
                    byte willBeDetachedByte,
                    byte willBeAttachedByte,
                    byte sharedByte,
                    short willBeDetachedShort,
                    short willBeAttachedShort,
                    short sharedShort,
                    int willBeDetachedInt,
                    int willBeAttachedInt,
                    int sharedInt,
                    long willBeDetachedLong,
                    long willBeAttachedLong,
                    long sharedLong,
                    float willBeDetachedFloat,
                    float willBeAttachedFloat,
                    float sharedFloat,
                    double willBeDetachedDouble,
                    double willBeAttachedDouble,
                    double sharedDouble,
                    String willBeDetachedString,
                    String willBeAttachedString,
                    String sharedString,
                    T willBeDetachedGeneric,
                    T willBeAttachedGeneric,
                    T sharedGeneric);
            
            TestedEvent<T> bubbleEvent(
                    Object source,
                    Cause cause,
                    BubbledPropertyConverter<Boolean> booleanConverter,
                    BubbledSharedPropertyConverter<Boolean> sharedBooleanConverter,
                    BubbledPropertyConverter<Character> charConverter,
                    BubbledSharedPropertyConverter<Character> sharedCharConverter,
                    BubbledPropertyConverter<Byte> byteConverter,
                    BubbledSharedPropertyConverter<Byte> sharedByteConverter,
                    BubbledPropertyConverter<Short> shortConverter,
                    BubbledSharedPropertyConverter<Short> sharedShortConverter,
                    BubbledPropertyConverter<Integer> intConverter,
                    BubbledSharedPropertyConverter<Integer> sharedIntConverter,
                    BubbledPropertyConverter<Long> longConverter,
                    BubbledSharedPropertyConverter<Long> sharedLongConverter,
                    BubbledPropertyConverter<Float> floatConverter,
                    BubbledSharedPropertyConverter<Float> sharedFloatConverter,
                    BubbledPropertyConverter<Double> doubleConverter,
                    BubbledSharedPropertyConverter<Double> sharedDoubleConverter,
                    BubbledPropertyConverter<String> stringConverter,
                    BubbledSharedPropertyConverter<String> sharedStringConverter,
                    BubbledPropertyConverter<T> genericConverter,
                    BubbledSharedPropertyConverter<T> sharedGenericConverter);
        }
    }
    
}
