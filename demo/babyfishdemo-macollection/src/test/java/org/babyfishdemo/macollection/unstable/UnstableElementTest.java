package org.babyfishdemo.macollection.unstable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import junit.framework.Assert;

import org.babyfish.collection.MACollections;
import org.babyfish.collection.MALinkedHashSet;
import org.babyfish.collection.MANavigableSet;
import org.babyfish.collection.MAOrderedSet;
import org.babyfish.collection.MATreeSet;
import org.babyfish.collection.event.ElementAdapter;
import org.babyfish.collection.event.ElementEvent;
import org.babyfish.modificationaware.event.PropertyVersion;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tao Chen
 */
public class UnstableElementTest {
    
    private Person hippasus;
    
    private Person hypatia;
    
    private Person servet;
    
    private Person bruno;
    
    @Before
    public void initPersons() {
        this.hippasus = new Person("Hippasus", null, Gender.MALE, date("BC500-01-01"));
        this.hypatia = new Person("Hypatia", null, Gender.FEMALE, date("AD0370-01-01"));
        this.servet = new Person("Miguel", "Servet", Gender.MALE, date("AD1511-09-29"));
        this.bruno = new Person("Giordano", "Bruno", Gender.MALE, date("AD1548-01-01"));
    }

    @Test
    public void testOrderedSetBaseOnFirstName() {
        MAOrderedSet<Person> set = new MALinkedHashSet<>(PersonComparators.FIRST_NAME_EQUALITY_COMPARATOR);
        set.addAll(MACollections.wrap(this.hippasus, this.hypatia, this.servet, this.bruno));
        
        ElementListenerImpl<Person> eli = new ElementListenerImpl<>();
        set.addElementListener(eli);
        
        assertCollection(set, this.hippasus, this.hypatia, this.servet, this.bruno);
        
        {
            this.hypatia.setFirstName("HYPATIA");
            assertCollection(set, this.hippasus, this.hypatia, this.servet, this.bruno);
            Assert.assertEquals(
                    "ElementEvent { "
                    +   "detachedElement: { "
                    +     "firstName: Hypatia, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "attchedElement: null, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$SuspendByElementViaFrozenContext{ "
                    +     "element : { "
                    +       "firstName: Hypatia, "
                    +       "lastName: null, "
                    +       "gender: FEMALE, "
                    +       "birthday: AD0370-01-01 "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ElementEvent { "
                    +   "detachedElement: null, "
                    +   "attchedElement: { "
                    +     "firstName: HYPATIA, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$ResumeViaFrozenContext "
                    + "}", 
                    eli.getHistoryAndClear()
            );
        }
        
        {
            this.hypatia.setFirstName("Hippasus");
            /*
             *  Set the first name of this.hypatia to be "Hippasus" that is conflict with the name of this.hippasus.
             *  In order to guarantee the unique constraint of the set, this.hippasus will be removed from the set
             *  because the replacementRule is NEW_REFERENCE_WIN
             *  (If the replacmentRule is OLD_REFERENCE_WIN, the modified element will be removed).
             */
            assertCollection(set, this.hypatia, this.servet, this.bruno);
            Assert.assertEquals(
                    "ElementEvent { "
                    +   "detachedElement: { "
                    +     "firstName: HYPATIA, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 }, "
                    +   "attchedElement: null, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$SuspendByElementViaFrozenContext{ "
                    +     "element : { "
                    +       "firstName: HYPATIA, "
                    +       "lastName: null, "
                    +       "gender: FEMALE, "
                    +       "birthday: AD0370-01-01 "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ElementEvent { "
                    +   "detachedElement: { " //this.hippasus is removed from the set to guarantee the unique constraint of the set
                    +     "firstName: Hippasus, "
                    +     "lastName: null, "
                    +     "gender: MALE, "
                    +     "birthday: BC0500-01-01 "
                    +   "}, "
                    +   "attchedElement: { "
                    +     "firstName: Hippasus, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$ResumeViaFrozenContext "
                    + "}",
                    eli.getHistoryAndClear()
            );
        }
    }
    
    @Test
    public void testNavigableSetBaseOnFirstName() {
        MANavigableSet<Person> set = new MATreeSet<>(PersonComparators.FIRST_NAME_COMPARATOR);
        set.addAll(MACollections.wrap(this.hippasus, this.hypatia, this.servet, this.bruno));
        
        ElementListenerImpl<Person> eli = new ElementListenerImpl<>();
        set.addElementListener(eli);
        
        /*
         * The natural order of firstName is:
         *      Giordano < Miguel < Hippasus < Hypatia
         */
        assertCollection(set, this.bruno, this.hippasus, this.hypatia, this.servet);
        
        {
            this.hypatia.setFirstName("HYPATIA");
            /*
             * The natural order of firstName is:
             *      Giordano  < HYPATIA < Hippasus < Miguel
             */
            assertCollection(set, this.bruno, this.hypatia, this.hippasus, this.servet);
            Assert.assertEquals(
                    "ElementEvent { "
                    +   "detachedElement: { "
                    +     "firstName: Hypatia, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "attchedElement: null, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$SuspendByElementViaFrozenContext{ "
                    +     "element : { "
                    +       "firstName: Hypatia, "
                    +       "lastName: null, "
                    +       "gender: FEMALE, "
                    +       "birthday: AD0370-01-01 "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ElementEvent { "
                    +   "detachedElement: null, "
                    +   "attchedElement: { "
                    +     "firstName: HYPATIA, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$ResumeViaFrozenContext "
                    + "}", 
                    eli.getHistoryAndClear()
            );
        }
    
        {
            this.hypatia.setFirstName("Hippasus");
            /*
             *  Set the first name of this.hypatia to be "Hippasus" that is conflict with the name of this.hippasus.
             *  
             *  (1) In order to guarantee the unique constraint of the set, this.hippasus will be removed from the set
             *  because the replacementRule is NEW_REFERENCE_WIN
             *  (If the replacmentRule is OLD_REFERENCE_WIN, the modified element will be removed).
             *  
             *  (2) After change, The natural order of firstName is:
             *    Giordano  < Hippasus(this.hypatia.firstName) < Miguel
             */
            assertCollection(set, this.bruno, this.hypatia, this.servet);
            Assert.assertEquals(
                    "ElementEvent { "
                    +   "detachedElement: { "
                    +     "firstName: HYPATIA, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "attchedElement: null, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$SuspendByElementViaFrozenContext{ "
                    +     "element : { "
                    +       "firstName: HYPATIA, "
                    +       "lastName: null, "
                    +       "gender: FEMALE, "
                    +       "birthday: AD0370-01-01 "
                    +     "} "
                    +   "} "
                    + "}"
                    + "ElementEvent { "
                    +   "detachedElement: { " //this.hippasus is removed from the set to guarantee the unique constraint of the set
                    +     "firstName: Hippasus, "
                    +     "lastName: null, "
                    +     "gender: MALE, "
                    +     "birthday: BC0500-01-01 "
                    +   "}, "
                    +   "attchedElement: { "
                    +     "firstName: Hippasus, "
                    +     "lastName: null, "
                    +     "gender: FEMALE, "
                    +     "birthday: AD0370-01-01 "
                    +   "}, "
                    +   "modification: org.babyfish.collection.event.modification.SetModifications$ResumeViaFrozenContext "
                    + "}",
                    eli.getHistoryAndClear()
            );
        }
    }
    
    private static Date date(String text) {
        try {
            return new SimpleDateFormat("Gyyyy-MM-dd", Locale.ENGLISH).parse(text);
        } catch (ParseException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
    
    @SafeVarargs
    private static <E> void assertCollection(Collection<E> c, E ... elements) {
        Assert.assertEquals(elements.length, c.size());
        int index = 0;
        for (E e : c) {
            Assert.assertSame(elements[index++], e);
        }
    }
    
    private static class ElementListenerImpl<E> extends ElementAdapter<E> {
        
        private StringBuilder builder = new StringBuilder();
        
        public String getHistoryAndClear() {
            String retval = this.builder.toString();
            this.builder.setLength(0);
            return retval;
        }
        
        @Override
        public void modified(ElementEvent<E> e) throws Throwable {
            this
            .builder
            .append("ElementEvent { detachedElement: ")
            .append(e.getElement(PropertyVersion.DETACH))
            .append(", attchedElement: ")
            .append(e.getElement(PropertyVersion.ATTACH))
            .append(", modification: ")
            .append(e.getModification())
            .append(" }");
        }
    }
}
