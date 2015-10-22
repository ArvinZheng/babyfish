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

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.babyfish.collection.MANavigableMap;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MATreeMap;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.collection.event.EntryElementEvent;
import org.babyfish.collection.event.MapElementEvent;
import org.babyfish.collection.event.MapElementEvent.MapModification;
import org.babyfish.collection.event.modification.CollectionModifications;
import org.babyfish.collection.event.modification.EntryModifications;
import org.babyfish.collection.event.modification.MapModifications;
import org.babyfish.collection.viewinfo.NavigableMapViewInfos;
import org.babyfish.collection.viewinfo.NavigableSetViewInfos;
import org.babyfish.lang.reflect.ClassInfo;
import org.babyfish.lang.reflect.MethodInfo;
import org.babyfish.modificationaware.event.Cause;
import org.babyfish.modificationaware.event.ModificationEvent;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class BridgeTest {
    
    private static final Field DISPATCHED_EVENTS_FIELD;

    @Test
    public void testMapElementEvent() {
        MANavigableMap<String, String> fakeMap = new MATreeMap<>();
        MANavigableMap<String, String> dispatchMap = new MATreeMap<>();
        MapElementEvent<String, String> event = MapElementEvent.createReplaceEvent(
                fakeMap.descendingMap(), 
                MapModifications.put("One", "I"),  "One", "1", "One", "I");
        testMapElementEvent(event);
        event = MapElementEvent.bubbleEvent(
                fakeMap, 
                new Cause(NavigableMapViewInfos.descendingMap(), event), 
                null,
                null);
        testMapElementEvent(event);
        clearDispatchedEvents(event);
        event = event.dispatch(dispatchMap);
        testMapElementEvent(event);
    }
    
    @Test
    public void testElementEvent() {
        MANavigableSet<String> fakeSet = new MATreeSet<>();
        ElementEvent<String> event = ElementEvent.createDetachEvent(
                fakeSet, 
                CollectionModifications.<String>remove("element"),
                "element");
        MANavigableSet<String> dispatchSet = new MATreeSet<>();
        testElementEvent(event);
        event = ElementEvent.bubbleEvent(
                fakeSet, 
                new Cause(NavigableSetViewInfos.descendingSet(), event), 
                null);
        testElementEvent(event);
        clearDispatchedEvents(event);
        event = event.dispatch(dispatchSet);
        testElementEvent(event);
    }
    
    @Test
    public void testEntryElementEvent() {
        MANavigableMap<String, String> fakeMap = new MATreeMap<>();
        fakeMap.put("A", "a");
        MANavigableMap<String, String> dispatchMap = new MATreeMap<>();
        dispatchMap.put("A", "a");
        EntryElementEvent<String, String> event = EntryElementEvent.createReplaceEvent(
                fakeMap.firstEntry(), 
                EntryModifications.set("a"), 
                "a", 
                "a",
                "A"); 
        testEntryElementEvent(event);
        clearDispatchedEvents(event);
        event = event.dispatch(dispatchMap);
        testEntryElementEvent(event);
    }

    private void testMapElementEvent(MapElementEvent<String, String> event) {
        MANavigableMap<String, String> dispatchMap = new MATreeMap<>();
        MethodInfo getModification = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        MapModification.class, 
                        "getModification");
        MethodInfo getModificationBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        org.babyfish.modificationaware.event.Modification.class, 
                        "getModification"
                );
        MethodInfo dispatch = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        MapElementEvent.class, 
                        "dispatch",
                        Object.class);
        MethodInfo dispatchBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ModificationEvent.class, 
                        "dispatch",
                        Object.class
                );
        Assert.assertTrue(getModification.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.MapElementEvent{"));
        Assert.assertTrue(getModificationBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.MapElementEvent{"));
        Assert.assertTrue(dispatch.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.MapElementEvent{"));
        Assert.assertTrue(dispatchBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.MapElementEvent{"));
        event.getModification();
        ((ModificationEvent)event).getModification();
        event.dispatch(dispatchMap);
        clearDispatchedEvents(event);
        ((ModificationEvent)event).dispatch(dispatchMap);
    }
    
    private void testElementEvent(ElementEvent<String> event) {
        MANavigableSet<String> dispatchSet = new MATreeSet<>();
        MethodInfo getModification = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ElementEvent.Modification.class, 
                        "getModification");
        MethodInfo getModificationBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        org.babyfish.modificationaware.event.Modification.class, 
                        "getModification"
                );
        MethodInfo dispatch = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ElementEvent.class, 
                        "dispatch",
                        Object.class);
        MethodInfo dispatchBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ModificationEvent.class, 
                        "dispatch",
                        Object.class
                );
        Assert.assertTrue(getModification.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.ElementEvent{"));
        Assert.assertTrue(getModificationBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.ElementEvent{"));
        Assert.assertTrue(dispatch.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.ElementEvent{"));
        Assert.assertTrue(dispatchBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.ElementEvent{"));
        event.getModification();
        ((ModificationEvent)event).getModification();
        event.dispatch(dispatchSet);
        clearDispatchedEvents(event);
        ((ModificationEvent)event).dispatch(dispatchSet);
    }
    
    private void testEntryElementEvent(EntryElementEvent<String, String> event) {
        MANavigableMap<String, String> dispatchMap = new MATreeMap<>();
        dispatchMap.put("A", "a");
        MethodInfo getModification = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        EntryModifications.SetByValue.class, 
                        "getModification");
        MethodInfo getModificationBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ElementEvent.Modification.class, 
                        "getModification"
                );
        MethodInfo getModificationBridge2 = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        org.babyfish.modificationaware.event.Modification.class, 
                        "getModification"
                );
        MethodInfo dispatch = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        EntryElementEvent.class, 
                        "dispatch",
                        Object.class);
        MethodInfo dispatchBridge = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ElementEvent.class, 
                        "dispatch",
                        Object.class);
        MethodInfo dispatchBridge2 = 
                ClassInfo.of(event.getClass())
                .getDeclaredErasedMethod(
                        ModificationEvent.class, 
                        "dispatch",
                        Object.class
                );
        Assert.assertTrue(getModification.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        Assert.assertTrue(getModificationBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        Assert.assertTrue(getModificationBridge2.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        Assert.assertTrue(dispatch.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        Assert.assertTrue(dispatchBridge.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        Assert.assertTrue(dispatchBridge2.getDeclaringClass().getName().startsWith(
                "org.babyfish.collection.event.EntryElementEvent{"));
        event.getModification();
        ((ModificationEvent)event).getModification();
        event.dispatch(dispatchMap.firstEntry());
        clearDispatchedEvents(event);
        ((ModificationEvent)event).dispatch(dispatchMap.firstEntry());
    }
    
    private static void clearDispatchedEvents(ModificationEvent e) {
        try {
            DISPATCHED_EVENTS_FIELD.set(e, null);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }
    
    static {
        try {
            DISPATCHED_EVENTS_FIELD = ModificationEvent.class.getDeclaredField("dispatchedEvents");
        } catch (NoSuchFieldException ex) {
            throw new AssertionError();
        }
        DISPATCHED_EVENTS_FIELD.setAccessible(true);
    }
}
