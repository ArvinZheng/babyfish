package org.babyfishdemo.foundation.combiner.event;

import java.util.EventObject;

import org.babyfishdemo.foundation.combiner.Book;

/**
 * @author Tao Chen
 */
public class PropertyChanagedEvent extends EventObject {

    private static final long serialVersionUID = 8323732437521704562L;

    private String propertyName;
    
    private Object oldValue;
    
    private Object newValue;
    
    private transient String toString;

    public PropertyChanagedEvent(
            Object source, 
            String propertyName, 
            Object oldValue,
            Object newValue) {
        super(source);
        this.propertyName = propertyName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public Book getSource() {
        return (Book)super.getSource();
    }

    public String getPropertyName() {
        return this.propertyName;
    }

    @SuppressWarnings("unchecked")
    public <T> T getOldValue() {
        return (T)this.oldValue;
    }

    @SuppressWarnings("unchecked")
    public <T> T getNewValue() {
        return (T)this.newValue;
    }

    @Override
    public String toString() {
        String str = this.toString;
        if (str == null) {
            this.toString = str = 
                    "{ propertyName: "
                    + text(this.propertyName)
                    + ", oldValue: "
                    + text(this.oldValue)
                    + ", newValue: "
                    + text(this.newValue)
                    + " }";
        }
        return str;
    }
    
    private static String text(Object value) {
        if (value == null) {
            return "null";
        }
        return '"' + value.toString() + '"';
    }
}
