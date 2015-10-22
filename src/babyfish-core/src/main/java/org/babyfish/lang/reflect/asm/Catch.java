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
package org.babyfish.lang.reflect.asm;

import java.util.List;

import org.babyfish.collection.MACollections;
import org.babyfish.lang.Action;
import org.babyfish.lang.Arguments;
import org.babyfish.org.objectweb.asm.Type;

/**
 * @author Tao Chen
 */
public class Catch {

    private static final String[] ANY = new String[] { null };
    
    private String[] exceptionInternalNames;
    
    private Action<XMethodVisitor> mvAction;
    
    public Catch(Action<XMethodVisitor> mvAction) {
        this.mvAction = 
                Arguments.mustNotBeNull(
                        "mvAction", 
                        mvAction);
        this.exceptionInternalNames = ANY;
    }
    
    public Catch(
            String exceptionInternalName,
            Action<XMethodVisitor> mvAction) {
        this(new String[] { exceptionInternalName }, mvAction);
    }
    
    @SuppressWarnings("unchecked")
    public Catch(
            Class<? extends Throwable> exceptionType,
            Action<XMethodVisitor> mvAction) {
        this(new Class[] { exceptionType }, mvAction);
    }
    
    public Catch(
            Type exceptionType,
            Action<XMethodVisitor> mvAction) {
        this(new Type[] { exceptionType }, mvAction);
    }
    
    public Catch(
            String[] exceptionInternalNames,
            Action<XMethodVisitor> mvAction) {
        if (exceptionInternalNames != null && exceptionInternalNames.length != 0) {
            this.exceptionInternalNames = exceptionInternalNames.clone();
        } else {
            this.exceptionInternalNames = ANY;
        }
        this.mvAction = 
                Arguments.mustNotBeNull(
                        "mvAction", 
                        mvAction);
    }
    
    public Catch(
            Class<? extends Throwable>[] exceptionTypes,
            Action<XMethodVisitor> mvAction) {
        if (exceptionTypes != null && exceptionTypes.length != 0) {
            String[] internalNames = new String[exceptionTypes.length];
            for (int i = internalNames.length - 1; i >= 0; i--) {
                internalNames[i] = ASM.getInternalName(exceptionTypes[i]);
            }
            this.exceptionInternalNames = internalNames;
        } else {
            this.exceptionInternalNames = ANY;
        }
        this.mvAction = 
                Arguments.mustNotBeNull(
                        "mvAction", 
                        mvAction);
    }
    
    public Catch(
            Type[] exceptionTypes,
            Action<XMethodVisitor> mvAction) {
        if (exceptionTypes != null && exceptionTypes.length != 0) {
            String[] internalNames = new String[exceptionTypes.length];
            for (int i = internalNames.length - 1; i >= 0; i--) {
                internalNames[i] = exceptionTypes[i].getInternalName();
            }
            this.exceptionInternalNames = internalNames;
        } else {
            this.exceptionInternalNames = new String[] { null };
        }
        this.mvAction = 
                Arguments.mustNotBeNull(
                        "mvAction", 
                        mvAction);
    }

    public List<String> getExceptionInternalNames() {
        return MACollections.wrap(this.exceptionInternalNames);
    }

    public Action<XMethodVisitor> getMvAction() {
        return this.mvAction;
    }
}
