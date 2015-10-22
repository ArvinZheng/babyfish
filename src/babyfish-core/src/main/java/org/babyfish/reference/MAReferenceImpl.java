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
package org.babyfish.reference;

import org.babyfish.lang.Combiner;
import org.babyfish.lang.Combiners;
import org.babyfish.lang.EmptyAction;
import org.babyfish.modificationaware.event.AttributeScope;
import org.babyfish.modificationaware.event.ModificationEventHandleException;
import org.babyfish.modificationaware.event.spi.GlobalAttributeContext;
import org.babyfish.modificationaware.event.spi.InAllChainAttributeContext;
import org.babyfish.reference.event.ValueEvent;
import org.babyfish.reference.event.ValueListener;
import org.babyfish.reference.event.modification.ReferenceModifications;

/**
 * @author Tao Chen
 */
public class MAReferenceImpl<T> extends ReferenceImpl<T> implements MAReference<T> {
    
    private static final long serialVersionUID = -5810451036533759049L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Combiner<ValueListener> VALUE_LISTENER_COMBINER = 
        (Combiner)Combiners.of(ValueListener.class);
    
    private static final Object AK_VALUE_LISTENER = new Object();
    
    protected transient ValueListener<T> valueListener;

    public MAReferenceImpl() {
        
    }

    public MAReferenceImpl(ReferenceComparator<? super T> comparator) {
        super(comparator);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void addValueListener(ValueListener<? super T> listener) {
        this.valueListener = 
            (ValueListener<T>)VALUE_LISTENER_COMBINER.combine(
                    (ValueListener)this.valueListener, 
                    (ValueListener)listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void removeValueListener(ValueListener<? super T> listener) {
        this.valueListener = 
            (ValueListener<T>)VALUE_LISTENER_COMBINER.remove(
                    (ValueListener)this.valueListener, 
                    (ValueListener)listener);
    }
    
    @Override
    public T set(final T value) {
        T oldValue = this.value;
        if (oldValue == value) {
            return oldValue;
        }
        this.validate(value);
        ValueEvent<T> event = ValueEvent.createReplaceEvent(
                this, 
                ReferenceModifications.set(value), 
                oldValue, 
                value);
        this.setRaw(event, new EmptyAction() {
            @Override
            public void run() {
                MAReferenceImpl.this.value = value;
            }
        });
        return oldValue;
    }

    protected void executeModifying(ValueEvent<T> e) {
        Throwable finalThrowable = null;
        try {
            this.onModifying(e);    
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.raiseModifying(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationEventHandleException(false, e, finalThrowable);
        }
    }

    protected void executeModified(ValueEvent<T> e) {
        Throwable finalThrowable = null;
        try {
            this.raiseModified(e);
        } catch (Throwable ex) {
            finalThrowable = ex;
        }
        try {
            this.onModified(e);
        } catch (Throwable ex) {
            if (finalThrowable == null) {
                finalThrowable = ex;
            }
        }
        if (finalThrowable != null) {
            throw new ModificationEventHandleException(true, e, finalThrowable);
        }
    }
    
    protected void onModifying(ValueEvent<T> e) throws Throwable {
        
    }
    
    protected void onModified(ValueEvent<T> e) throws Throwable {
        
    }
    
    protected void raiseModifying(ValueEvent<T> e) throws Throwable {
        ValueListener<T> valueListener = this.valueListener;
        if (valueListener != null) {
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .addAttribute(AK_VALUE_LISTENER, valueListener);
            valueListener.modifying(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    protected void raiseModified(ValueEvent<T> e) throws Throwable {
        ValueListener<T> valueListener = 
            (ValueListener<T>)
            e
            .getAttributeContext(AttributeScope.LOCAL)
            .removeAttribute(AK_VALUE_LISTENER);
        if (valueListener != null) {
            valueListener.modified(e);
        }
    }
    
    protected void setRaw(ValueEvent<T> event, EmptyAction rawSetter) {
        
        Throwable finalException = null;
        try {
            this.executeModifying(event);
        } catch (RuntimeException | Error ex) {
            ((InAllChainAttributeContext)event.getAttributeContext(AttributeScope.IN_ALL_CHAIN)).setPreThrowable(ex);
            if (finalException == null) {
                finalException = ex;
            }
        }
        try {
            rawSetter.run();
            ((GlobalAttributeContext)event.getModification().getAttributeContext()).success();
        } catch (RuntimeException | Error ex) {
            ((GlobalAttributeContext)event.getModification().getAttributeContext()).setThrowable(ex);
            if (finalException == null) {
                finalException = ex;
            }
        }
        try {
            this.executeModified(event);
        } catch (RuntimeException | Error ex) {
            if (finalException == null) {
                finalException = ex;
            }
        }
        if (finalException instanceof RuntimeException) {
            throw (RuntimeException)finalException;
        }
        if (finalException instanceof Error) {
            throw (Error)finalException;
        }
    }
}
